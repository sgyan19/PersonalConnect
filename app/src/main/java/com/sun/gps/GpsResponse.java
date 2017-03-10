package com.sun.gps;

/**
 * Created by guoyao on 2017/2/16.
 */
public class GpsResponse extends GpsNote{
    public static final int ERR_TYPE_NONE = 0;
    public static final int ERR_TYPE_PERMISSION = 1;
    public static final int ERR_TYPE_DEVICE = 2;
    private long time;
    private double longitude;
    private double latitude;
    private double altitude;
    private String errInfo;
    private int errType = ERR_TYPE_NONE;
    private String device;

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public String getErrInfo() {
        return errInfo;
    }

    public void setErrInfo(String errInfo) {
        this.errInfo = errInfo;
    }

    public int getErrType() {
        return errType;
    }

    public void setErrType(int errType) {
        this.errType = errType;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }
}
