package com.sun.connect;

/**
 * Created by guoyao on 2017/4/21.
 */
public class EventNetwork {
    private int key;
    private ResponseJson response;
    private String error;
    private int step;
    private Object object;

    public void reset(){
        object = null;
        step = 0;
        key = -1;
        response = null;
        error = "";
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public ResponseJson getResponse() {
        return response;
    }

    public void setResponse(ResponseJson response) {
        this.response = response;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }
}
