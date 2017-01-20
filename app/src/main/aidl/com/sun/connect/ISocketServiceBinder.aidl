package com.sun.connect;

/**
 * Created by guoyao on 2016/12/23.
 */
interface ISocketServiceBinder  {
    void request(int key, int type, String request);
    boolean isConnected();
    void stopReceive();
}
