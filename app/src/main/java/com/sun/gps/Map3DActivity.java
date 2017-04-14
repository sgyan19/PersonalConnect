package com.sun.gps;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.sun.account.Account;
import com.sun.personalconnect.Application;
import com.sun.personalconnect.BaseActivity;
import com.sun.personalconnect.R;
import com.sun.utils.Utils;

import java.util.HashMap;

/**
 * Created by guoyao on 2017/4/13.
 */
public class Map3DActivity extends BaseActivity implements GpsListener,LocationSource {
    private GpsService.ServiceBinder mGpsServiceBinder;
    private ServiceConnection mGpsServiceConn;
    private MapView mMapView;
    private AMap aMap;
    private boolean MoveIt = true;
    private OnLocationChangedListener mListener;

    private int updateIndex = 1;
    private TextView mTxtIndex;
    private TextView mTxtLongitude;
    private TextView mTxtLatitude;
    private TextView mTxtAltitude;
    private TextView mTxtTime;
    private TextView mTxtUser;
    private TextView mTxtDevice;
    private TextView mTxtErr;
    private TextView mTxtSource;

    private HashMap<String,Marker> markers = new HashMap<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_3d);
        mMapView = (MapView) findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);
        aMap = mMapView.getMap();
        aMap.setTrafficEnabled(true);
        aMap.setLocationSource(this);
        mGpsServiceConn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                mGpsServiceBinder = (GpsService.ServiceBinder) iBinder;
                mGpsServiceBinder.setGpsListener(Map3DActivity.this);
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                mGpsServiceBinder = null;
            }
        };
        bindService(new Intent(Map3DActivity.this, GpsService.class), mGpsServiceConn, BIND_AUTO_CREATE);
//

        mTxtIndex = (TextView) findViewById(R.id.txt_gps_index);
        mTxtLongitude = (TextView)findViewById(R.id.txt_gps_longitude);
        mTxtLatitude = (TextView)findViewById(R.id.txt_gps_latitude);
        mTxtAltitude = (TextView)findViewById(R.id.txt_gps_altitude);
        mTxtTime = (TextView)findViewById(R.id.txt_gps_time);
        mTxtUser = (TextView) findViewById(R.id.txt_gps_user);
        mTxtDevice = (TextView) findViewById(R.id.txt_gps_device);
        mTxtErr = (TextView) findViewById(R.id.txt_gps_err);
        mTxtSource = (TextView)findViewById(R.id.txt_gps_source);

        findViewById(R.id.btn_gps_get_local).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mGpsServiceBinder != null){
                    mGpsServiceBinder.requestGps(Map3DActivity.this, GpsService.WHO_MINE, GpsGear.Normal);
                }
            }
        });findViewById(R.id.btn_gps_get_remote).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mGpsServiceBinder != null){
                    mGpsServiceBinder.requestGps(Map3DActivity.this, GpsService.WHO_USER, GpsGear.Low);
                }
            }
        });
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mMapView.onSaveInstanceState(outState);
    }
    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mGpsServiceConn != null) {
            unbindService(mGpsServiceConn);
        }
        mMapView.onDestroy();
    }

    @Override
    public void onGpsUpdate(GpsResponse gpsResponse) {
        if(gpsResponse == null) return;

        mTxtIndex.setText(String.format("index:%d", updateIndex++));
        mTxtUser.setText(String.format("用户:%s", gpsResponse.getUserName()));
        mTxtDevice.setText(String.format("设备:%s",gpsResponse.getDevice()));
        mTxtErr.setText(String.format("错误：%s", gpsResponse.getErrInfo()));
        Location l = gpsResponse.getLocation();
        mTxtLongitude.setText(String.format("经度:%f" , l.getLongitude()));
        mTxtLatitude.setText(String.format("纬度:%f" , l.getLatitude()));
        mTxtAltitude.setText(String.format("海拔:%f" , l.getAltitude()));
        mTxtSource.setText(String.format("来源:%s %s", l.getProvider(), gpsResponse.getDebugMsg()));
        mTxtTime.setText(String.format("时间:%s", Utils.getFormatTime(l.getTime())));
        Location gaoDeLocation = GPSUtil.gps84_To_Gcj02(l);

        if(gpsResponse.getUserId() == Application.App.getAccount().getLoginId() && mListener != null){
            mListener.onLocationChanged(l);
        }else{
            Marker marker = markers.get(gpsResponse.getDevice());
            if(marker == null){
                marker = aMap.addMarker(
                        new MarkerOptions().position(
                                new LatLng(gaoDeLocation.getLatitude(),gaoDeLocation.getLongitude()))
                                .title(gpsResponse.getUserName())
                                .draggable(false)
                                .snippet(String.format("%s\n%s", gpsResponse.getDevice(), Utils.getFormatTime(l.getTime()))));
                markers.put(gpsResponse.getDevice(), marker);
            }else {
                marker.setPosition(new LatLng(gaoDeLocation.getLatitude(), gaoDeLocation.getLongitude()));
                marker.setSnippet(
                        String.format("%s\n%s", gpsResponse.getDevice(), Utils.getFormatTime(l.getTime())));
            }
        }
    }

    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        mListener = onLocationChangedListener;
    }

    @Override
    public void deactivate() {
        mListener = null;
    }
}
