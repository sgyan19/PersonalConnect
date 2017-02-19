package com.sun.gps;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.sun.conversation.CvsService;
import com.sun.personalconnect.Application;
import com.sun.personalconnect.BaseActivity;
import com.sun.personalconnect.Permission;

import java.lang.ref.WeakReference;
import java.util.Iterator;

/**
 * Created by guoyao on 2017/2/14.
 */
public class Gps {
    private LocationManager lm;
    private static final String TAG = "Gps";
    private Context mContext;

    public static final int STAT_NONE = 0;
    public static final int STAT_SINGLE_UPDATING = 1;
    public static final int STAT_SINGLE_UPDATED = 2;
    public static final int STAT_UPDATING = 3;
    private int mStatus;

    private static final String ERROR_PREMISSION = "未获取到权限";
    private static final String ERROR_GPS_DISABLE = "GPS关闭状态";

    private WeakReference<GpsListener> mGpsListenerReference;

    public void setGpsListener(GpsListener gpsListener) {
        mGpsListenerReference = new WeakReference<>(gpsListener);
    }

    public void clearListener(GpsListener gpsListener) {
        if (mGpsListenerReference != null && mGpsListenerReference.get() == gpsListener) {
            mGpsListenerReference.clear();
            mGpsListenerReference = null;
        }
    }

    public Gps() {
        mContext = Application.App.getApplicationContext();
        lm = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
    }

    public void requestOnce(BaseActivity activity) {
        activity.requestPermission(new Permission(Manifest.permission.ACCESS_FINE_LOCATION, new Permission.Runnable() {
            @Override
            public void run(Permission p) {
                if (p.isSuccess()) {

                } else {
                    GpsListener listener;
                    if (mGpsListenerReference != null && (listener = mGpsListenerReference.get()) != null) {
                        listener.onNonePermission();
                    }
                }
            }
        }));
    }

    private Permission.Runnable mRequestOnce = new Permission.Runnable() {
        @Override
        public void run(Permission permission) {
            if (permission.isSuccess()) {
                try {
                    lm.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener, Looper.getMainLooper());
                }catch (SecurityException e){
                    e.printStackTrace();
                }
            }else{
                GpsListener listener ;
                if(mGpsListenerReference !=null && (listener =  mGpsListenerReference.get()) != null){
                    listener.onNonePermission();
                }
            }
        }
    };

    public void check(){
        // 判断GPS是否正常启动
        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Log.d(TAG, "GPS 未开启");
            // 返回开启GPS导航设置界面
//            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//            startActivityForResult(intent, 0);
        }else{
        }
    }

    public void release() {
        // TODO Auto-generated method stub
        try {
            lm.removeUpdates(locationListener);
        }catch (SecurityException e){
            e.printStackTrace();
        }
    }

    public boolean start() {

        // 判断GPS是否正常启动
        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Log.d(TAG, "GPS 未开启");
            // 返回开启GPS导航设置界面
//            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//            startActivityForResult(intent, 0);
            return false;
        }
        try {
            Location location = getLastKnownLocation();
            updateView(location);
            // 监听状态
            lm.addGpsStatusListener(listener);
            // 绑定监听，有4个参数
            // 参数1，设备：有GPS_PROVIDER和NETWORK_PROVIDER两种
            // 参数2，位置信息更新周期，单位毫秒
            // 参数3，位置变化最小距离：当位置距离变化超过此值时，将更新位置信息
            // 参数4，监听
            // 备注：参数2和3，如果参数3不为0，则以参数3为准；参数3为0，则通过时间来定时更新；两者为0，则随时刷新

            // 1秒更新一次，或最小位移变化超过1米更新一次；
            // 注意：此处更新准确度非常低，推荐在service里面启动一个Thread，在run中sleep(10000);然后执行handler.sendMessage(),更新位置
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locationListener);
        }catch (SecurityException e){
            e.printStackTrace();
        }
        return true;
    }

    // 位置监听
    private LocationListener locationListener = new LocationListener() {

        /**
         * 位置信息变化时触发
         */
        public void onLocationChanged(Location location) {
            updateView(location);
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
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            Location location = lm.getLastKnownLocation(provider);
            updateView(location);
        }

        /**
         * GPS禁用时触发
         */
        public void onProviderDisabled(String provider) {
            updateView(null);
        }

    };

    // 状态监听
    GpsStatus.Listener listener = new GpsStatus.Listener() {
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
//                        GpsSatellite s = iters.next();
                        count++;
                    }
                    System.out.println("搜索到：" + count + "颗卫星");
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
    private void updateView(Location location) {
        if (location != null) {
//            editText.setText("设备位置信息\n\n经度：");
//            editText.append(String.valueOf(location.getLongitude()));
//            editText.append("\n纬度：");
//            editText.append(String.valueOf(location.getLatitude()));
        } else {
            // 清空EditText对象
//            editText.getEditableText().clear();
        }
    }

    public Location getLastKnownLocation() throws SecurityException{
        // 为获取地理位置信息时设置查询条件
        String bestProvider = lm.getBestProvider(getCriteria(), true);
        Location location;
        // 获取位置信息
        // 如果不设置查询要求，getLastKnownLocation方法传入的参数为LocationManager.GPS_PROVIDER
        location = lm.getLastKnownLocation(bestProvider);
        return location;
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
        criteria.setCostAllowed(false);
        // 设置是否需要方位信息
        criteria.setBearingRequired(false);
        // 设置是否需要海拔信息
        criteria.setAltitudeRequired(false);
        // 设置对电源的需求
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        return criteria;
    }
}
