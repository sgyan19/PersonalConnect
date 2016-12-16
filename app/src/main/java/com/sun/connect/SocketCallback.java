package com.sun.connect;

/**
 * Created by guoyao on 2016/12/14.
 */
public interface SocketCallback {
    void onError(int eventId,Throwable e);
    void onComplete(int eventId, ResponseData data);
    void onConnect(int eventId);
}
