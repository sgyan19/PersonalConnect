package com.sun.gps;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.sun.personalconnect.BaseActivity;
import com.sun.personalconnect.R;
import com.sun.utils.ToastUtils;

/**
 * Created by guoyao on 2017/3/6.
 */
public class GpsActivity extends BaseActivity implements GpsListener{

    private static final String TAG = "GpsActivity";

    private TextView mTxtLongitude;
    private TextView mTxtLatitude;
    private TextView mTxtAltitude;

    private GpsService.ServiceBinder mGpsServiceBinder;
    private ServiceConnection mGpsServiceConn;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gps);

        mTxtLongitude = (TextView)findViewById(R.id.txt_gps_longitude);
        mTxtLatitude = (TextView)findViewById(R.id.txt_gps_latitude);
        mTxtAltitude = (TextView)findViewById(R.id.txt_gps_altitude);

        findViewById(R.id.btn_gps_get_local).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mGpsServiceBinder != null){
                    mGpsServiceBinder.requestGps(GpsActivity.this, GpsService.WHO_MINE, GpsGear.Once);
                }
            }
        });findViewById(R.id.btn_gps_get_remote).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mGpsServiceBinder != null){
                    mGpsServiceBinder.requestGps(GpsActivity.this, GpsService.WHO_USER, GpsGear.Once);
                }
            }
        });

        mGpsServiceConn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                mGpsServiceBinder = (GpsService.ServiceBinder) iBinder;
                mGpsServiceBinder.setGpsListener(GpsActivity.this);
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                mGpsServiceBinder = null;
            }
        };
        bindService(new Intent(GpsActivity.this, GpsService.class), mGpsServiceConn, BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mGpsServiceConn != null) {
            unbindService(mGpsServiceConn);
        }
    }

    @Override
    public void onGpsUpdate(GpsResponse gpsResponse) {
        if(gpsResponse != null){
            if(gpsResponse.getErrType() == GpsResponse.ERR_TYPE_NONE){
                mTxtLongitude.setText(String.format("经度:%f" , gpsResponse.getLongitude()));
                mTxtLatitude.setText(String.format("纬度:%f" , gpsResponse.getLatitude()));
                mTxtAltitude.setText(String.format("海拔:%f" , gpsResponse.getAltitude()));
            }else if(gpsResponse.getErrType() == GpsResponse.ERR_TYPE_DEVICE){
                ToastUtils.show(String.format("gps 设备未启动， %s", gpsResponse.getErrInfo()), Toast.LENGTH_SHORT);
            }else if(gpsResponse.getErrType() == GpsResponse.ERR_TYPE_PERMISSION){

            }
        }
    }
}
