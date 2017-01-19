package com.sun.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AlertDialog;

/**
 * Created by guoyao on 2017/1/17.
 */
public class Permissions {

    private void showMessageOKCancel(Context context, String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(context)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    public static boolean selfPermissionGranted(Context context, String permission) {
        // For Android < Android M, self permissions are always granted.
        boolean result;
        if(isAndroidM(context)){
            result = context.checkSelfPermission(permission)
                    == PackageManager.PERMISSION_GRANTED;
        }else{
            result = PermissionChecker.checkSelfPermission(context, permission)
                    == PermissionChecker.PERMISSION_GRANTED;
        }
        return result;
    }

    public static boolean isAndroidM(Context context){
        int target =  getTargetVersion(context);
        return target>= Build.VERSION_CODES.M && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    public static int getTargetVersion(Context context){
        int targetSdkVersion = -1;
        try {
            final PackageInfo info = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0);
            targetSdkVersion = info.applicationInfo.targetSdkVersion;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return targetSdkVersion;
    }
}
