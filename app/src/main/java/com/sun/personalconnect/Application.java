package com.sun.personalconnect;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.sun.account.Account;
import com.sun.connect.NetworkChannel;
import com.sun.connect.ResponseHistoryManager;
import com.sun.conversation.CvsHistoryManager;
import com.sun.connect.SocketService;
import com.sun.conversation.CvsService;
import com.sun.gps.GpsService;
import com.sun.level.LevelCenter;
import com.sun.level.Ring;
import com.sun.service.AnswerService;
import com.sun.utils.DirectoryManager;
import com.sun.utils.SharedPreferencesUtil;
import com.tencent.bugly.crashreport.CrashReport;

/**
 * Created by guoyao on 2016/12/13.
 */
public class Application extends android.app.Application {
    private static final String TAG = "APPApplication";

    public String KEY_STRING_DEVICEID = "device_id";

    public static Application App;

    public static Context getContext() {
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

    private NetworkChannel mNetworkChannel;
    private InfoKeeper mInfoKeeper;
    public String VersionName;
    public int VersionCode;

    @Override
    public void onCreate() {
        super.onCreate();
        App = this;
        String packageName = getPackageName();
        String processName = getProcessName();
        if (packageName.equals(processName)) {
            mUiApp = true;
            init();
        }else {
            mDeviceId = getDeviceId();
            if(TextUtils.isEmpty(mDeviceId)){
                SharedPreferencesUtil.ObserverSharedPreferenceChange(KEY_STRING_DEVICEID, new SharedPreferencesUtil.OnPreferencesChangedListener() {
                    @Override
                    public void onChanged(String key, String value) {
                        Log.d(TAG, "mDeviceId SharedPreferenceChange:" + value);
                        mDeviceId = value;
                    }
                },false);
            }
        }
        mSocketRawFolder = DirectoryManager.getDownloadPath();
    }

    private void init() {
        CrashReport.initCrashReport(getApplicationContext(), "0e5b8e8cca", true);
        initVersionInfo();
        account = new Account();
        daoSessionManager = new DaoSessionManager();
        cvsHistoryManager = new CvsHistoryManager();
        responseHistoryManager = new ResponseHistoryManager();
        responseHistoryManager.init(daoSessionManager.getDaoSession(this));
        cvsHistoryManager.init(daoSessionManager.getDaoSession(this));

        mNetworkChannel = new NetworkChannel();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getContext())
                .threadPriority(Thread.NORM_PRIORITY - 1).threadPoolSize(4)
                .denyCacheImageMultipleSizesInMemory().tasksProcessingOrder(QueueProcessingType.LIFO)
                .diskCacheSize(50 * 1024 * 1024).diskCacheFileCount(100)
                .build();
        ImageLoader.getInstance().init(config);
        startService(new Intent(this, SocketService.class));
        startService(new Intent(this, CvsService.class));
        startService(new Intent(this, GpsService.class));
        startService(new Intent(this, AnswerService.class));
        mNetworkChannel.init(this);
        mRing = new Ring();
        mLevelCenter = new LevelCenter();

        BaseActivity.requestPermissionExt(new Permission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                new Permission.Runnable() {
                    @Override
                    public void run(Permission p) {
                        Log.d(TAG, "WRITE_EXTERNAL_STORAGE permission " + p.isSuccess());
                        initPaths(getContext());
                    }
                }
        ));
        BaseActivity.requestPermissionExt(new Permission(
                Manifest.permission.READ_PHONE_STATE,
                new Permission.Runnable() {
                    @Override
                    public void run(Permission p) {
                        if(p.isSuccess()) {
                            Log.d(TAG, "READ_PHONE_STATE permission OK");
                            initDeviceId();
                        }else {
                            Log.d(TAG, "READ_PHONE_STATE no permission");
                        }
                    }
                }
        ));
        mInfoKeeper = new InfoKeeper();
        //socketTask = new SocketTask();
        //socketTask.start();
    }

    public CvsHistoryManager getCvsHistoryManager() {
        return cvsHistoryManager;
    }

    public ResponseHistoryManager getResponseHistoryManager() {
        return responseHistoryManager;
    }

    public DaoSessionManager getDaoSessionManager() {
        return daoSessionManager;
    }

    public Account getAccount() {
        return account;
    }

//    public SocketTask getSocketTask(){
//        return socketTask;
//    }

    public Ring getRing() {
        return mRing;
    }

    public LevelCenter getLevelCenter() {
        return mLevelCenter;
    }

    @Override
    public void onTerminate() {
        if (mUiApp) {
            daoSessionManager.release();
            mNetworkChannel.release();
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

    public String getDeviceId() {
        Log.d(TAG, "getDeviceId:" + mDeviceId);
        if(TextUtils.isEmpty(mDeviceId)){
            mDeviceId = SharedPreferencesUtil.getString(KEY_STRING_DEVICEID);
        }
        return mDeviceId;
    }

    public String getSocketRawFolder() {
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

    public void initDeviceId() {
        mDeviceId = Build.MODEL.replace(" ", "").replace("-", "_") + "_" + ((TelephonyManager) getSystemService(TELEPHONY_SERVICE))
                .getDeviceId();
        Log.d(TAG, "initDeviceId:" + mDeviceId);
        SharedPreferencesUtil.putString(KEY_STRING_DEVICEID, mDeviceId);
    }

    public NetworkChannel getNetworkService() {
        return mNetworkChannel;
    }

    public InfoKeeper getInfoKeeper() {
        return mInfoKeeper;
    }

    /**
     * get App versionCode
     * @return
     */
    public void initVersionInfo(){
        PackageManager packageManager=getPackageManager();
        PackageInfo packageInfo;
        try {
            packageInfo=packageManager.getPackageInfo(getPackageName(),0);
            VersionCode = packageInfo.versionCode;
            VersionName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        Log.d(TAG,String.format("VERSION_CODE:%d,VERSION_NAME:%s", VersionCode, VersionName));
    }
}