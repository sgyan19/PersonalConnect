package com.sun.gps;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by guoyao on 2017/2/16.
 */
public class GpsService extends Service {
    private Callback mCallbck;
    private Gps mGpsCore;
    private interface Callback{

    }

    public class ServiceBinder extends Binder{
        public void setCallback(Callback callback){
            mCallbck = callback;
        }

        public Location getLocation(){
            return null;
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
