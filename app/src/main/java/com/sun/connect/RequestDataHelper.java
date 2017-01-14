package com.sun.connect;

/**
 * Created by guoyao on 2016/12/20.
 */
public class RequestDataHelper {
    public static final String CODE_ConversationLongLink = "11";
    public static final String CODE_ConversationNote = "12";
    public static final String CODE_ConversationDisconnect = "13";
    public static final String CODE_ConversationImage = "15";


    public static final RequestData CvsConnect = new RequestData();

    public static final String CvsConnectRequest = "{\"args\":[],\"code\":\"11\",\"requestId\":0,\"deviceId\":\"%s\"}";
    static {
        CvsConnect.setCode(CODE_ConversationLongLink);
    }
}
