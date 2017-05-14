package com.sun.connect;

/**
 * Created by guoyao on 2016/12/14.
 */
public interface SocketCallback {
    void onError(String requestKey,Throwable e);
    void onComplete(String requestKey, SocketMessage response);
    void onConnected(String requestKey);
}
