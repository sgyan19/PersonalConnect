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
        @Index(value = "timeStamp DESC", unique = true)
})
public class CvsNote implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private int id;
    private String content;
    private String userName;
    private int userId;
    private String timeFormat;
    private long timeStamp;
    private String extend;
    private boolean isSend;

    @Generated(hash = 1306584971)
    public CvsNote(int id, String content, String userName, int userId,
            String timeFormat, long timeStamp, String extend, boolean isSend) {
        this.id = id;
        this.content = content;
        this.userName = userName;
        this.userId = userId;
        this.timeFormat = timeFormat;
        this.timeStamp = timeStamp;
        this.extend = extend;
        this.isSend = isSend;
    }

    @Generated(hash = 1880824119)
    public CvsNote() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
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

    public boolean isSend() {
        return isSend;
    }

    public void setIsSend(boolean isSend) {
        this.isSend = isSend;
    }

    public boolean getIsSend() {
        return this.isSend;
    }
}
