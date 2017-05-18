package com.sun.connect;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by guoyao on 2016/12/13.
 */
public class RequestJson {
    private String code;
    private List<String> args;
    private String requestId = "0";

    private String deviceId;

    public RequestJson(){
        args = new ArrayList<>();
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void addArg(String arg){
        args.add(arg);
    }

    public void addArgs(String... args){
        for(String arg : args) {
            this.args.add(arg);
        }
    }

    public void setArg(int index, String arg){
        args.set(index,arg);
    }

    public List<String> getArgs(){
        return args;
    }

    public void clearArgs(){
        args.clear();
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
}
