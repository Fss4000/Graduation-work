package com.example.alt_beacon.location;

import com.example.alt_beacon.location.rendering.LocationNode;
import com.example.alt_beacon.location.rendering.LocationNodeRender;
import com.google.ar.sceneform.Node;

public class LocationMarker {

    public double longitude;
    public double latitude;

    // AR의 위치
    public LocationNode anchorNode;

    // 렌더 노드
    public Node node;

    // AR에서 null 렌더 이벤트가 아닌 경우 각 프레임에서 호출
    private LocationNodeRender renderEvent;
    private float scaleModifier = 1F;
    private float height = 0F;
    private int onlyRenderWhenWithin = Integer.MAX_VALUE;
    private ScalingMode scalingMode = ScalingMode.FIXED_SIZE_ON_SCREEN;
    private float gradualScalingMinScale = 0.8F;
    private float gradualScalingMaxScale = 1.4F;

    public LocationMarker(double longitude, double latitude, Node node) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.node = node;
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

    /**
     * onlyRenderWhenWithin] 미터 내에있을 때만이 마커를 렌더링
     *
     * @return - metres or -1
     */
    public int getOnlyRenderWhenWithin() {
        return onlyRenderWhenWithin;
    }

    /**
     * [onlyRenderWhenWithin] 미터 내에있을 때만이 마커를 렌더링
     *
     * @param onlyRenderWhenWithin - 미터
     */
    public void setOnlyRenderWhenWithin(int onlyRenderWhenWithin) {
        this.onlyRenderWhenWithin = onlyRenderWhenWithin;
    }

    /**
     * 카메라 높이를 기준으로 한 높이
     *
     * @return - 높이 기준은 미터
     */
    public float getHeight() {
        return height;
    }

    /**
     * 카메라 높이를 기준으로 한 높이
     *
     * @param height - 높이 기준은 미터
     */
    public void setHeight(float height) {
        this.height = height;
    }

    /**
     * 마커의 스케일 조정 방법
     *
     * @return - ScalingMode
     */
    public ScalingMode getScalingMode() {
        return scalingMode;
    }

    /**
     * 거리에 관계없이 마커가 스케일링되어야하는지 여부
     *
     * @param scalingMode - ScalingMode.X
     */
    public void setScalingMode(ScalingMode scalingMode) {
        this.scalingMode = scalingMode;
    }

    /**
     * 스케일 배율기
     *
     * @return - multiplier
     */
    public float getScaleModifier() {
        return scaleModifier;
    }

    /**
     * 스케일 배율기
     *
     * @param scaleModifier - multiplier
     */
    public void setScaleModifier(float scaleModifier) {
        this.scaleModifier = scaleModifier;
    }

    /**
     * 각 프레임에서 호출
     *
     * @return - LocationNodeRender (event)
     */
    public LocationNodeRender getRenderEvent() {
        return renderEvent;
    }

    /**
     * 각 프레임에서 호출
     */
    public void setRenderEvent(LocationNodeRender renderEvent) {
        this.renderEvent = renderEvent;
    }

    public enum ScalingMode {
        FIXED_SIZE_ON_SCREEN,
        NO_SCALING,
        GRADUAL_TO_MAX_RENDER_DISTANCE
    }

}
