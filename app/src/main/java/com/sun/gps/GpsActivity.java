package com.sun.gps;

import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.sun.personalconnect.Application;
import com.sun.personalconnect.BaseActivity;
import com.sun.personalconnect.R;
import com.sun.utils.FormatUtils;
import com.sun.utils.Utils;

/**
 * Created by guoyao on 2017/3/6.
 */
public class GpsActivity extends BaseActivity implements GpsListener{

    private static final String TAG = "GpsActivity";
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gps);

        mTxtIndex = (TextView) findViewById(R.id.txt_gps_index);
        mTxtLongitude = (TextView)findViewById(R.id.txt_gps_longitude);
        mTxtLatitude = (TextView)findViewById(R.id.txt_gps_latitude);
        mTxtAltitude = (TextView)findViewById(R.id.txt_gps_altitude);
        mTxtTime = (TextView)findViewById(R.id.txt_gps_time);
        mTxtUser = (TextView) findViewById(R.id.txt_gps_user);
        mTxtDevice = (TextView) findViewById(R.id.txt_gps_device);
        mTxtErr = (TextView) findViewById(R.id.txt_gps_err);
        mTxtSource = (TextView)findViewById(R.id.txt_gps_source);
        Application.App.getModelManager().addGpsWeakListener(this);

        findViewById(R.id.btn_gps_get_local).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    Application.App.getModelManager().setGpsStatus(GpsGear.Once);
            }
        });findViewById(R.id.btn_gps_get_remote).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Application.App.getNetworkService().request(FormatUtils.makeGpsRequest(null,null,GpsGear.Once));
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onGpsUpdate(GpsResponse gpsResponse) {
        if(gpsResponse != null){
            mTxtIndex.setText(String.format("index:%d", updateIndex++));
            mTxtUser.setText(String.format("用户:%s", gpsResponse.getUserName()));
            mTxtDevice.setText(String.format("设备:%s",gpsResponse.getDevice()));
            mTxtErr.setText(String.format("错误：%s", gpsResponse.getErrInfo()));
            Location l = gpsResponse.getLocation();
                mTxtLongitude.setText(String.format("经度:%f", l.getLongitude()));
                mTxtLatitude.setText(String.format("纬度:%f", l.getLatitude()));
                mTxtAltitude.setText(String.format("海拔:%f", l.getAltitude()));
                mTxtTime.setText(String.format("时间:%s", Utils.getFormatTime(l.getTime())));
                mTxtSource.setText(String.format("来源:%s %s", l.getProvider(), gpsResponse.getDebugMsg()));
//            if(gpsResponse.getErrType() == GpsResponse.ERR_TYPE_NONE){
//            }else if(gpsResponse.getErrType() == GpsResponse.ERR_TYPE_DEVICE){
//                ToastUtils.show(String.format("gps 设备未启动， %s", gpsResponse.getErrInfo()), Toast.LENGTH_SHORT);
//            }else if(gpsResponse.getErrType() == GpsResponse.ERR_TYPE_PERMISSION){
//
//            }
        }
    }
}
