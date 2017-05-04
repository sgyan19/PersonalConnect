package com.sun.connect;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import com.sun.personalconnect.Application;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import de.greenrobot.event.EventBus;

/**
 * Created by guoyao on 2017/4/21.
 */
public class AppLifeNetworkService {
    private ServiceConnection mSocketConn;
    private ISocketServiceBinder mSocketBinder;
    private HashMap<Integer,String> mWaitSend;

    public static AppLifeNetworkService getInstance(){
        return Application.App.getNetworkService();
    }

    public AppLifeNetworkService(){
        mWaitSend = new HashMap<>();
        mSocketConn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                mSocketBinder = ISocketServiceBinder.Stub.asInterface(iBinder);
                HashMap<Integer, String> tmp = new HashMap<>(mWaitSend);
                for(Map.Entry<Integer, String> entry : tmp.entrySet()){
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
        public boolean onError(int key, String error) {
            mEventNetwork.reset();
            mEventNetwork.setError(error);
            mEventNetwork.setKey(key);
            mEventNetwork.setStep(1);
            EventBus.getDefault().post(mEventNetwork);
            return false;
        }

        @Override
        public boolean onParserResponse(int key, ResponseJson json, String info) {
            mEventNetwork.reset();
            mEventNetwork.setError(info);
            mEventNetwork.setKey(key);
            mEventNetwork.setResponse(json);
            mEventNetwork.setStep(2);
            EventBus.getDefault().post(mEventNetwork);
            return false;
        }

        @Override
        public boolean onReceiveFile(int key, File file, String info) {
            mEventNetwork.reset();
            mEventNetwork.setError(info);
            mEventNetwork.setKey(key);
            mEventNetwork.setObject(file);
            mEventNetwork.setStep(3);
            EventBus.getDefault().post(mEventNetwork);
            return false;
        }

        @Override
        public boolean onParserData(int key, ResponseJson json, Object data, String info) {
            mEventNetwork.reset();
            mEventNetwork.setError(info);
            mEventNetwork.setKey(key);
            mEventNetwork.setObject(data);
            mEventNetwork.setStep(3);
            EventBus.getDefault().post(mEventNetwork);
            return false;
        }
    };

    public void request(int key, String request){
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
        }else{
            mWaitSend.put(key,request);
        }
    }

    public boolean isServiceAlive(){
        return mSocketBinder != null;
    }
}
