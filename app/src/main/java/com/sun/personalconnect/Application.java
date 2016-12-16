package com.sun.personalconnect;

import android.content.Context;

import com.sun.account.Account;
import com.sun.conversation.CvsHistoryManager;

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
    }

    public CvsHistoryManager getCvsHistoryManager(){
        return cvsHistoryManager;
    }

    public Account getAccount(){
        return account;
    }
}
