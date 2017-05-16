package com.sun.gps;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import com.sun.connect.ISocketServiceBinder;
import com.sun.connect.RequestJson;
import com.sun.connect.ResponseJson;
import com.sun.connect.SocketMessage;
import com.sun.connect.SocketReceiver;
import com.sun.connect.SocketService;
import com.sun.connect.SocketTask;
import com.sun.level.LevelCenter;
import com.sun.personalconnect.BaseActivity;
import com.sun.utils.FormatUtils;
import com.sun.utils.GsonUtils;
import com.sun.utils.StatusFragment;

import java.io.File;
import java.util.LinkedHashMap;

/**
 * Created by guoyao on 2017/2/16.
 */
public class GpsService extends Service {
    public static final String TAG = "GpsService";
    public static final int WHO_MINE = 0;
    public static final int WHO_USER = 1;
    public static final int WHO_ALL = 2;
    private Gps mGpsCore;
    private GpsListener mOutGpsListener;
    private ServiceBinder mBinder;
    private ISocketServiceBinder mSocketBinder;
    private ServiceConnection mSocketConn;
    private LinkedHashMap<String, GpsNote> mRequestHistory = new LinkedHashMap<>();


    private GpsListener mInGpsListener = new GpsListener() {
        @Override
        public void onGpsUpdate(GpsResponse gpsResponse) {
            RequestJson requestJson = FormatUtils.makeGpsReponseRequest(gpsResponse);
            mRequestHistory.put(requestJson.getRequestId(),gpsResponse);
            if(mSocketBinder != null){
                try {
                    mSocketBinder.request(requestJson.getRequestId(), SocketMessage.SOCKET_TYPE_JSON, GsonUtils.mGson.toJson(requestJson));
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }else{
                mReceiveListener.onError(requestJson.getRequestId(), "local service not bind");
            }
        }
    };

    public class ServiceBinder extends Binder{
        public void setGpsListener(GpsListener gpsListener){
            mOutGpsListener = gpsListener;
        }

        public void clearListener(GpsListener gpsListener){
            if(mGpsCore != null){
                mGpsCore.clearListener(gpsListener);
            }
            mOutGpsListener = null;
        }
        public void requestGps(BaseActivity activity, int who, GpsGear gpsGear){
//            if(arg)
            if(who == WHO_MINE){
                if(mGpsCore == null){
                    mGpsCore = new Gps();
                }
                if(mOutGpsListener != null){
                    mGpsCore.setGpsListener(mOutGpsListener);
                        mGpsCore.requestUpdate(activity, gpsGear);
                }
            }else if(who == WHO_USER){
                Object[] objects = FormatUtils.makeGpsRequest(gpsGear);
                RequestJson requestJson = (RequestJson)objects[0];
                GpsRequest note = (GpsRequest)objects[1];
                if(note != null)
                    mRequestHistory.put(requestJson.getRequestId(), note);
                if(mSocketBinder != null){
                    try {
                        mSocketBinder.request(requestJson.getRequestId(), SocketMessage.SOCKET_TYPE_JSON, GsonUtils.mGson.toJson(requestJson));
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }else{
                    mReceiveListener.onError(requestJson.getRequestId(), "local service not bind");
                }
            }
        }

        public void stopGpsUpdate(int who){
            if(who == WHO_MINE) {
                if (mGpsCore != null) {
                    mGpsCore.stopUpdate();
                }
            }else if(who == WHO_USER){
                Object[] objects = FormatUtils.makeGpsRequest(GpsGear.None);
                RequestJson requestJson = (RequestJson)objects[0];
                GpsRequest note = (GpsRequest)objects[1];
                if(note != null)
                    mRequestHistory.put(requestJson.getRequestId(), note);
                if(mSocketBinder != null){
                    try {
                        mSocketBinder.request(requestJson.getRequestId(), SocketMessage.SOCKET_TYPE_JSON, GsonUtils.mGson.toJson(requestJson));
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }else{
                    mReceiveListener.onError(requestJson.getRequestId(), "local service not bind");
                }
            }
        }

        public Location getLastLocation(){
            if(mGpsCore != null){
                return mGpsCore.getLastKnownLocation();
            }
            return null;
        }
    }

    private SocketReceiver.SocketReceiveListener mReceiveListener = new SocketReceiver.SocketReceiveListener(){

        @Override
        public boolean onReconnected(boolean connected) {
            return false;
        }

        @Override
        public boolean onError(String key, String error) {
            if(mRequestHistory.containsKey(key)){
                GpsNote note = mRequestHistory.get(key);
                mRequestHistory.remove(key);
//                StatusFragment.addMessage(String.format("gps request error,id:%d,gear:%s", note.getId(), note.getGpsGear()));
                return true;
            }else {
                return false;
            }
        }

        @Override
        public boolean onParserResponse(String key, ResponseJson json, String info) {
            return false;
        }

        @Override
        public boolean onReceiveFile(String key, File file, String info) {
            return false;
        }

        @Override
        public boolean onParserData(String key, ResponseJson json, Object data, String info) {
            if(mRequestHistory.containsKey(key)){
                GpsNote note = mRequestHistory.get(key);
                Log.d(TAG, String.format("request back key:%s,note:%d", key, note.getId()));
                mRequestHistory.remove(key);
//                StatusFragment.addMessage(String.format("gps request success,id:%d", note.getId()));
                return true;
            }

            if(data != null) {
                if (data instanceof GpsNote) {
                    if (data instanceof GpsRequest) {
                        GpsRequest gpsRequest = (GpsRequest) data;
                        if(LevelCenter.serverCheck(gpsRequest.getUserId())){
                            Log.d(TAG, "server gps request");
                            mOutGpsListener = mInGpsListener;
                            mBinder.requestGps(BaseActivity.getAnyInstance(), WHO_MINE, gpsRequest.getGpsGear());
                        }
                    } else if (data instanceof GpsResponse) {
                        if(mOutGpsListener != null){
                            mOutGpsListener.onGpsUpdate((GpsResponse) data);
                        }
                    } else {
                        Log.e(TAG, "GPS解析遇到未料想的class:" + data.getClass());
                    }
                    return true;
                }
            }
            return false;
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        SocketReceiver.register(this, mReceiveListener);
        mBinder = new ServiceBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(mSocketBinder == null) {
            mSocketConn = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                    mSocketBinder = ISocketServiceBinder.Stub.asInterface(iBinder);
                }

                @Override
                public void onServiceDisconnected(ComponentName componentName) {
                    mSocketBinder = null;
                }
            };
            bindService(new Intent(GpsService.this, SocketService.class), mSocketConn, BIND_AUTO_CREATE);
        }
        return Service. START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SocketReceiver.unregister(this);
        if(mGpsCore != null){
            mGpsCore.stopUpdate();
        }
        if(mSocketConn != null){
            unbindService(mSocketConn);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
}
