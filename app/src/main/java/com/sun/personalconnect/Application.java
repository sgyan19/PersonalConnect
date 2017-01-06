package com.sun.personalconnect;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;

import com.sun.account.Account;
import com.sun.conversation.CvsHistoryManager;
import com.sun.connect.SocketService;
import com.sun.conversation.CvsService;
import com.sun.power.PowerTaskManager;
import com.sun.power.Ring;

/**
 * Created by guoyao on 2016/12/13.
 */
public class Application extends android.app.Application {

    public static Application App;

    public static Context getContext(){
        return App.getApplicationContext();
    }

    private CvsHistoryManager cvsHistoryManager;
    private Account account;

    private boolean mUiApp;

    private String mDeviceId;
    //private SocketTask socketTask;
    private PowerTaskManager mPowerTaskManager;
    private Ring mRing;
    @Override
    public void onCreate() {
        super.onCreate();
        App = this;
        String packageName = getPackageName();
        String processName = getProcessName();
        if(packageName.equals(processName)) {
            mUiApp = true;
            init();
        }
        mDeviceId = ((TelephonyManager) getSystemService(TELEPHONY_SERVICE))
                .getDeviceId();
    }

    private void init(){
        account = new Account();
        cvsHistoryManager = new CvsHistoryManager();
        cvsHistoryManager.init(this);

        startService(new Intent(this, SocketService.class));
        startService(new Intent(this, CvsService.class));
        mRing = new Ring();
        mPowerTaskManager = new PowerTaskManager();
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

    public Ring getRing(){
        return mRing;
    }

    public PowerTaskManager getPowerTaskManger(){
        return mPowerTaskManager;
    }
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

    public String getDeviceId(){
        return mDeviceId;
    }
}
