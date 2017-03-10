package com.sun.level;

import android.os.Handler;
import android.os.Looper;

import com.sun.account.Account;
import com.sun.personalconnect.Application;

/**
 * Created by guoyao on 2017/1/5.
 */
public class LevelCenter {

    private RingNoteTask mRingNoteTask;
    private Handler mMainHandler;

    public void executeRingNote(){
        if(!clientCheck()){
            return;
        }
        if(mMainHandler == null){
            mMainHandler = new Handler(Looper.getMainLooper());
        }
        if(mRingNoteTask == null){
            mRingNoteTask = new RingNoteTask(mMainHandler);
        }
        mRingNoteTask.execute();
    }

    public void closeRingNote(){
        if(mRingNoteTask != null){
            mRingNoteTask.close();
        }
    }

    public boolean serverCheck(){
        return Application.App.getAccount().isLoginAccount(Account.Server.getId());
    }

    public boolean clientCheck(){
        return Application.App.getAccount().isLoginAccount(Account.Client.getId());
    }

    public static boolean serverCheck(long id){
        return Account.Server.getId() == id;
    }

    public static boolean clientCheck(long id){
        return Account.Client.getId() == id;
    }
}
