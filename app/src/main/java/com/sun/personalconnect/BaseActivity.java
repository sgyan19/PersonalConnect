package com.sun.personalconnect;

import android.Manifest;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import com.sun.utils.Permissions;

/**
 * Created by guoyao on 2017/2/6.
 */
public class BaseActivity extends AppCompatActivity {
    private static boolean PermissionCheck = false;
    public class Permission{
        String name;
        int request;
        Runnable runnable;
        boolean success = false;
        public Permission(String name, int code, Runnable runnable){
            this.name = name;
            this.request = code;
            this.runnable = runnable;
        }
    }

    public Permission[] PermissionData = new Permission[]{
            new Permission(Manifest.permission.WRITE_EXTERNAL_STORAGE, 98, new Runnable() {
                @Override
                public void run() {
                    Application.App.initPaths(Application.getContext());
                }
            }),
            new Permission(Manifest.permission.READ_PHONE_STATE, 99, new Runnable() {
                @Override
                public void run() {
                    Application.App.initDeviceId();
                }
            }),
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(!PermissionCheck) {
            for (int i = 0; i < PermissionData.length; i++) {
                boolean isPermission = Permissions.selfPermissionGranted(this, PermissionData[i].name);
                if (!isPermission) {
                    try {
                        ActivityCompat.requestPermissions(this,
                                new String[]{PermissionData[i].name},
                                PermissionData[i].request);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    PermissionData[i].runnable.run();
                    PermissionData[i].success = true;
                }
            }
        }
        PermissionCheck = true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for(int i = 0 ; i < PermissionData.length; i ++){
            if(requestCode == PermissionData[i].request){
                PermissionData[i].runnable.run();
                PermissionData[i].success = true;
                break;
            }
        }
    }
}
