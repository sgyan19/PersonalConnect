package com.sun.connect;

/**
 * Created by guoyao on 2016/12/14.
 */
public interface SocketCallback {
    void onError(int requestKey,Throwable e);
    void onComplete(int requestKey, String stream);
    void onConnected(int requestKey);
}
