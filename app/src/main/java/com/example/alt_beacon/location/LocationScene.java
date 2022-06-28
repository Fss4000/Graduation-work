package com.example.alt_beacon.location;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.util.Log;


import com.example.alt_beacon.location.rendering.LocationNode;
import com.example.alt_beacon.location.sensor.DeviceOrientation;
import com.example.alt_beacon.location.sensor.LocationManager;
import com.example.alt_beacon.location.utils.LocationUtils;
import com.google.ar.core.Anchor;
import com.google.ar.core.Frame;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.sceneform.ArSceneView;

import java.util.ArrayList;

public class LocationScene {

    private String TAG = "LocationScene";
    public ArSceneView mArSceneView;
    public LocationManager locationManager;
    public DeviceOrientation deviceOrientation;
    public Context mContext;
    public Activity mActivity;
    public ArrayList<LocationMarker> mLocationMarkers = new ArrayList<>();
    // 앵커는 현재 간격에 따라 다시 그려집니다
    private int anchorRefreshInterval = 1000 * 5; // 5 seconds
    // AR 장면 내에서 마커를 그릴 위치 제한.
    // 자동 확장되지만 렌더링 문제를 방지하는 데 도움이됩니다.
    private int distanceLimit = 20;
    private boolean offsetOverlapping = false;
    // 베어링 조정 : 진북으로 보정하도록 설정 가능
    private int bearingAdjustment = 0;
    private boolean anchorsNeedRefresh = true;
    private boolean minimalRefreshing = false;
    private boolean refreshAnchorsAsLocationChanges = false;
    private Handler mHandler = new Handler();
    Runnable anchorRefreshTask = new Runnable() {
        @Override
        public void run() {
            anchorsNeedRefresh = true;
            mHandler.postDelayed(anchorRefreshTask, anchorRefreshInterval);
        }
    };
    private boolean debugEnabled = false;
    private Session mSession;
    public LocationScene(Context mContext, Activity mActivity, ArSceneView mArSceneView) {
        Log.i(TAG, "Location Scene 초기화.");
        this.mContext = mContext;
        this.mActivity = mActivity;
        this.mSession = mArSceneView.getSession();
        this.mArSceneView = mArSceneView;

        startCalculationTask();

        locationManager = new LocationManager(mContext);
        deviceOrientation = new DeviceOrientation(this);
        deviceOrientation.resume();
    }

    public boolean isDebugEnabled() {
        return debugEnabled;
    }

    public void setDebugEnabled(boolean debugEnabled) {
        this.debugEnabled = debugEnabled;
    }

    public boolean minimalRefreshing() {
        return minimalRefreshing;
    }

    public void setMinimalRefreshing(boolean minimalRefreshing) {
        this.minimalRefreshing = minimalRefreshing;
    }

    public boolean refreshAnchorsAsLocationChanges() {
        return refreshAnchorsAsLocationChanges;
    }

    public void setRefreshAnchorsAsLocationChanges(boolean refreshAnchorsAsLocationChanges) {
        if (refreshAnchorsAsLocationChanges) {
            stopCalculationTask();
        } else {
            startCalculationTask();
        }
        refreshAnchors();
        this.refreshAnchorsAsLocationChanges = refreshAnchorsAsLocationChanges;
    }

    public int getAnchorRefreshInterval() {
        return anchorRefreshInterval;
    }

    /**
     * 앵커가 자동으로 다시 계산되는 간격을 설정하십시오.
     *
     * @param anchorRefreshInterval
     */
    public void setAnchorRefreshInterval(int anchorRefreshInterval) {
        this.anchorRefreshInterval = anchorRefreshInterval;
        stopCalculationTask();
        startCalculationTask();
    }

    public void clearMarkers() {
        for (LocationMarker lm : mLocationMarkers) {
            if (lm.anchorNode != null) {
                lm.anchorNode.getAnchor().detach();
                lm.anchorNode.setEnabled(false);
                lm.anchorNode = null;
            }
        }
        mLocationMarkers = new ArrayList<>();
    }

    /**
     * 먼 마커의 거리 제한입니다.
     *  ARCore는 2000km 떨어진 마커를 제한
     *
     * @return
     */
    public int getDistanceLimit() {
        return distanceLimit;
    }

    /**
     * 먼 마커의 거리 제한.
     * ARCore는 2000km 떨어진 마커를 제한/
     * 기본 20
     * **/
    public void setDistanceLimit(int distanceLimit) {
        this.distanceLimit = distanceLimit;
    }

    public boolean shouldOffsetOverlapping() {
        return offsetOverlapping;
    }

    /**
     * 마커가 겹쳐지면 세로로 올리도록함.
     *
     * @param offsetOverlapping
     */
    public void setOffsetOverlapping(boolean offsetOverlapping) {
        this.offsetOverlapping = offsetOverlapping;
    }

    public void processFrame(Frame frame) {
        refreshAnchorsIfRequired(frame);
    }

    /**
     * 앵커를 다시 계산하도록 강제
     */
    public void refreshAnchors() {
        anchorsNeedRefresh = true;
    }

