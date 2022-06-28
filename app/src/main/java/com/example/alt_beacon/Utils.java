package com.example.alt_beacon;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Config;
import com.google.ar.core.Session;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/** 퍼미션 단순화 정적 유틸리티 */
public class Utils {
    private static final String TAG = "Utils";
    private static final double MIN_OPENGL_VERSION = 3.0;

    private Utils() {}
    /**
     * 오류 메시지가 포함 된 토스트를 생성하고 보여줍니다. 예외가 전달 된 경우
     * 토스트에 추가됩니다. 오류는 로그에 기록됩니다
     */
    public static void displayError(
            final Context context, final String errorMsg, @Nullable final Throwable problem) {
        final String tag = context.getClass().getSimpleName();
        final String toastText;
        if (problem != null && problem.getMessage() != null) {
            Log.e(tag, errorMsg, problem);
            toastText = errorMsg + ": " + problem.getMessage();
        } else if (problem != null) {
            Log.e(tag, errorMsg, problem);
            toastText = errorMsg;
        } else {
            Log.e(tag, errorMsg);
            toastText = errorMsg;
        }

        new Handler(Looper.getMainLooper())
                .post(
                        new Runnable() {
                            @Override
                            public void run() {
                                Toast toast = Toast.makeText(context, toastText, Toast.LENGTH_LONG);
                                toast.setGravity(Gravity.CENTER, 0, 0);
                                toast.show();
                            }
                        });
    }
    /**
     * ARCore 세션을 생성하여 CAMERA 권한을 확인하고 부여 된 경우
     * ARCore 설치 상태 문제가있는 경우 예외가 발생 무한 검사 루프를 피하기 위해 필요에 따라 installRequested 플래그를 업데이트하는 데 사용
     * 이 메서드에서 null이 반환되면 *를 true로 설정
     * @param activity-현재 활성화 된 활동입니다.
     * @param installRequested-ARCore의 상태를 확인할 때 ARCore의 표시기
     * 설치가 이미 요청되었습니다.이 방법이 이전에 리턴 된 경우에 해당됩니다.
     * null이며 카메라 권한이 부여되었습니다.
     */
    public static Session createArSession(Activity activity, boolean installRequested)
            throws UnavailableException {
        Session session = null;
        //카메라 권한이 있으면 세션을 만듬
        switch (ArCoreApk.getInstance().requestInstall(activity, !installRequested)) {
            case INSTALL_REQUESTED:
                return null;
            case INSTALLED:
                break;
        }
        session = new Session(activity);
        //ArSceneView에는`LATEST_CAMERA_IMAGE` 비차단 업데이트 모드가 필요
        Config config = new Config(session);
        config.setUpdateMode(Config.UpdateMode.LATEST_CAMERA_IMAGE);
        session.configure(config);
        return session;
    }

    public static void handleSessionException(
            Activity activity, UnavailableException sessionException) {
        String message;
        if (sessionException instanceof UnavailableArcoreNotInstalledException) {
            message = "Please install ARCore";
        } else if (sessionException instanceof UnavailableApkTooOldException) {
            message = "Please update ARCore";
        } else if (sessionException instanceof UnavailableSdkTooOldException) {
            message = "Please update this app";
        } else if (sessionException instanceof UnavailableDeviceNotCompatibleException) {
            message = "This device does not support AR";
        } else {
            message = "Failed to create AR session";
            Log.e(TAG, "Exception: " + sessionException);
        }
        Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
    }

    public static boolean checkIsSupportedDeviceOrFinish(final Activity activity) {
        if (Build.VERSION.SDK_INT < VERSION_CODES.N) {
            Log.e(TAG, "이 앱에는 Android N 이상이 필요");
            Toast.makeText(activity, "이 앱에는 Android N 이상이 입니다.", Toast.LENGTH_LONG).show();
            activity.finish();
            return false;
        }
        String openGlVersionString =
                ((ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE))
                        .getDeviceConfigurationInfo()
                        .getGlEsVersion();
        if (Double.parseDouble(openGlVersionString) < MIN_OPENGL_VERSION) {
            Log.e(TAG, "Sceneform에는 OpenGL ES 3.0 이상이 필요");
            Toast.makeText(activity, "Sceneform에는 OpenGL ES 3.0 이상입니다.", Toast.LENGTH_LONG)
                    .show();
            activity.finish();
            return false;
        }
        return true;
    }

    public static String[] needPermissions = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.CAMERA
    };

    public static final int PERMISSION_REQUESTCODE = 0;

    public static void checkPermissions(final Activity activity) {
        List<String> needRequestPermissionList = findDeniedPermissions(activity,needPermissions);
        if (needRequestPermissionList != null
                && needRequestPermissionList.size() > 0) {
            String[] array = needRequestPermissionList.toArray(new String[needRequestPermissionList.size()]);
            activity.requestPermissions(array, PERMISSION_REQUESTCODE);
        }
    }

    public static List<String> findDeniedPermissions(final Activity activity,String[] permissions) {
        List<String> needRequestPermissionList = new ArrayList<String>();
        try {
            for (String perm : permissions) {
                Method checkSelfMethod = activity.getClass().getMethod("checkSelfPermission", String.class);
                Method shouldShowRequestPermissionRationaleMethod = activity.getClass().getMethod("shouldShowRequestPermissionRationale",
                        String.class);
                if ((Integer)checkSelfMethod.invoke(activity, perm) != PackageManager.PERMISSION_GRANTED
                        || (Boolean)shouldShowRequestPermissionRationaleMethod.invoke(activity, perm)) {
                    needRequestPermissionList.add(perm);
                }
            }
        } catch (Throwable e) {

        }
        return needRequestPermissionList;
    }

    public static void launchPermissionSettings(final Activity activity) {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.fromParts("package", activity.getPackageName(), null));
        activity.startActivity(intent);
    }

    public static boolean verifyPermissions(int[] grantResults) {
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public static void showMissingPermissionDialog(final Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("\n" +
                "프롬프트");
        builder.setMessage("w");

        // 거부, 앱 종료
        builder.setNegativeButton("취소",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        activity.finish();
                    }
                });

        builder.setPositiveButton("설정",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        launchPermissionSettings(activity);
                    }
                });
        builder.setCancelable(false);
        builder.show();
    }

}
