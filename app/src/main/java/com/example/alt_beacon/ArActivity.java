package com.example.alt_beacon;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;


import com.example.alt_beacon.location.LocationMarker;
import com.example.alt_beacon.location.LocationScene;
import com.example.alt_beacon.location.rendering.LocationNode;
import com.example.alt_beacon.location.rendering.LocationNodeRender;
import com.google.ar.core.Frame;
import com.google.ar.core.Plane;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.UnavailableException;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.HitTestResult;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ViewRenderable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.BiFunction;


public class ArActivity extends AppCompatActivity  {

    private boolean installRequested;
    private boolean hasFinishedLoadingRenderable = false;
    private boolean hasFinishedSetRenderable = false;

    private Snackbar loadingMessageSnackbar = null;
    private ArSceneView arSceneView;
    // 렌더링
    private ViewRenderable renderable3;
    private ViewRenderable renderable2;
    private ViewRenderable renderable1;

    //private SensorManager mSensorManager;


    // Our ARCore-Location scene
    private LocationScene locationScene;
    private TextView locationText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        Intent intent=getIntent();
        double mylot=intent.getDoubleExtra("longitude",0.0);
        double mylat=intent.getDoubleExtra("latitude",0.0);

        double targetlot=intent.getDoubleExtra("beaconx",0.0);
        double targetlat=intent.getDoubleExtra("beacony",0.0);



        if (!Utils.checkIsSupportedDeviceOrFinish(this)) {
            // 디바이스가 지원하지 않음
            return;
        }
        Utils.checkPermissions(this);

        setContentView(R.layout.activity_ar);
        arSceneView = findViewById(R.id.ar_scene_view);
        locationText = findViewById(R.id.tv_location);

        // 2D로 부터 랜더링 빌드
        final CompletableFuture<ViewRenderable> CCNUViewLayout =
                ViewRenderable.builder()
                        .setView(getApplicationContext(), R.layout.card)
                        .build();

        final CompletableFuture<ViewRenderable> WHUViewLayout =
                ViewRenderable.builder()
                        .setView(getApplicationContext(), R.layout.card)
                        .build();
        final CompletableFuture<ViewRenderable> MAKE3ViewLayout =
                ViewRenderable.builder()
                        .setView(getApplicationContext(), R.layout.card)
                        .build();

        CompletableFuture.allOf(
                CCNUViewLayout,
                WHUViewLayout,
                MAKE3ViewLayout)
                .handle(
                        new BiFunction<Void, Throwable, Object>() {
                            @Override
                            public Object apply(Void notUsed, Throwable throwable) {
                                // Renderable을 빌드하면 Sceneform은 백그라운드에서 리소스를 로드합니다
                                // CompletableFuture를 반환합니다. handle() ,thenAccept() 를 호출하거나 isDone() 을 확인하십시오.
                                // get() 을 호출하기 전에
                                if (throwable != null) {
                                    Utils.displayError(ArActivity.this, "랜더러들을 불러올 수 없습니다.", throwable);
                                    return null;
                                }
                                try {
                                    renderable3 = MAKE3ViewLayout.get();
                                    renderable2 = CCNUViewLayout.get();
                                    renderable1 = WHUViewLayout.get();
                                    hasFinishedLoadingRenderable = true;

                                } catch (InterruptedException | ExecutionException ex) {
                                    Utils.displayError(ArActivity.this, "랜더러들을 불러올 수 없습니다.", ex);
                                }
                                return null;
                            }
                        });

