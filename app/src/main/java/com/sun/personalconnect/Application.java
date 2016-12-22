package com.sun.personalconnect;

import android.content.Context;
import android.content.Intent;

import com.sun.account.Account;
import com.sun.conversation.CvsHistoryManager;
import com.sun.connect.SocketService;

/**
 * Created by guoyao on 2016/12/13.
 */
public class Application extends android.app.Application {

    private static Application mApp;

    public static Application getInstance(){
        return mApp;
    }

    public static Context getContext(){
        return mApp.getApplicationContext();
    }

    private CvsHistoryManager cvsHistoryManager;
    private Account account;
    //private SocketTask socketTask;
    @Override
    public void onCreate() {
        super.onCreate();
        mApp = this;
        init();
    }

    private void init(){
        account = new Account();
        cvsHistoryManager = new CvsHistoryManager();
        cvsHistoryManager.init(this);

        startService(new Intent(this, SocketService.class));
        //socketTask = new SocketTask();
        //socketTask.start();
    }

    public CvsHistoryManager getCvsHistoryManager(){
        return cvsHistoryManager;
    }

    public Account getAccount(){
        return account;
    }

//    public SocketTask getSocketTask(){
//        return socketTask;
//    }

    @Override
    public void onTerminate() {
        cvsHistoryManager.close();
        super.onTerminate();
    }
}
