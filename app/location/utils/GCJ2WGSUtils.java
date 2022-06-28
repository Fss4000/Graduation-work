package com.example.alt_beacon.location.utils;

public class GCJ2WGSUtils {

    //WGS 위도에 GCJ 위도 및 경도 입력
    public static double WGSLat (double lat,double lon) {
        double PI = 3.14159265358979324;//파이
        double a = 6378245.0;//Krasovski 타원체 파라미터 긴 반축 a
        double ee = 0.00669342162296594323;//Klassowski 타원체 매개 변수 첫 편심 제곱
        double dLat = transformLat(lon - 105.0, lat - 35.0);
        double radLat = lat / 180.0 * PI;
        double magic = Math.sin(radLat);
        magic = 1 - ee * magic * magic;
        double sqrtMagic = Math.sqrt(magic);
        dLat = (dLat * 180.0) / ((a * (1 - ee)) / (magic * sqrtMagic) * PI);
        return (lat - dLat);
    }

    //WC 경도에 GCJ 위도 및 경도 입력
    public static double WGSLon (double lat,double lon) {
        double PI = 3.14159265358979324;//파이
        double a = 6378245.0;//Krasovski 타원체 파라미터 긴 반축 a
        double ee = 0.00669342162296594323;//Klassowski 타원체 매개 변수 첫 편심 제곱
        double dLon = transformLon(lon - 105.0, lat - 35.0);
        double radLat = lat / 180.0 * PI;
        double magic = Math.sin(radLat);
        magic = 1 - ee * magic * magic;
        double sqrtMagic = Math.sqrt(magic);
        dLon = (dLon * 180.0) / (a / sqrtMagic * Math.cos(radLat) * PI);
        return (lon - dLon);
    }

    //경도를 변환하는 데 필요
    public static double transformLon(double x, double y) {
        double PI = 3.14159265358979324;//파이
        double ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1 * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * PI) + 20.0 * Math.sin(2.0 * x * PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(x * PI) + 40.0 * Math.sin(x / 3.0 * PI)) * 2.0 / 3.0;
        ret += (150.0 * Math.sin(x / 12.0 * PI) + 300.0 * Math.sin(x / 30.0 * PI)) * 2.0 / 3.0;
        return ret;
    }
    //위도를 변환하는 데 필요
    public static double transformLat(double x, double y) {
        double PI = 3.14159265358979324;//파이
        double ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y + 0.2 * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * PI) + 20.0 * Math.sin(2.0 * x * PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(y * PI) + 40.0 * Math.sin(y / 3.0 * PI)) * 2.0 / 3.0;
        ret += (160.0 * Math.sin(y / 12.0 * PI) + 320 * Math.sin(y * PI / 30.0)) * 2.0 / 3.0;
        return ret;
    }

}