package com.sun.gps;

import android.location.Location;

import com.sun.common.SessionNote;

/**
 * Created by guoyao on 2017/2/16.
 */
public class GpsResponse extends SessionNote{
    public static final int ERR_TYPE_NONE = 0;
    public static final int ERR_TYPE_PERMISSION = 1;
    public static final int ERR_TYPE_DEVICE = 2;
    private long time;
    private double longitude;
    private double latitude;
    private double altitude;
    private long elapsedRealtimeNanos;
    private float speed;
    private float bearing;
    private float accuracy;
    private String errInfo;
    private int errType = ERR_TYPE_NONE;
    private String device;
    private String provider;
    private String debugMsg;

    public GpsResponse(){
        mGpsNote = new GpsNote();
    }

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

    public long getElapsedRealtimeNanos() {
        return elapsedRealtimeNanos;
    }

    public void setElapsedRealtimeNanos(long elapsedRealtimeNanos) {
        this.elapsedRealtimeNanos = elapsedRealtimeNanos;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public float getBearing() {
        return bearing;
    }

    public void setBearing(float bearing) {
        this.bearing = bearing;
    }

    public float getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(float accuracy) {
        this.accuracy = accuracy;
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

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getDebugMsg() {
        return debugMsg;
    }

    public void setDebugMsg(String debugMsg) {
        this.debugMsg = debugMsg;
    }

    public void setLocation(Location l){
        time = l.getTime();
        elapsedRealtimeNanos = l.getElapsedRealtimeNanos();
        latitude = l.getLatitude();
        longitude = l.getLongitude();
        altitude = l.getAltitude();
        speed = l.getSpeed();
        bearing = l.getBearing();
        accuracy = l.getAccuracy();
        provider = l.getProvider();
    }

    public Location getLocation(){
        Location l = new Location(provider);
        l.setLongitude(longitude);
        l.setLatitude(latitude);
        l.setAltitude(altitude);
        l.setAccuracy(accuracy);
        l.setSpeed(speed);
        l.setBearing(bearing);
        l.setTime(time);
        l.setElapsedRealtimeNanos(elapsedRealtimeNanos);
        return l;
    }

    GpsNote mGpsNote;

    public long getId() {
        return mGpsNote.getId();
    }

    public void setId(long id) {
        mGpsNote.setId(id);
    }

    public String getUserName() {
        return mGpsNote.getUserName();
    }

    public void setUserName(String userName) {
        mGpsNote.setUserName(userName);
    }

    public int getUserId() {
        return mGpsNote.getUserId();
    }

    public void setUserId(int userId) {
        mGpsNote.setUserId( userId );
    }

    public GpsGear getGpsGear() {
        return mGpsNote.getGpsGear();
    }

    public void setGpsGear(GpsGear gpsGear) {
        mGpsNote.setGpsGear(gpsGear);
    }
}
