package com.sun.connect;

/**
 * Created by guoyao on 2016/12/13.
 */
public class ResponseJson {
    private String code;
    private String data;
    private int requestId;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }
}
