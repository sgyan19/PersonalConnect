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
    public static final int STATUS_INIT = 0;
    public static final int STATUS_SUC = 1;
    public static final int STATUS_FAL = 2;
    @Id
    private long id;
    private String content;
    private String userName;
    private int userId;
    private String timeFormat;
    private long timeStamp;
    private String extend;
    private int sendStatus;

    @Generated(hash = 1880824119)
    public CvsNote() {
    }

    @Generated(hash = 276671708)
    public CvsNote(long id, String content, String userName, int userId,
            String timeFormat, long timeStamp, String extend, int sendStatus) {
        this.id = id;
        this.content = content;
        this.userName = userName;
        this.userId = userId;
        this.timeFormat = timeFormat;
        this.timeStamp = timeStamp;
        this.extend = extend;
        this.sendStatus = sendStatus;
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
}
