package com.example .alt_beacon;

public class MyBeacon {


    private String address;
    private int rssi;
    private int txPower;
    private String now;
    private double[]location;
    private double distance;

    public MyBeacon(String address, int rssi, int txPower, String now, double[] location, double distance) {
        this.address = address;
        this.rssi = rssi;
        this.txPower = txPower;
        this.now = now;
        this.location = location;
        this.distance = distance;
    }

    public String getAddress() {
        return address;
    }

    public int getRssi() {
        return rssi;
    }

    public int getTxPower() {
        return txPower;
    }

    public String getNow() {
        return now;
    }

    public double[] getLocation(){
        return location;
    }

    public double getDistance(){
        return distance;
    }
}

