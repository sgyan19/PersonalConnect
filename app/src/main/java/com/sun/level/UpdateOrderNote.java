package com.sun.level;

/**
 * Created by sun on 2017/5/21.
 */

public class UpdateOrderNote extends OrderNote{
    private int versionCode;
    private String apkName;

    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public String getApkName() {
        return apkName;
    }

    public void setApkName(String apkName) {
        this.apkName = apkName;
    }
}
