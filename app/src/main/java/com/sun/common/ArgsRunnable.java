package com.sun.common;

/**
 * Created by guoyao on 2017/4/13.
 */
public abstract class ArgsRunnable implements Runnable{
    private Object[] args;
    private ArgsRunnable(){}
    public ArgsRunnable(Object... args){
        this.args = args;
    }

    protected Object[] getArgs(){
        return args;
    }
}