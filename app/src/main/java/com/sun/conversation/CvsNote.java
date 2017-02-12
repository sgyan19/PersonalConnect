package com.sun.conversation;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.NotNull;

import java.io.Serializable;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by guoyao on 2016/12/16.
 */
@Entity(indexes = {
        @Index(value = "timeStamp ASC", unique = true)
})
public class CvsNote implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final int STATUS_SENDING = 0;
    public static final int STATUS_SUC = 1;
    public static final int STATUS_FAL = 2;

    public static final int TYPE_TEXT = 10;
    public static final int TYPE_IMAGE = 11;

    public static final int POWER_NORMAL = 20;
    public static final int POWER_RING = 21;

    @Id
    private long id;
    private String content;
    private String userName;
    private int userId;
    private String timeFormat;
    private long timeStamp;
    private String extend;
    private int sendStatus;
    private int type = TYPE_TEXT;
    private int power = POWER_NORMAL;

    @Generated(hash = 1880824119)
    public CvsNote() {
    }

    @Generated(hash = 1612060684)
    public CvsNote(long id, String content, String userName, int userId,
            String timeFormat, long timeStamp, String extend, int sendStatus,
            int type, int power) {
        this.id = id;
        this.content = content;
        this.userName = userName;
        this.userId = userId;
        this.timeFormat = timeFormat;
        this.timeStamp = timeStamp;
        this.extend = extend;
        this.sendStatus = sendStatus;
        this.type = type;
        this.power = power;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
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

    public String getTimeFormat() {
        return timeFormat;
    }

    public void setTimeFormat(String timeFormat) {
        this.timeFormat = timeFormat;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getExtend() {
        return extend;
    }

    public void setExtend(String extend) {
        this.extend = extend;
    }

    public int getSendStatus() {
        return sendStatus;
    }

    public void setSendStatus(int sendStatus) {
        this.sendStatus = sendStatus;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getPower() {
        return power;
    }

    public void setPower(int power) {
        this.power = power;
    }

    public void clone(CvsNote tar){
        tar.setSendStatus(sendStatus);
        tar.setContent(content);
        tar.setExtend(extend);
        tar.setId(id);
        tar.setPower(power);
        tar.setTimeFormat(timeFormat);
        tar.setTimeStamp(timeStamp);
        tar.setType(type);
        tar.setUserId(userId);
        tar.setUserName(userName);
    }
}
