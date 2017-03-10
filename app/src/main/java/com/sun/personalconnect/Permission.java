package com.sun.personalconnect;

/**
 * Created by guoyao on 2017/2/16.
 */
public class Permission {
    private String name;
    private int request;
    private Runnable runnable;
    private Boolean success = null;
    public interface Runnable{
        void run(Permission permission);
    }

    public Permission(String name, Runnable runnable){
        this.name = name;
        this.request = hashCode() & 0xffff;
        this.runnable = runnable;
    }

    public String getName() {
        return name;
    }

    public int getRequest() {
        return request;
    }

    public Runnable getRunnable() {
        return runnable;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean suc){
        success = suc;
    }
}
