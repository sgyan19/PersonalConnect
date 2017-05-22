package com.sun.gps;

import android.Manifest;
import android.content.Context;
import android.location.Criteria;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.util.Log;

import com.sun.device.BatteryReceiver;
import com.sun.personalconnect.Application;
import com.sun.personalconnect.BaseActivity;
import com.sun.personalconnect.Permission;
import com.sun.utils.FormatUtils;
import com.sun.utils.PermissionUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by guoyao on 2017/2/14.
 */
public class Gps {
    private LocationManager lm;
    private static final String TAG = "Gps";

    public static Location LastLocation;

    private GpsResponse mErrPermissionNote;
    private GpsResponse mErrDeviceNote;

    private LinkedList<WeakReference<GpsListener>> mListenerReferences;
    private HashSet<GpsListener> mHardListeners;
    private HashSet<GpsListener> mListeners;
    private GpsGear mGpsGear = GpsGear.None;

    private MyLocationListener mGpsListener = new MyLocationListener();
    private MyLocationListener mNetworkListener = new MyLocationListener();

    private BatteryReceiver.BatteryListener mBatteryListener = new BatteryReceiver.BatteryListener() {
        @Override
        public void onHighPower() {
            if(mGpsGear != GpsGear.High) {
                mGpsGear = GpsGear.High;
//                mListenBatteryContext.requestPermission(new Permission(Manifest.permission.ACCESS_FINE_LOCATION, mRequestGpsUpdate));
            }
        }

        @Override
        public void onLowPower() {
            if(mGpsGear != GpsGear.Low) {
                mGpsGear = GpsGear.Low;
//                mListenBatteryContext.requestPermission(new Permission(Manifest.permission.ACCESS_FINE_LOCATION, mRequestGpsUpdate));
            }
        }
    };

    public Gps() {
        mListenerReferences = new LinkedList<>();
        mListeners = new HashSet<>();
        mHardListeners = new HashSet<>();
        lm = (LocationManager) Application.App.getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        mErrPermissionNote = new GpsResponse();
        mErrPermissionNote.setErrInfo("");
        mErrPermissionNote.setErrType(GpsResponse.ERR_TYPE_PERMISSION);
        mErrDeviceNote = new GpsResponse();
        mErrDeviceNote.setErrInfo("");
        mErrDeviceNote.setErrType(GpsResponse.ERR_TYPE_DEVICE);
    }

    public boolean changeGpsGear(GpsGear gpsGear){
        if(gpsGear != GpsGear.Once){
            if(gpsGear == mGpsGear){
                return false;
            }else{
                stopUpdate();
                mGpsGear = gpsGear;
            }
        }

        BaseActivity activity = BaseActivity.getAnyInstance();
        if(activity == null){
            boolean isPermission = PermissionUtils.selfPermissionGranted(Application.App, Manifest.permission.ACCESS_FINE_LOCATION);
            if(mGpsGear == GpsGear.Once) {
                mRequestOnce.run(isPermission);
            }else if(mGpsGear != GpsGear.None){
                mRequestGpsUpdate.run(isPermission);
            }
        }else {
            if (mGpsGear == GpsGear.Once) {
                activity.requestPermission(new Permission(Manifest.permission.ACCESS_FINE_LOCATION, mRequestOnce));
            } else if(mGpsGear != GpsGear.None){
                activity.requestPermission(new Permission(Manifest.permission.ACCESS_FINE_LOCATION, mRequestGpsUpdate));
            }
        }
        return true;
    }

    private void stopUpdate(){
        // TODO Auto-generated method stub
        try {
            lm.removeUpdates(mGpsListener);
            lm.removeUpdates(mNetworkListener);
        }catch (SecurityException e){
            e.printStackTrace();
        }
    }
    private interface PermissionCallback extends Permission.Runnable{
        void run(boolean permission);
    }


