package com.tenpitech.apmd.apmdsdk.bean;

import android.location.Location;

public class ServerCallObject {
    private BLEDeviceObject bleDeviceObject;
    private CurrentLocation location;
    private SensorObject sensorObject;
    private String date;

    public BLEDeviceObject getBleDeviceObject() {
        return bleDeviceObject;
    }

    public void setBleDeviceObject(BLEDeviceObject bleDeviceObject) {
        this.bleDeviceObject = bleDeviceObject;
    }

    public CurrentLocation getLocation() {
        return location;
    }

    public void setLocation(CurrentLocation location) {
        this.location = location;
    }

    public SensorObject getSensorObject() {
        return sensorObject;
    }

    public void setSensorObject(SensorObject sensorObject) {
        this.sensorObject = sensorObject;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
