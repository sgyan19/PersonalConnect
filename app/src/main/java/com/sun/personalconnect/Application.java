package com.sun.personalconnect;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;

import com.sun.account.Account;
import com.sun.conversation.CvsHistoryManager;
import com.sun.connect.SocketService;
import com.sun.conversation.CvsService;

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

    private boolean mUiApp;
    //private SocketTask socketTask;
    @Override
    public void onCreate() {
        super.onCreate();
        mApp = this;
        String packageName = getPackageName();
        String processName = getProcessName();
        if(packageName.equals(processName)) {
            mUiApp = true;
            init();
        }
    }

    private void init(){
        account = new Account();
        cvsHistoryManager = new CvsHistoryManager();
        cvsHistoryManager.init(this);

        startService(new Intent(this, SocketService.class));
        startService(new Intent(this, CvsService.class));
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
        if(mUiApp) {
            cvsHistoryManager.close();
        }
        super.onTerminate();
    }

    /**
     * 获取Context所在进程的名称
     *
     * @return
     */
    public String getProcessName() {
        int pid = android.os.Process.myPid();
        ActivityManager mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo appProcess : mActivityManager.getRunningAppProcesses()) {
            if (appProcess.pid == pid) {
                return appProcess.processName;
            }
        }
        return null;
    }
}
