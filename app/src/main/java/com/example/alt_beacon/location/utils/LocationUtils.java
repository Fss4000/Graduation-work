package com.example.alt_beacon.location.utils;

public class LocationUtils {

    // 위도 및 경도를 입력하여 방향 각도를 계산
    public static double bearing(double lat1, double lon1, double lat2, double lon2){
        double longitude1 = lon1;
        double longitude2 = lon2;
        double latitude1 = Math.toRadians(lat1);
        double latitude2 = Math.toRadians(lat2);
        double longDiff = Math.toRadians(longitude2-longitude1);
        double y = Math.sin(longDiff)*Math.cos(latitude2);
        double x = Math.cos(latitude1)*Math.sin(latitude2)-Math.sin(latitude1)*Math.cos(latitude2)*Math.cos(longDiff);
        return (Math.toDegrees(Math.atan2(y, x))+360)%360;
    }

    // 위도 및 경도를 입력하여 거리를 계산
    public static double distance(double lat1, double lat2, double lon1,
                                  double lon2) {
        final int R = 6371; // 지구 반경
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // 미터로 변환 된 단위
        distance = Math.pow(distance, 2);
        return Math.sqrt(distance);
    }

}
