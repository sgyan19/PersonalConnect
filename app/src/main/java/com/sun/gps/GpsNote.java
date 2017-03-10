package com.sun.gps;

/**
 * Created by guoyao on 2017/3/8.
 */
public class GpsNote {
    private long id;
    private String userName;
    private int userId;
    private GpsGear gpsGear;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public GpsGear getGpsGear() {
        return gpsGear;
    }

    public void setGpsGear(GpsGear gpsGear) {
        this.gpsGear = gpsGear;
    }
}