        arSceneView
                .getScene()
                .addOnUpdateListener(
                        frameTime -> {
                            //모델이로드 된 후 다음 작업이 수행
                            if (!hasFinishedLoadingRenderable) {
                                return;
                            }
                            //새로운 Inherited LocationScene 객체 생성
                            if (locationScene == null) {
                                locationScene = new LocationScene(this, this, arSceneView);
                                locationScene.setOffsetOverlapping(false);//겹치는 모델에 오프셋을 추가할지 여부를 설정합니다
                            }
                            //ArFrame 받기
                            Frame frame = arSceneView.getArFrame();
                            if (frame == null) {
                                return;
                            }
                            //프레임이 추적 상태에 있으면 계속
                            if (frame.getCamera().getTrackingState() != TrackingState.TRACKING) {
                                return;
                            }
                            //locationScene이 비어 있지 않고 모델이 아직 배치되지 않은 경우
                            if(locationScene != null && !hasFinishedSetRenderable)
                            {
                                //현재 장치 위치를 얻은 경우
                                if(this.locationScene.locationManager.currentLocation!=null) {
                                    //마커2 식별하는 모델 만들기
                                    final LocationMarker Marker1 = new LocationMarker(
                                            targetlot,
                                            targetlat,
                                            getViewRenderable(renderable2)
                                    );
                                    //위치 정보를 표시하도록 사용자 지정 렌더링 이벤트 설정
                                    Marker1.setRenderEvent(new LocationNodeRender() {
                                        @Override
                                        public void render(LocationNode node) {
                                            View eView = renderable2.getView();
                                            TextView distanceTextView = eView.findViewById(R.id.tv_message);
                                            String renderInfo = "타겟 비콘"+"\n"
                                                    +"Longitude:"+Marker1.longitude+"\n"
                                                    +"Latitude:"+Marker1.latitude+"\n";
                                                    //+node.getDistanceInGPS() + "M";//모델과 장치 사이의 거리를 표시
                                            distanceTextView.setText(renderInfo);
                                        }
                                    });
                                    //locationScene에 모델 추가
                                    locationScene.mLocationMarkers.add(Marker1);

                                    //식별 모델 만들기
                                    /*LocationMarker Marker2 = new LocationMarker(
                                            127.1387567,
                                            37.534127,
                                            getViewRenderable(renderable1)
                                    );
                                    Marker2.setRenderEvent(new LocationNodeRender() {
                                        @Override
                                        public void render(LocationNode node) {
                                            View eView = renderable1.getView();
                                            TextView distanceTextView = eView.findViewById(R.id.tv_message);

                                            String renderInfo = "마커 2           "+"\n"
                                                    +"Longitude:"+Marker2.longitude+"\n"
                                                    +"Latitude:"+Marker2.latitude+"\n"
                                                    +node.getDistanceInGPS() + "M";
                                            distanceTextView.setText(renderInfo);
                                        }
                                    });
                                    locationScene.mLocationMarkers.add(Marker2);


                                    LocationMarker Marker3 = new LocationMarker(
                                            127.138858,
                                            37.534083,
                                            getViewRenderable(renderable1)
                                    );
                                    Marker3.setRenderEvent(new LocationNodeRender() {
                                        @Override
                                        public void render(LocationNode node) {
                                            View eView = renderable3.getView();
                                            TextView distanceTextView = eView.findViewById(R.id.tv_message);

                                            String renderInfo = "마커 3           "+"\n"
                                                    +"Longitude:"+Marker3.longitude+"\n"
                                                    +"Latitude:"+Marker3.latitude+"\n"
                                                    +node.getDistanceInGPS() + "M";
                                            distanceTextView.setText(renderInfo);
                                        }
                                    });
                                    locationScene.mLocationMarkers.add(Marker3);*/

                                    //모델이 배치되었습니다
                                    hasFinishedSetRenderable = true;
                                }
                            }

                            if (locationScene != null) {
                                locationScene.processFrame(frame);
                                if(this.locationScene.locationManager.currentLocation!=null)
                                {
                                    //gps값받아오기
                                    String deviceInfo = "경도 :"+mylot+"\n"
                                            +"위도 :"+mylat
                                            /*"WGS Longitude:"+this.locationScene.locationManager.currentLocation.getLongitude()+"\n"
                                            +"WGS Latitude:"+this.locationScene.locationManager.currentLocation.getLatitude()+"\n"
                                            +"AMap Longitude:"+this.locationScene.locationManager.currentAmapLocation.getLongitude()+"\n"
                                            +"AMap Latitude:"+this.locationScene.locationManager.currentAmapLocation.getLatitude()+"\n"
                                            +"Location Type:"+this.locationScene.locationManager.currentAmapLocation.getLocationType()+"\n"
                                            +"Accuracy:"+this.locationScene.locationManager.currentAmapLocation.getAccuracy()+"\n"
                                            +"Address:"+this.locationScene.locationManager.currentAmapLocation.getAddress()*/


                                            ;
                                    locationText.setText(deviceInfo);
                                }

                            }

                            if (loadingMessageSnackbar != null) {
                                for (Plane plane : frame.getUpdatedTrackables(Plane.class)) {
                                    if (plane.getTrackingState() == TrackingState.TRACKING) {
                                        hideLoadingMessage();//트래킹시 탐지 평면이 종료되지만 트래킹되지 않은 상태는 이미 모델을 표시 할 수 있음
                                    }
                                }
                            }


                        });
    }

    /**
     * Example node 레이아웃
     *
     * @return
     */
    private Node getViewRenderable(ViewRenderable renderable) {
        Node base = new Node();
        base.setRenderable(renderable);
        Context c = this;
        //여기에 리스너 추가
        View eView = renderable.getView();
        eView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Toast.makeText(
                        getApplicationContext(), "Location marker touched.", Toast.LENGTH_LONG)
                        .show();
                return false;
            }
        });
        return base;
    }

    /***
     * Example Node of a 3D 모델
     *
     * @return
     */
    private Node getModelRenderable(ModelRenderable renderable) {
        Node base = new Node();
        base.setRenderable(renderable);
        Context c = this;
        base.setOnTapListener(new Node.OnTapListener() {
            @Override
            public void onTap(HitTestResult v, MotionEvent event) {
                Toast.makeText(
                        getApplicationContext(), "Andy touched.", Toast.LENGTH_LONG)
                        .show();
            }
        });
        return base;
    }

    /**
     * Make sure we call locationScene.resume();
     */
    @Override
    protected void onResume() {
        super.onResume();

        if (locationScene != null) {
            locationScene.resume();
        }

        if (arSceneView.getSession() == null) {
            // 세션이 아직 생성되지 않은 경우 렌더링을 다시 시작하지 마시오.
            // ARCore를 업데이트해야하거나 권한이 아직 부여되지 않은 경우에 발생할 수 있음
            try
            {
                Session session = Utils.createArSession(this, installRequested);
                if (session == null)
                {
                    installRequested = true;
                    return;
                }
                else
                {
                    arSceneView.setupSession(session);
                }
            }
            catch (UnavailableException e)
            {
                Utils.handleSessionException(this, e);
            }
        }

        try {
            arSceneView.resume();
        } catch (CameraNotAvailableException ex) {
            Utils.displayError(this, "Unable to get camera", ex);
            finish();
            return;
        }

        if (arSceneView.getSession() != null) {
            //showLoadingMessage();
        }
    }

    /**
     * locationScene.pause () 호출해야합니다
     */
    @Override
    public void onPause() {
        super.onPause();

        if (locationScene != null) {
            locationScene.pause();
        }

        arSceneView.pause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        arSceneView.destroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String[] permissions, int[] paramArrayOfInt) {
        if (requestCode == Utils.PERMISSION_REQUESTCODE) {
            if (!Utils.verifyPermissions(paramArrayOfInt)) {
                finish();
            }
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            // 표준 Android 전체 화면 기능.
            getWindow()
                    .getDecorView()
                    .setSystemUiVisibility(
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    /*private void showLoadingMessage() {
        if (loadingMessageSnackbar != null && loadingMessageSnackbar.isShownOrQueued()) {
            return;
        }

        loadingMessageSnackbar =
                Snackbar.make(
                        ArActivity.this.findViewById(android.R.id.content),
                        "plane을 찾고 있음 !!!",
                        Snackbar.LENGTH_INDEFINITE);
        loadingMessageSnackbar.getView().setBackgroundColor(0xbf323232);
        loadingMessageSnackbar.show();
    }*/

    private void hideLoadingMessage() {
        if (loadingMessageSnackbar == null) {
            return;
        }

        loadingMessageSnackbar.dismiss();
        loadingMessageSnackbar = null;
    }
}
