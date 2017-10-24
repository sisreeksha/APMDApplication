package com.tenpitech.apmd.apmdsdk.bean;

public class BLEDeviceObject {
    private String deviceAddress;
    private String deviceName;

    public BLEDeviceObject(String deviceName, String deviceAddress){
        this.deviceName = deviceName;
        this.deviceAddress = deviceAddress;
    }

    public String getDeviceAddress() {
        return deviceAddress;
    }

    public void setDeviceAddress(String deviceAddress) {
        this.deviceAddress = deviceAddress;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    @Override
    public boolean equals(Object obj) {
        boolean isEqual = false;
        BLEDeviceObject bleDeviceObject = (BLEDeviceObject) obj;
        if(bleDeviceObject.getDeviceAddress().equals(deviceAddress))
            if(bleDeviceObject.getDeviceName().equals(deviceName))
                isEqual = true;
        return isEqual;
    }
}
