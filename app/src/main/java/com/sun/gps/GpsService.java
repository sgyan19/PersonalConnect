package com.sun.gps;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.sun.conversation.CvsService;

import java.lang.ref.WeakReference;

/**
 * Created by guoyao on 2017/2/16.
 */
public class GpsService extends Service {
    private static final int REQ_MINE = 0;
    private static final int REQ_USER = 1;
    private static final int REQ_ALL = 2;
    private Gps mGpsCore;

    public class ServiceBinder extends Binder{
        public void setGpsListener(GpsListener gpsListener){
            mGpsCore.setGpsListener(gpsListener);
        }

        public void clearListener(GpsListener gpsListener){
            mGpsCore.clearListener(gpsListener);
        }
        public void requestGps(int model, String arg){
            if(arg)
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mGpsCore = new Gps();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
