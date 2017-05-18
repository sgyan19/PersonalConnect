package com.sun.connect;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;

import com.sun.personalconnect.Application;
import com.sun.utils.GsonUtils;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import de.greenrobot.event.EventBus;

/**
 * Created by guoyao on 2017/4/21.
 */
public class AppLifeNetworkService {
    private ServiceConnection mSocketConn;
    private ISocketServiceBinder mSocketBinder;
    private HashMap<String,String> mWaitSend;
    private HashSet<String> mRequestHistory;

    public static AppLifeNetworkService getInstance(){
        return Application.App.getNetworkService();
    }

    public AppLifeNetworkService(){
        mWaitSend = new HashMap<>();
        mRequestHistory = new HashSet<>();
        mSocketConn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                mSocketBinder = ISocketServiceBinder.Stub.asInterface(iBinder);
                HashMap<String, String> tmp = new HashMap<>(mWaitSend);
                for(Map.Entry<String, String> entry : tmp.entrySet()){
                    request(entry.getKey(), entry.getValue());
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                mSocketBinder = null;
            }
        };
    }

    public void init(){
        Application.App.bindService(new Intent(Application.App, SocketService.class), mSocketConn, Context.BIND_AUTO_CREATE);
        SocketReceiver.register(Application.App, mReceiveListener);
    }

    public void release(){
        Application.App.unbindService(mSocketConn);
        SocketReceiver.unregister(Application.App);
    }

    private SocketReceiver.SocketReceiveListener mReceiveListener = new SocketReceiver.SocketReceiveListener() {
        EventNetwork mEventNetwork = new EventNetwork();

        @Override
        public boolean onReconnected(boolean connected) {
            mEventNetwork.reset();
            EventBus.getDefault().post(mEventNetwork);
            return false;
        }

        @Override
        public boolean onError(String key, String error) {
            mEventNetwork.reset();
            if(mRequestHistory.remove(key)){
                mEventNetwork.setMine(true);
            }
            mEventNetwork.setError(error);
            mEventNetwork.setKey(key);
            mEventNetwork.setStep(1);
            EventBus.getDefault().post(mEventNetwork);
            return false;
        }

        @Override
        public boolean onParserResponse(String key, ResponseJson json, String info) {
            mEventNetwork.reset();
            if(mRequestHistory.remove(key)){
                mEventNetwork.setMine(true);
                if(!TextUtils.isEmpty(info)) {
                    mEventNetwork.setError(info);
                    mEventNetwork.setKey(key);
                    mEventNetwork.setResponse(json);
                    mEventNetwork.setStep(2);
                    EventBus.getDefault().post(mEventNetwork);
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean onReceiveFile(String key, File file, String info) {
            if(mRequestHistory.remove(key)){
                mEventNetwork.setMine(true);
            }
            mEventNetwork.setError(info);
            mEventNetwork.setKey(key);
            mEventNetwork.setObject(file);
            mEventNetwork.setStep(3);
            EventBus.getDefault().post(mEventNetwork);
            return false;
        }

        @Override
        public boolean onParserData(String key, ResponseJson json, Object data, String info) {
            if(mRequestHistory.remove(key)){
                mEventNetwork.setMine(true);
            }
            mEventNetwork.setError(info);
            mEventNetwork.setKey(key);
            mEventNetwork.setObject(data);
            mEventNetwork.setStep(3);
            EventBus.getDefault().post(mEventNetwork);
            return false;
        }
    };

    public void request(String key, String request){
        boolean send = false;
        if(mSocketBinder != null){
            try {
                mSocketBinder.request(key, SocketMessage.SOCKET_TYPE_JSON, request);
                send = true;
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        if(send){
            mWaitSend.remove(key);
            mRequestHistory.add(key);
        }else{
            mWaitSend.put(key,request);
        }
    }

    public String request(RequestJson json){
        String key = json.getRequestId();
        String request = GsonUtils.mGson.toJson(json);
        request(key,request);
        return key;
    }

    public boolean isServiceAlive(){
        return mSocketBinder != null;
    }
}
