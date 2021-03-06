package com.sun.connect;


/**
 * Created by sun on 2017/1/14.
 */

public class SocketMessage {

    public static final int SOCKET_TYPE_NONE = -1;
    public static final int SOCKET_TYPE_HEART = 0;
    public static final int SOCKET_TYPE_JSON = 100;
    public static final int SOCKET_TYPE_JSON_DOWNLOAD_RAW = 101;
    public static final int SOCKET_TYPE_RAW = 102;

    public int type = SOCKET_TYPE_NONE;
    public String data;
    public Throwable exception;

    public SocketMessage(int type, String data){
        this.type = type;
        this.data = data;
    }

    public SocketMessage(){
    }

    public final static SocketMessage EmptyMessage = new SocketMessage(SOCKET_TYPE_NONE, "");
}
