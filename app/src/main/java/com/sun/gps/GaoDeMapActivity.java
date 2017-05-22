package com.sun.gps;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdate;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.LocationSource;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.CameraPosition;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.maps2d.model.MyLocationStyle;
import com.sun.connect.EventNetwork;
import com.sun.personalconnect.Application;
import com.sun.personalconnect.BaseActivity;
import com.sun.personalconnect.R;
import com.sun.utils.FormatUtils;
import com.sun.utils.StatusFragment;
import com.sun.utils.Utils;

import java.util.HashMap;

import de.greenrobot.event.EventBus;

/**
 * Created by guoyao on 2017/4/13.
 */
public class GaoDeMapActivity extends BaseActivity implements GpsListener,LocationSource {
    private MapView mMapView;
    private AMap aMap;
    private boolean MoveIt = true;
    private OnLocationChangedListener mListener;

    private int updateIndex = 1;
    private ViewGroup mDebugView;
    private TextView mTxtIndex;
    private TextView mTxtLongitude;
    private TextView mTxtLatitude;
    private TextView mTxtAltitude;
    private TextView mTxtTime;
    private TextView mTxtUser;
    private TextView mTxtDevice;
    private TextView mTxtErr;
    private TextView mTxtSource;

    private ViewGroup mDebugView2;

    private HashMap<String,Marker> markers = new HashMap<>();
    private HashMap<String, LocationTrajectory> mUserTrajectory = new HashMap<>();

    private StatusFragment mStatusFragment;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        mMapView = (MapView) findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);
        aMap = mMapView.getMap();
        aMap.setTrafficEnabled(false);
        aMap.setLocationSource(this);

        findViewById(R.id.view_gps_hide).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDebugView.setVisibility(mDebugView.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
            }
        });
        findViewById(R.id.view_gps_hide_2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDebugView2.setVisibility(mDebugView2.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
            }
        });
        mDebugView = (ViewGroup)findViewById(R.id.container_gps_debug);
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
        });
        findViewById(R.id.btn_gps_get_remote).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            Application.App.getNetworkService().request(FormatUtils.makeGpsRequest(null,null,GpsGear.Once));
            }
        });

        mDebugView2 = (ViewGroup)findViewById(R.id.container_gps_debug_2);
        mStatusFragment = new StatusFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.container_gps_debug_2, mStatusFragment).commit();

        MyLocationStyle myLocationStyle;
        myLocationStyle = new MyLocationStyle();//初始化定位蓝点样式类
        aMap.setMyLocationStyle(myLocationStyle);//设置定位蓝点的Style
        //aMap.getUiSettings().setMyLocationButtonEnabled(true);设置默认定位按钮是否显示，非必需设置。
        aMap.setMyLocationEnabled(true);


        CameraPosition position = aMap.getCameraPosition();
        CameraPosition newPosition = new CameraPosition(
                new LatLng(31.22f, 121.48F),
                position.zoom,
                position.tilt,
                position.bearing
        );
        CameraUpdate mCameraUpdate = CameraUpdateFactory.newCameraPosition(
                newPosition);
        aMap.moveCamera(mCameraUpdate);

        Application.App.getModelManager().setGpsStatus(GpsGear.Low);
        Application.App.getNetworkService().request(FormatUtils.makeGpsRequest(null,null,GpsGear.Low));

        EventBus.getDefault().register(this);
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
        EventBus.getDefault().unregister(this);
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onGpsUpdate(GpsResponse gpsResponse) {
        if(gpsResponse == null) return;
        Location l = gpsResponse.getLocation();

        LocationTrajectory trajectory = mUserTrajectory.get(gpsResponse.getUserName());
        if(trajectory == null){
            trajectory = new LocationTrajectory();
            mUserTrajectory.put(gpsResponse.getUserName(), trajectory);
        }

        if(!trajectory.update(l)){
            return;
        }

        mStatusFragment.addMessage(String.format("%s,%s,%s,%s", gpsResponse.getUserName(), Utils.getFormatTime(l.getTime()), l.getProvider(), gpsResponse.getDevice()));

        mTxtIndex.setText(String.format("index:%d", updateIndex++));
        mTxtUser.setText(String.format("用户:%s", gpsResponse.getUserName()));
        mTxtDevice.setText(String.format("设备:%s",gpsResponse.getDevice()));
        mTxtErr.setText(String.format("错误：%s", gpsResponse.getErrInfo()));
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

    public void onEvent(EventNetwork eventNetwork){
        Object obj = eventNetwork.getObject();
        if(obj != null && obj instanceof GpsResponse){
            onGpsUpdate((GpsResponse)obj);
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
