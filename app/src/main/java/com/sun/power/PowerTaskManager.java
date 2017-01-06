package com.sun.power;

import com.sun.account.Account;
import com.sun.personalconnect.Application;

/**
 * Created by guoyao on 2017/1/5.
 */
public class PowerTaskManager {

    private RingNoteTask mRingNoteTask;

    public void executeRingNote(){
        if(!clientCheck()){
            return;
        }
        if(mRingNoteTask == null){
            mRingNoteTask = new RingNoteTask();
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
}
