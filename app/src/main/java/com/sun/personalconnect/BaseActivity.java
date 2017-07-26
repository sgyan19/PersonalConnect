package com.sun.personalconnect;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import com.sun.utils.PermissionUtils;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by guoyao on 2017/2/6.
 */
public class BaseActivity extends AppCompatActivity {
    private static LinkedList<BaseActivity> InstanceStack = new LinkedList<>();
    public static LinkedList<Permission> WaitForRequest = new LinkedList<>();
    public static LinkedList<Permission> WaitForCallback = new LinkedList<>();
    public final static int LIVE_UNKOWN = -1;
    public final static int LIVE_CREATE = 0;
    public final static int LIVE_RESUME = 1;
    public final static int LIVE_START = 2;
    public final static int LIVE_PAUSE = 3;
    public final static int LIVE_STOP = 4;
    public final static int LIVE_DESTROY = 5;


    private int mLiveStatus = LIVE_UNKOWN;

    public Permission[] PermissionData = new Permission[]{
            new Permission(Manifest.permission.WRITE_EXTERNAL_STORAGE, new Permission.Runnable() {
                @Override
                public void run(Permission p) {
                    Application.App.initPaths(Application.getContext());
                }
            }),
            new Permission(Manifest.permission.READ_PHONE_STATE, new Permission.Runnable() {
                @Override
                public void run(Permission p) {
                    Application.App.initDeviceId();
                }
            }),
            new Permission(Manifest.permission.ACCESS_COARSE_LOCATION, new Permission.Runnable() {
                @Override
                public void run(Permission p) {
//                    Application.App.initDeviceId();
                }
            }),
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLiveStatus = LIVE_CREATE;
        InstanceStack.add(this);
        if(WaitForRequest.size() > 0 ) {
            Iterator<Permission> iterator = WaitForRequest.iterator();
            while (iterator.hasNext()) {
                Permission p = iterator.next();
                requestPermission(p);
                iterator.remove();
            }
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        mLiveStatus = LIVE_CREATE;
    }

    @Override
    protected void onStart() {
        super.onStart();
        mLiveStatus = LIVE_START;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mLiveStatus = LIVE_RESUME;
    }

    @Override
    protected void onPause() {
        super.onPause();
        mLiveStatus = LIVE_PAUSE;
    }

    @Override
    protected void onStop() {
        super.onStop();
        mLiveStatus = LIVE_STOP;
    }

    @Override
    protected void onDestroy() {
        InstanceStack.remove(this);
        mLiveStatus = LIVE_DESTROY;
        super.onDestroy();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(WaitForCallback.size() > 0 ) {
            Iterator<Permission> iterator = WaitForCallback.iterator();
            while (iterator.hasNext()) {

                Permission p = iterator.next();
                if(PermissionUtils.selfPermissionGranted(this,p.getName())){
                    p.setSuccess(true);
                    p.getRunnable().run(p);
                    iterator.remove();
                    continue;
                }
                if(p.getRequest() == requestCode) {
                    if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        p.setSuccess(true);
                        p.getRunnable().run(p);
                    }else{
                        p.setSuccess(false);
                        p.getRunnable().run(p);
                    }
                    iterator.remove();
                    break;
                }
            }
        }
    }

    public void requestPermission(Permission permission){
        if(WaitForCallback.contains(permission)) return;
        boolean isPermission = PermissionUtils.selfPermissionGranted(this, permission.getName());
        if (!isPermission) {
            try {
                ActivityCompat.requestPermissions(this,
                        new String[]{permission.getName()},
                        permission.getRequest());
                WaitForCallback.add(permission);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            permission.setSuccess(true);
            permission.getRunnable().run(permission);
        }
    }

    public static void requestPermissionExt(Permission permission){
        if(InstanceStack.size() == 0){
            WaitForRequest.add(permission);
        }else{
            InstanceStack.getLast().requestPermission(permission);
        }
    }

    public static BaseActivity getAnyInstance(){
        if(InstanceStack.size() == 0){
            return null;
        }
        return InstanceStack.getLast();
    }

    public static boolean isBackground(){
        if(InstanceStack.size() == 0){
            return true;
        }
        for(BaseActivity activity : InstanceStack){
            if(activity.mLiveStatus == LIVE_RESUME){
                return true;
            }
        }
        return false;
    }

    public static boolean isBackground(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.processName.equals(context.getPackageName())) {
                if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    System.out.print(String.format("Foreground App:", appProcess.processName));
                    return false;
                }else{
                    System.out.print("Background App:"+appProcess.processName);
                    return true;
                }
            }
        }
        return false;
    }
}
