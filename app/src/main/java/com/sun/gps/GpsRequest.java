package com.sun.gps;

import com.sun.common.SessionNote;

/**
 * Created by guoyao on 2017/3/8.
 */
public class GpsRequest extends SessionNote{
    GpsNote mGpsNote;

    public GpsRequest(){
        mGpsNote = new GpsNote();
    }

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
