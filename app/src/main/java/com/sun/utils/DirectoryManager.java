package com.sun.utils;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import java.io.File;

public class DirectoryManager {
    private static Context sAppContext;

    public static void init(Context context) {
        sAppContext = context.getApplicationContext();
    }

    private final static String SD_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();

    private final static String SD_APP_PATH = SD_PATH + "/sun";

    public static String getPrivateCachePath() {
        return sAppContext.getCacheDir().getAbsolutePath();
    }

    public static String getPrivateFilesPath() {
        return sAppContext.getFilesDir().getAbsolutePath();
    }

    public static String getCachePath() {
        if (hasExternalStorage()) {
            return sAppContext.getExternalCacheDir().getAbsolutePath();
        } else {
            return getPrivateCachePath();
        }
    }

    public static String getFilesPath() {
        if (hasExternalStorage()) {
            return sAppContext.getExternalFilesDir(null).getAbsolutePath();
        } else {
            return getPrivateFilesPath();
        }
    }

    public static String getAppPath() {
        if (hasExternalStorage()) {
            return SD_APP_PATH;
        } else {
            return getPrivateFilesPath();
        }
    }

    public static String getShareCachePath() {
        return getCachePath() + "/share";
    }

    public static String getImageCachePath() {
        return getCachePath() + "/image";
    }

    public static String getLogCachePath() {
        return getCachePath() + "/log";
    }

    public static String getCrashCachePath() {
        return getCachePath() + "/crash";
    }

    public static String getDownloadPath() {
        return getAppPath() + "/download";
    }

    public static boolean hasExternalStorage() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    public static boolean checkPath(String path) {

        if (TextUtils.isEmpty(path))
            return false;
        File file = new File(path);
        if (!file.exists()) {
            return file.mkdirs();
        } else {
            return file.isDirectory();
        }
    }

}
