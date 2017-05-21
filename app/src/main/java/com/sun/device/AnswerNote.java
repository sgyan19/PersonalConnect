package com.sun.device;

import com.sun.common.SessionNote;

import java.util.Locale;

/**
 * Created by guoyao on 2017/4/21.
 */
public class AnswerNote extends SessionNote {
    private int userId;
    private String userName;
    private String time;
    private String deviceId;
    private int versionCode;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    @Override
    public String toString() {
        return String.format(Locale.CHINA,"Did:%s, uid:%d, name:%s versionCode:%d time:%s", deviceId ,userId,userName, versionCode,time);
    }
}