    private void refreshAnchorsIfRequired(Frame frame) {
        if (anchorsNeedRefresh) {
            Log.i(TAG, "Refreshing anchors...");
            anchorsNeedRefresh = false;

            if (locationManager.currentLocation == null) {
                Log.i(TAG, "아직 위치가 잡히지 않음.");
                return;
            }

            for (int i = 0; i < mLocationMarkers.size(); i++) {
                try {

                    int markerDistance = (int) Math.round(
                            LocationUtils.distance(
                                    mLocationMarkers.get(i).latitude,
                                    locationManager.currentLocation.getLatitude(),
                                    mLocationMarkers.get(i).longitude,
                                    locationManager.currentLocation.getLongitude())
                    );

                    if (markerDistance > mLocationMarkers.get(i).getOnlyRenderWhenWithin()) {

                        // 해당 부분이 설정 되어있을경우, 너무 멀리 있으면 렌더링하지 않습니다.
                        Log.i(TAG, "Not rendering. Marker distance: " + markerDistance + " Max render distance: " + mLocationMarkers.get(i).getOnlyRenderWhenWithin());
                        continue;
                    }

                    float markerBearing = deviceOrientation.currentDegree + (float) LocationUtils.bearing(
                            locationManager.currentLocation.getLatitude(),
                            locationManager.currentLocation.getLongitude(),
                            mLocationMarkers.get(i).latitude,
                            mLocationMarkers.get(i).longitude);


                    // 베어링 조정을 설정
                    // 북쪽 방향을 수정 -setBearingAdjustment (10)
                    markerBearing = markerBearing + bearingAdjustment;
                    markerBearing = markerBearing % 360;

                    double rotation = Math.floor(markerBearing);



                    // 기기를 위쪽으로 향한 경우 (카메라가 하늘을 향함)
                    // 나침반 베어링이 뒤집어 질 수 있음
                    // pitch에서 발생하는거 같음 ~ = -25
                    if (deviceOrientation.pitch > -25)
                        rotation = rotation * Math.PI / 180;

                    int renderDistance = markerDistance;


                    // 장면 내에서 앵커 거리를 제한
                    // 렌더링 문제를 방지
                    if (renderDistance > distanceLimit)
                        renderDistance = distanceLimit;


                    // 카메라 바로 앞에있는 대신 수평선에 마커를 추가하도록 조정
                    double heightAdjustment = 0;
                    // Math.round(renderDistance * (Math.tan(Math.toRadians(deviceOrientation.pitch)))) - 1.5F;

                    // 거리의 착시를 높이기 위해 먼 거리의 마커를 올림
                    //  Hacky-임시 측정 값으로 작동
                    int cappedRealDistance = markerDistance > 500 ? 500 : markerDistance;
                    if (renderDistance != markerDistance)
                        heightAdjustment += 0.005F * (cappedRealDistance - renderDistance);

                    float x = 0;
                    float z = -renderDistance;

                    float zRotated = (float) (z * Math.cos(rotation) - x * Math.sin(rotation));
                    float xRotated = (float) -(z * Math.sin(rotation) + x * Math.cos(rotation));


                    // 현재 카메라 높이
                    float y = frame.getCamera().getDisplayOrientedPose().ty();

                    if (mLocationMarkers.get(i).anchorNode != null &&
                            mLocationMarkers.get(i).anchorNode.getAnchor() != null) {
                        mLocationMarkers.get(i).anchorNode.getAnchor().detach();
                        mLocationMarkers.get(i).anchorNode.setAnchor(null);
                        mLocationMarkers.get(i).anchorNode.setEnabled(false);
                        mLocationMarkers.get(i).anchorNode = null;
                    }


                    // 예외 발생시 새로 생성 된 앵커를 즉시 할당하지 않음
                    Anchor newAnchor = mSession.createAnchor(
                            frame.getCamera().getPose()
                                    .compose(Pose.makeTranslation(xRotated, y + (float) heightAdjustment, zRotated)));


                    mLocationMarkers.get(i).anchorNode = new LocationNode(newAnchor, mLocationMarkers.get(i), this);
                    mLocationMarkers.get(i).anchorNode.setParent(mArSceneView.getScene());
                    mLocationMarkers.get(i).anchorNode.addChild(mLocationMarkers.get(i).node);

                    if (mLocationMarkers.get(i).getRenderEvent() != null) {
                        mLocationMarkers.get(i).anchorNode.setRenderEvent(mLocationMarkers.get(i).getRenderEvent());
                    }

                    mLocationMarkers.get(i).anchorNode.setScaleModifier(mLocationMarkers.get(i).getScaleModifier());
                    mLocationMarkers.get(i).anchorNode.setScalingMode(mLocationMarkers.get(i).getScalingMode());
                    mLocationMarkers.get(i).anchorNode.setGradualScalingMaxScale(mLocationMarkers.get(i).getGradualScalingMaxScale());
                    mLocationMarkers.get(i).anchorNode.setGradualScalingMinScale(mLocationMarkers.get(i).getGradualScalingMinScale());
                    mLocationMarkers.get(i).anchorNode.setHeight(mLocationMarkers.get(i).getHeight());

                    if (minimalRefreshing)
                        mLocationMarkers.get(i).anchorNode.scaleAndRotate();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            System.gc();
        }
    }

    /**
     * 나침반 베어링 조정
     *
     * @return
     */
    public int getBearingAdjustment() {
        return bearingAdjustment;
    }

    /**
     * 나침반 베어링 조정.
     * 정밀도를 향상시키는 맞춤형 방법으로 사용할 수 있습니다.
     * @param i
     */
    public void setBearingAdjustment(int i) {
        bearingAdjustment = i;
        anchorsNeedRefresh = true;
    }

    /**
     * 센서 서비스를 다시 시작
     */
    public void resume() {
        deviceOrientation.resume();
    }

    /**
     * 센서 서비스 일시 중지
     */
    public void pause() {
        deviceOrientation.pause();
    }

    void startCalculationTask() {
        anchorRefreshTask.run();
    }

    void stopCalculationTask() {
        mHandler.removeCallbacks(anchorRefreshTask);
    }
}
