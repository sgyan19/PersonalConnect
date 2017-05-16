package com.sun.device;

/**
 * Created by guoyao on 2017/4/21.
 */
public class AskNote {
    public static final int TYPE_EASY = 0;
    public static final int TYPE_DETAIL = 1;

    private int type;
    private String deviceId;
    public AskNote(){
        this(TYPE_EASY,null);
    }

    public AskNote(int type,String deviceId){
        this.type = type;
        this.deviceId = deviceId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
}
