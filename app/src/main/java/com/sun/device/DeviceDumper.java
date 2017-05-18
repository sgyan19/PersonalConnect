package com.sun.device;

import android.os.Build;

import com.sun.common.ArgsRunnable;
import com.sun.gps.Gps;
import com.sun.personalconnect.Application;

/**
 * Created by guoyao on 2017/4/18.
 */
public class DeviceDumper {

    static DeviceInfo info = new DeviceInfo();

    public static DeviceInfo dump(){
        info.buildBoard = Build.BOARD;
        info.buildBootloader = Build.BOOTLOADER;
        info.buildBrand = Build.BRAND;
        info.buildCpuAbi = Build.CPU_ABI;
        info.buildCpuAbi2 = Build.CPU_ABI2;
        info.buildDevice = Build.DEVICE;
        info.buildDisplay = Build.DISPLAY;
        info.buildFingerprint = Build.FINGERPRINT;
        info.buildHardware = Build.HARDWARE;
        info.buildHost = Build.HOST;
        info.buildId = Build.ID;
        info.buildManufacturer = Build.MANUFACTURER;
        info.buildModel = Build.MODEL;
        info.buildProduct = Build.PRODUCT;
        info.buildRadioversion = Build.getRadioVersion();
        info.buildSerial = Build.SERIAL;
        info.buildTags = Build.TAGS;
        info.buildTime = Build.TIME;
        info.buildType = Build.TYPE;
        info.buildUser = Build.USER;
        info.buildVersionCodename = Build.VERSION.CODENAME;
        info.buildVersionIncremental = Build.VERSION.INCREMENTAL;
        info.buildVersionRelease = Build.VERSION.RELEASE;
        info.buildVersionSDKInt = Build.VERSION.SDK_INT;

        info.osVersion = System.getProperty("os.version");
        info.osName = System.getProperty("os.name");
        info.osArch = System.getProperty("os.arch");
        info.userHome = System.getProperty("user.home");
        info.userName = System.getProperty("user.name");
        info.userDir = System.getProperty("user.dir");
        info.userTimezone = System.getProperty("user.timezone");
        info.pathSeparator = System.getProperty("path.separator");
        info.lineSeparator = System.getProperty("line.separator");
        info.fileSeparator = System.getProperty("file.separator");
        info.javaClassPath = System.getProperty("java.class.path");
        info.javaClassVersion = System.getProperty("java.class.version");
        info.javaHome = System.getProperty("java.home");
        info.javaVendor = System.getProperty("java.vendor");
        info.javaVendorUrl = System.getProperty("java.vendor.url");
        info.javaVersion = System.getProperty("java.version");

        info.battery = BatteryReceiver.getBattery();
        info.location = Gps.LastLocation;
        info.deviceId = Application.App.getDeviceId();
        return info;
    }
}
