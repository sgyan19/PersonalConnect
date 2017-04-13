package com.sun.gps;

import android.content.ServiceConnection;
import android.os.Bundle;

import com.sun.personalconnect.BaseActivity;
import com.sun.personalconnect.R;

/**
 * Created by guoyao on 2017/4/13.
 */
public class MapActivity extends BaseActivity {
    private GpsService.ServiceBinder mGpsServiceBinder;
    private ServiceConnection mGpsServiceConn;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
    }
}
