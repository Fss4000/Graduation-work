package com.example.alt_beacon.location.rendering;

import com.example.alt_beacon.location.LocationMarker;
import com.example.alt_beacon.location.LocationScene;
import com.example.alt_beacon.location.utils.LocationUtils;
import com.google.ar.core.Anchor;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;

public class LocationNode extends AnchorNode {

    private String TAG = "LocationNode";

    private LocationMarker locationMarker;
    private LocationNodeRender renderEvent;
    private int distanceInGPS;
    private double distanceInAR;
    private float scaleModifier = 1F;
    private float height = 0F;
    private float gradualScalingMinScale = 0.8F;
    private float gradualScalingMaxScale = 1.4F;

    private LocationMarker.ScalingMode scalingMode = LocationMarker.ScalingMode.FIXED_SIZE_ON_SCREEN;
    private LocationScene locationScene;

    public LocationNode(Anchor anchor, LocationMarker locationMarker, LocationScene locationScene) {
        super(anchor);
        this.locationMarker = locationMarker;
        this.locationScene = locationScene;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public float getScaleModifier() {
        return scaleModifier;
    }

    public void setScaleModifier(float scaleModifier) {
        this.scaleModifier = scaleModifier;
    }

    public LocationNodeRender getRenderEvent() {
        return renderEvent;
    }

    public void setRenderEvent(LocationNodeRender renderEvent) {
        this.renderEvent = renderEvent;
    }

    public int getDistanceInGPS() {
        return distanceInGPS;
    }

    public double getDistanceInAR() {
        return distanceInAR;
    }

    public void setDistanceInGPS(int distanceInGPS) {
        this.distanceInGPS = distanceInGPS;
    }

    public void setDistanceInAR(double distanceInAR) {
        this.distanceInAR = distanceInAR;
    }

    public LocationMarker.ScalingMode getScalingMode() {
        return scalingMode;
    }

    public void setScalingMode(LocationMarker.ScalingMode scalingMode) {
        this.scalingMode = scalingMode;
    }

    @Override
    public void onUpdate(FrameTime frameTime) {
        // 일반적으로 getUpdate ()는 노드가 호출 될 때만 호출되므로 getScene ()은 null을 반환하지 않음
        // 그러나 onUpdate가 명시 적으로 호출되거나 노드가 씬에서 제거 된 경우
        // onUpdate 동안 다른 스레드, getScene이 null 일 수 있음
        for (Node n : getChildren()) {
            if (getScene() == null) {
                return;
            }

            Vector3 cameraPosition = getScene().getCamera().getWorldPosition();
            Vector3 nodePosition = n.getWorldPosition();

            // 카메라와 앵커 간의 차이 벡터 계산 카메라와 앵커 간의 좌표 거리를 계산
            float dx = cameraPosition.x - nodePosition.x;
            float dy = cameraPosition.y - nodePosition.y;
            float dz = cameraPosition.z - nodePosition.z;


            // 직선 거리 계산 InAR은 AR에서 직선 거리를 계산
            setDistanceInAR(Math.sqrt(dx * dx + dy * dy + dz * dz));

            if (locationScene.shouldOffsetOverlapping()) {
                if (locationScene.mArSceneView.getScene().overlapTestAll(n).size() > 0) {
                    setHeight(getHeight() + 1.2F);
                }
            }
        }

        if(!locationScene.minimalRefreshing())
            scaleAndRotate();

        if (renderEvent != null) {
            if(this.isTracking() && this.isActive() && this.isEnabled())
                renderEvent.render(this);
        }

    }

    public void scaleAndRotate() {

        for (Node n : getChildren()) {
            int markerDistance = (int) Math.ceil(
                    LocationUtils.distance(
                            locationMarker.latitude,
                            locationScene.locationManager.currentLocation.getLatitude(),
                            locationMarker.longitude,
                            locationScene.locationManager.currentLocation.getLongitude())
            );

            setDistanceInGPS(markerDistance);
            //장면 내 앵커에서 거리를 표시
            int renderDistance = markerDistance;
            if (renderDistance > locationScene.getDistanceLimit())
                renderDistance = locationScene.getDistanceLimit();

            float scale = 1F;

            switch (scalingMode) {
                // 거리에 관계없이 마커가 화면에서 동일한 크기로 유지
                case FIXED_SIZE_ON_SCREEN:
                    scale = 0.5F * (float) renderDistance;
                    // 먼 마커는 작음
                    if (markerDistance > 3000)
                        scale *= 0.75F;
                    break;
                // 모델이 커지고있음
                case GRADUAL_TO_MAX_RENDER_DISTANCE:
                    float scaleDifference = gradualScalingMaxScale - gradualScalingMinScale;
                    scale = (gradualScalingMinScale + ((locationScene.getDistanceLimit() - markerDistance) * (scaleDifference / locationScene.getDistanceLimit()))) * renderDistance;
                    break;
                case NO_SCALING:
                    break;
            }

            scale *= scaleModifier;

            Vector3 cameraPosition = getScene().getCamera().getWorldPosition();
            Vector3 nodePosition = n.getWorldPosition();
            // 위치 설정
            n.setWorldPosition(new Vector3(n.getWorldPosition().x, getHeight(), n.getWorldPosition().z));
            Vector3 direction = Vector3.subtract(cameraPosition, nodePosition);
            Quaternion lookRotation = Quaternion.lookRotation(direction, Vector3.up());
            //방향 설정
            n.setWorldRotation(lookRotation);
            // 크기 설정
            n.setWorldScale(new Vector3(scale, scale, scale));

        }
    }

    public float getGradualScalingMinScale() {
        return gradualScalingMinScale;
    }

    public void setGradualScalingMinScale(float gradualScalingMinScale) {
        this.gradualScalingMinScale = gradualScalingMinScale;
    }

    public float getGradualScalingMaxScale() {
        return gradualScalingMaxScale;
    }

    public void setGradualScalingMaxScale(float gradualScalingMaxScale) {
        this.gradualScalingMaxScale = gradualScalingMaxScale;
    }
}
