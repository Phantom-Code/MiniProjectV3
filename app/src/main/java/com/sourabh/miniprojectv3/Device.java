package com.sourabh.miniprojectv3;

import com.google.firebase.database.Exclude;

public class Device {


    private String lastUpdated;
    private double latitude;
    private double longitude;
    private String deviceName;
    private String deviceId;
    private String batteryStatus;
    private String updateFrequency;



    public String getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getBatteryStatus() {
        return batteryStatus;
    }

    public void setBatteryStatus(String batteryStatus) {
        this.batteryStatus = batteryStatus;
    }

    public String getUpdateFrequency() {
        return updateFrequency;
    }

    public void setUpdateFrequency(String updateFrequency) {
        this.updateFrequency = updateFrequency;
    }

    public Device(String lastUpdated, double latitude, double longitude, String deviceName, String deviceId, String batteryStatus, String updateFrequency) {
        this.lastUpdated = lastUpdated;
        this.latitude = latitude;
        this.longitude = longitude;
        this.deviceName = deviceName;
        this.deviceId = deviceId;
        this.batteryStatus = batteryStatus;
        this.updateFrequency = updateFrequency;
    }

    @Exclude
    public boolean equals(Device obj) {
        boolean b = true;
        if(!this.lastUpdated.equals(obj.getLastUpdated())){
            b = false;
        }

        if(! (this.latitude == obj.getLatitude())){
            b = false;
        }

        if(!(this.longitude == obj.getLongitude())){
            b = false;
        }

        if(!this.deviceName.equals(obj.getDeviceName())){
            b = false;
        }

        if(!this.deviceId.equals(obj.getDeviceId())){
            b = false;
        }

        if(!this.batteryStatus.equals(obj.getBatteryStatus())){
            b = false;
        }

        if(!this.getUpdateFrequency().equals(obj.getUpdateFrequency())){
            b = false;
        }
        return b;
    }

    public Device(){}
}
