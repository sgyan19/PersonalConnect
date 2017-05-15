package com.sun.personalconnect;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.telephony.TelephonyManager;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.sun.account.Account;
import com.sun.connect.AppLifeNetworkService;
import com.sun.connect.ResponseHistoryManager;
import com.sun.conversation.CvsHistoryManager;
import com.sun.connect.SocketService;
import com.sun.conversation.CvsService;
import com.sun.gps.GpsService;
import com.sun.level.LevelCenter;
import com.sun.level.Ring;
import com.sun.utils.DirectoryManager;
import com.sun.utils.SharedPreferencesUtil;

/**
 * Created by guoyao on 2016/12/13.
 */
public class Application extends android.app.Application {

    public String KEY_STRING_DEVICEID = "device_id";

    public static Application App;

    public static Context getContext(){
        return App.getApplicationContext();
    }

    private CvsHistoryManager cvsHistoryManager;
    private ResponseHistoryManager responseHistoryManager;
    private DaoSessionManager daoSessionManager;
    private Account account;

    private boolean mUiApp;

    private String mDeviceId;

    private String mSocketRawFolder;
    //private SocketTask socketTask;
    private LevelCenter mLevelCenter;
    private Ring mRing;

    private AppLifeNetworkService mAppLifeNetworkService;
    private InfoKeeper mInfoKeeper;
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
        mDeviceId = SharedPreferencesUtil.getString(KEY_STRING_DEVICEID);
        BaseActivity.requestPermissionExt(new Permission(
                Manifest.permission.READ_PHONE_STATE,
                new Permission.Runnable() {
                    @Override
                    public void run(Permission p) {
                        initDeviceId();
                    }
                }
        ));
        mSocketRawFolder = DirectoryManager.getDownloadPath();
    }

    private void init(){
        account = new Account();
        daoSessionManager = new DaoSessionManager();
        cvsHistoryManager = new CvsHistoryManager();
        responseHistoryManager = new ResponseHistoryManager();
        responseHistoryManager.init(daoSessionManager.getDaoSession(this));
        cvsHistoryManager.init(daoSessionManager.getDaoSession(this));

        mAppLifeNetworkService = new AppLifeNetworkService();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getContext())
                .threadPriority(Thread.NORM_PRIORITY - 1).threadPoolSize(4)
                .denyCacheImageMultipleSizesInMemory().tasksProcessingOrder(QueueProcessingType.LIFO)
                .diskCacheSize(50 * 1024 * 1024).diskCacheFileCount(100)
                .build();
        ImageLoader.getInstance().init(config);
        startService(new Intent(this, SocketService.class));
        startService(new Intent(this, CvsService.class));
        startService(new Intent(this, GpsService.class));
        mAppLifeNetworkService.init();
        mRing = new Ring();
        mLevelCenter = new LevelCenter();

        BaseActivity.requestPermissionExt(new Permission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                new Permission.Runnable() {
                    @Override
                    public void run(Permission p) {
                        initPaths(getContext());
                    }
                }
        ));
        mInfoKeeper = new InfoKeeper();
        //socketTask = new SocketTask();
        //socketTask.start();
    }

    public CvsHistoryManager getCvsHistoryManager(){
        return cvsHistoryManager;
    }

    public ResponseHistoryManager getResponseHistoryManager(){
        return responseHistoryManager;
    }

    public DaoSessionManager getDaoSessionManager(){
        return daoSessionManager;
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

    public LevelCenter getLevelCenter(){
        return mLevelCenter;
    }
    @Override
    public void onTerminate() {
        if(mUiApp) {
            daoSessionManager.release();
            mAppLifeNetworkService.release();
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

    public String getSocketRawFolder(){
        return mSocketRawFolder;
    }

    public void initPaths(Context context) {
        DirectoryManager.init(context);

        DirectoryManager.checkPath(DirectoryManager.getPrivateCachePath());
        DirectoryManager.checkPath(DirectoryManager.getPrivateFilesPath());
        DirectoryManager.checkPath(DirectoryManager.getCachePath());
        DirectoryManager.checkPath(DirectoryManager.getFilesPath());
        DirectoryManager.checkPath(DirectoryManager.getAppPath());
        DirectoryManager.checkPath(DirectoryManager.getLogCachePath());
        DirectoryManager.checkPath(DirectoryManager.getImageCachePath());
        DirectoryManager.checkPath(DirectoryManager.getCrashCachePath());
        DirectoryManager.checkPath(DirectoryManager.getShareCachePath());
        DirectoryManager.checkPath(DirectoryManager.getDownloadPath());
    }

    public void initDeviceId(){
        mDeviceId = Build.MODEL.replace(" ","").replace("-","_") + "_" + ((TelephonyManager) getSystemService(TELEPHONY_SERVICE))
                .getDeviceId();
        SharedPreferencesUtil.putString(KEY_STRING_DEVICEID, mDeviceId);
    }

    public AppLifeNetworkService getNetworkService(){
        return mAppLifeNetworkService;
    }

    public InfoKeeper getInfoKeeper(){
        return mInfoKeeper;
    }
}
