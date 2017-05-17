package com.sun.device;

/**
 * Created by guoyao on 2017/4/21.
 */
public class AnswerNote extends BaseNote{
    private int userId;
    private String userName;
    private String time;

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

    @Override
    public String toString() {
        return String.format("Did:%s, uid:%d, name:%s time:%s",getDeviceId(),userId,userName,time);
    }
}
