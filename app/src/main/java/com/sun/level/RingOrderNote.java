package com.sun.level;

/**
 * Created by sun on 2017/5/21.
 */

public class RingOrderNote extends OrderNote{
    private String filePath;
    private long time;
    private boolean ifNow;

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public boolean isIfNow() {
        return ifNow;
    }

    public void setIfNow(boolean ifNow) {
        this.ifNow = ifNow;
    }
}
