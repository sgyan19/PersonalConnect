package com.sun.common;

/**
 * Created by guoyao on 2017/4/13.
 */
public abstract class ArgsRunnable implements Runnable{
    private Object[] args;
    private Object[] result;
    private ArgsRunnable(){}
    public ArgsRunnable(Object... args){
        this.args = args;
    }

    protected Object[] getArgs(){
        return args;
    }

    protected void setResult(Object... result){
        this.result = result;
    }

    protected Object[] getResult(){
        return result;
    }
}