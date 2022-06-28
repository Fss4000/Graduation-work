package com.example.alt_beacon.location.sensor;

import android.content.Context;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.DPoint;
import com.example.alt_beacon.location.utils.GCJ2WGSUtils;


public class LocationManager {
    public Context mContext;
    public AMapLocationClient mLocationClient;
    public AMapLocationListener mLocationListener;
    public DPoint currentLocation=null;
    public AMapLocation currentAmapLocation=null;
    public LocationManager(Context context){
        this.mContext = context;
        //AMapLocationClient 클래스 객체 선언
        mLocationClient = null;
        //초기 위치
        mLocationClient = new AMapLocationClient(mContext);
        //위치 결정 콜백 작업 설정
        mLocationListener = new AMapLocationListener() {
            @Override
            public void onLocationChanged(AMapLocation amapLocation) {
                if (amapLocation != null) {
                    if (amapLocation.getErrorCode() == 0) {
                        currentAmapLocation=amapLocation;
                        double gcjLat=currentAmapLocation.getLatitude();
                        double gcjLon=currentAmapLocation.getLongitude();
                        double wgsLat= GCJ2WGSUtils.WGSLat(gcjLat,gcjLon);
                        double wgsLon=GCJ2WGSUtils.WGSLon(gcjLat,gcjLon);
                        currentLocation=new DPoint(wgsLat,wgsLon);
                    }
                    else{
                        //오류 메시지
                    }
                }
                else{
                    //빈 반품
                }
            }
        };
        //포지셔닝 콜백 리스너 설정
        mLocationClient.setLocationListener(mLocationListener);
        //포지셔닝 시작
        mLocationClient.startLocation();
        //비동기 적으로 위치 결정 결과 얻기
    }
}