    private PermissionCallback mRequestOnce = new PermissionCallback() {
        public void run(boolean permission){
                if (permission) {
                    boolean start = false;
                    if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        try {
                            lm.requestSingleUpdate(LocationManager.GPS_PROVIDER, mGpsListener, null);
                            start = true;
                            updateLocation(getLastKnownLocation(), "getLastKnownLocation");
                        } catch (SecurityException e) {
                            e.printStackTrace();
                        }
                    }

                    if(lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
                        try {
                            lm.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, mNetworkListener, null);
                        }catch (SecurityException e) {
                            e.printStackTrace();
                        }
                    }

                    if(!start) {
                        FormatUtils.fillCommonArgs(mErrDeviceNote);
                        for(GpsListener listener : getListeners()) {
                            listener.onGpsUpdate(mErrDeviceNote);
                        }
                    }
                } else {
                    FormatUtils.fillCommonArgs(mErrPermissionNote);
                    for(GpsListener listener : getListeners()) {
                        listener.onGpsUpdate(mErrPermissionNote);
                    }
                }
        }

        @Override
        public void run(Permission permission) {
            run(permission.isSuccess());
        }
    };

    private PermissionCallback mRequestGpsUpdate = new PermissionCallback() {
        public void run(boolean permission){
                if (permission){
                    updateLocation(getLastKnownLocation(), "getLastKnownLocation");
                    boolean start = false;
                    if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                        try {
                            // 这个太耗电，暂时不用
//                            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, mGpsGear.period, mGpsGear.minDistance, mGpsListener);
                            lm.requestSingleUpdate(LocationManager.GPS_PROVIDER, mGpsListener, null);
                            start = true;
                        }catch (SecurityException e) {
                            e.printStackTrace();
                        }
                    }
                    if(lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
                        try {
                            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, mGpsGear.period, mGpsGear.minDistance, mNetworkListener);
                            start = true;
                        }catch (SecurityException e) {
                            e.printStackTrace();
                        }
                    }
                    if(!start){
                        FormatUtils.fillCommonArgs(mErrDeviceNote);
                        for(GpsListener listener : getListeners()) {
                            listener.onGpsUpdate(mErrDeviceNote);
                        }
                    }
                }else{
                    FormatUtils.fillCommonArgs(mErrPermissionNote);
                    for(GpsListener listener : getListeners()) {
                        listener.onGpsUpdate(mErrPermissionNote);
                    }
                }
        }

        @Override
        public void run(Permission permission) {
            run(permission.isSuccess());
        }
    };

    public void addWeakListener(GpsListener listener){
        synchronized (mListenerReferences) {
            mListenerReferences.add(new WeakReference<>(listener));
        }
    }

    public void addHardListener(GpsListener listener){
        mHardListeners.add(listener);
    }

    private class MyLocationListener implements LocationListener{
        /**
         * 位置信息变化时触发
         */
        public void onLocationChanged(Location location) {
            updateLocation(location, "onLocationChanged");
            Log.i(TAG, "时间：" + location.getTime());
            Log.i(TAG, "经度：" + location.getLongitude());
            Log.i(TAG, "纬度：" + location.getLatitude());
            Log.i(TAG, "海拔：" + location.getAltitude());
        }

        /**
         * GPS状态变化时触发
         */
        public void onStatusChanged(String provider, int status, Bundle extras) {
            switch (status) {
                // GPS状态为可见时
                case LocationProvider.AVAILABLE:
                    Log.i(TAG, "当前GPS状态为可见状态");
                    break;
                // GPS状态为服务区外时
                case LocationProvider.OUT_OF_SERVICE:
                    Log.i(TAG, "当前GPS状态为服务区外状态");
                    break;
                // GPS状态为暂停服务时
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    Log.i(TAG, "当前GPS状态为暂停服务状态");
                    break;
            }
        }

        /**
         * GPS开启时触发
         */
        public void onProviderEnabled(String provider) {
            Location location = lm.getLastKnownLocation(provider);
            updateLocation(location, "onProviderEnabled");
        }

        /**
         * GPS禁用时触发
         */
        public void onProviderDisabled(String provider) {
                FormatUtils.fillCommonArgs(mErrDeviceNote);
                mErrDeviceNote.setErrInfo("gps设备被主动关闭");
            for(GpsListener listener : getListeners()) {
                listener.onGpsUpdate(mErrDeviceNote);
            }
        }
    }

    // 状态监听
    GpsStatus.Listener gpsStatusListener = new GpsStatus.Listener() {
        public void onGpsStatusChanged(int event) {
            switch (event) {
                // 第一次定位
                case GpsStatus.GPS_EVENT_FIRST_FIX:
                    Log.i(TAG, "第一次定位");
                    break;
                // 卫星状态改变
                case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                    Log.i(TAG, "卫星状态改变");
                    // 获取当前状态
                    GpsStatus gpsStatus = lm.getGpsStatus(null);
                    // 获取卫星颗数的默认最大值
                    int maxSatellites = gpsStatus.getMaxSatellites();
                    // 创建一个迭代器保存所有卫星
                    Iterator<GpsSatellite> iters = gpsStatus.getSatellites()
                            .iterator();
                    int count = 0;
                    while (iters.hasNext() && count <= maxSatellites) {
                        GpsSatellite s = iters.next();
                        count++;
                    }
                    Log.i(TAG, "搜索到：" + count + "颗卫星");
                    break;
                // 定位启动
                case GpsStatus.GPS_EVENT_STARTED:
                    Log.i(TAG, "定位启动");
                    break;
                // 定位结束
                case GpsStatus.GPS_EVENT_STOPPED:
                    Log.i(TAG, "定位结束");
                    break;
            }
        }
    };

    /**
     * 实时更新文本内容
     *
     * @param location
     */
    private void updateLocation(Location location, String debugMsg) {
        if (location != null) {
            GpsResponse note = new GpsResponse();
            FormatUtils.fillCommonArgs(note);
            note.setGpsGear(mGpsGear);
            note.setDebugMsg(debugMsg);
            note.setLocation(location);
            for(GpsListener listener : getListeners()) {
                listener.onGpsUpdate(note);
            }
            LastLocation = location;
        }
    }

    public Location getLastKnownLocation() throws SecurityException{
        // 为获取地理位置信息时设置查询条件
//        String bestProvider = lm.getBestProvider(getCriteria(), true);
        Location location;
        // 获取位置信息
        // 如果不设置查询要求，getLastKnownLocation方法传入的参数为LocationManager.GPS_PROVIDER
//        location = lm.getLastKnownLocation(bestProvider);
        location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if(location == null){
            location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
        return location;
    }

    private Collection<GpsListener> getListeners(){
        mListeners.clear();
        mListeners.addAll(mHardListeners);
        synchronized (mListenerReferences) {
            Iterator<WeakReference<GpsListener>> iterator = mListenerReferences.iterator();
            while (iterator.hasNext()) {
                WeakReference<GpsListener> weakReference = iterator.next();
                GpsListener listener;
                if (weakReference != null && (listener = weakReference.get()) != null) {
                    mListeners.add(listener);
                } else {
                    iterator.remove();
                }
            }
        }
        return mListeners;
    }

    /**
     * 返回查询条件
     *
     * @return
     */
    private Criteria getCriteria() {
        Criteria criteria = new Criteria();
        // 设置定位精确度 Criteria.ACCURACY_COARSE比较粗略，Criteria.ACCURACY_FINE则比较精细
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        // 设置是否要求速度
        criteria.setSpeedRequired(false);
        // 设置是否允许运营商收费
        criteria.setCostAllowed(true);
        // 设置是否需要方位信息
        criteria.setBearingRequired(false);
        // 设置是否需要海拔信息
        criteria.setAltitudeRequired(false);
        // 设置对电源的需求
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        return criteria;
    }
}
