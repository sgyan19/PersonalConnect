package com.sun.device;

import android.location.Location;

import com.sun.common.SessionNote;

/**
 * Created by guoyao on 2017/4/18.
 */
public class DeviceInfo extends SessionNote {

    public String deviceId;
    public String buildBoard; //主板
    public String buildBootloader; //系统启动程序版本
    public String buildBrand; //系统定制商
    public String buildCpuAbi; //cpu指令集
    public String buildCpuAbi2; //cpu指令集2
    public String buildDevice; //设置参数
    public String buildDisplay;    //显示屏参数
    public String buildManufacturer;   //硬件制造商
    public String buildRadioversion;   //无线电固件版本
    public String buildFingerprint;    //硬件识别码
    public String buildHardware;   //硬件名称
    public String buildHost;   //HOS
    public String buildId;     // 修订版本列表
    public String buildModel;  //手机型号(MI XXX)
    public String buildSerial; //硬件序列号
    public String buildProduct;    //手机制造商
    public String buildTags;   //描述Build的标签
    public long buildTime;   //编译时间
    public String buildType;   //builder类型
    public String buildUser;   //USER
    public String buildVersionCodename; //当前开发代号
    public String buildVersionIncremental; //源码控制版本号
    public int buildVersionSDKInt;  //版本号
    public String buildVersionRelease; //版本字符串

    public String osVersion;   //OS版本
    public String osName;  //OS名称
    public String osArch;  //OS架构
    public String userHome;    //HOME属性
    public String userName;    //Name属性
    public String userDir;     //Dir属性
    public String userTimezone;    //时区
    public String pathSeparator;   //路径分隔符
    public String lineSeparator;   //行分隔符
    public String fileSeparator;   //文件分隔符
    public String javaVendorUrl;   //Java vendor URL 属性
    public String javaClassPath;   //Java Class  版本
    public String javaClassVersion;    //Java Class  版本
    public String javaVendor;      //Java Vendor 属性
    public String javaVersion;     //Java 版本
    public String javaHome;        //Java Home属性

    public Battery battery; // 电量
    public Location location; // 位置信息

    @Override
    public String toString(){
        return String.format("%s,%s,%s,%s,%s,%s,%s",buildCpuAbi, osName,osArch,osVersion, battery, buildProduct,buildModel );
    }
}
