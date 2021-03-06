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
        // ??????????????? getUpdate ()??? ????????? ?????? ??? ?????? ??????????????? getScene ()??? null??? ???????????? ??????
        // ????????? onUpdate??? ?????? ????????? ??????????????? ????????? ????????? ?????? ??? ??????
        // onUpdate ?????? ?????? ?????????, getScene??? null ??? ??? ??????
        for (Node n : getChildren()) {
            if (getScene() == null) {
                return;
            }

            Vector3 cameraPosition = getScene().getCamera().getWorldPosition();
            Vector3 nodePosition = n.getWorldPosition();

            // ???????????? ?????? ?????? ?????? ?????? ?????? ???????????? ?????? ?????? ?????? ????????? ??????
            float dx = cameraPosition.x - nodePosition.x;
            float dy = cameraPosition.y - nodePosition.y;
            float dz = cameraPosition.z - nodePosition.z;


            // ?????? ?????? ?????? InAR??? AR?????? ?????? ????????? ??????
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
            //?????? ??? ???????????? ????????? ??????
            int renderDistance = markerDistance;
            if (renderDistance > locationScene.getDistanceLimit())
                renderDistance = locationScene.getDistanceLimit();

            float scale = 1F;

            switch (scalingMode) {
                // ????????? ???????????? ????????? ???????????? ????????? ????????? ??????
                case FIXED_SIZE_ON_SCREEN:
                    scale = 0.5F * (float) renderDistance;
                    // ??? ????????? ??????
                    if (markerDistance > 3000)
                        scale *= 0.75F;
                    break;
                // ????????? ???????????????
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
            // ?????? ??????
            n.setWorldPosition(new Vector3(n.getWorldPosition().x, getHeight(), n.getWorldPosition().z));
            Vector3 direction = Vector3.subtract(cameraPosition, nodePosition);
            Quaternion lookRotation = Quaternion.lookRotation(direction, Vector3.up());
            //?????? ??????
            n.setWorldRotation(lookRotation);
            // ?????? ??????
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
