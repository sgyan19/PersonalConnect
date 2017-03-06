package com.sun.gps;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.sun.connect.ResponseJson;
import com.sun.connect.SocketReceiver;
import com.sun.conversation.CvsService;
import com.sun.personalconnect.BaseActivity;

import java.io.File;
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
        public void requestGps(BaseActivity activity, int model, String arg){
//            if(arg)
            if(model == REQ_MINE){
                mGpsCore.requestOnce(activity);
            }
        }
    }

    private SocketReceiver.SocketReceiveListener mReceiveListener = new SocketReceiver.SocketReceiveListener(){

        @Override
        public boolean onReconnected(boolean connected) {
            return false;
        }

        @Override
        public boolean onError(int key, String error) {
            return false;
        }

        @Override
        public boolean onParserResponse(int key, ResponseJson json, String info) {
            return false;
        }

        @Override
        public boolean onReceiveFile(int key, File file, String info) {
            return false;
        }

        @Override
        public boolean onParserData(int key, ResponseJson json, Object data, String info) {
            return false;
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        mGpsCore = new Gps();
        SocketReceiver.register(this, mReceiveListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SocketReceiver.unregister(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
