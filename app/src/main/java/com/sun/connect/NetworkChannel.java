package com.sun.connect;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;

import com.sun.personalconnect.Application;
import com.sun.utils.FileUtils;
import com.sun.utils.FormatUtils;
import com.sun.utils.GsonUtils;
import com.sun.utils.IdUtils;
import com.sun.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import de.greenrobot.event.EventBus;

/**
 * Created by guoyao on 2017/4/21.
 */
public class NetworkChannel {
    private ServiceConnection mSocketConn;
    private ISocketServiceBinder mSocketBinder;
    private HashMap<String,String> mWaitSend;
    private HashMap<String,String> mWaitSendRaw;
    private static HashSet<String> mRequestHistory = new HashSet<>();
    private Context mContext;

    private INetworkListener listener;

    public interface INetworkListener{
        void onEventNetwork(EventNetwork network);
    }

    public void setNetworkListener(INetworkListener l){
        listener = l;
    }

    public static NetworkChannel getInstance(){
        return Application.App.getNetworkService();
    }

    public NetworkChannel(){
        mWaitSend = new HashMap<>();
        mWaitSendRaw = new HashMap<>();
        mSocketConn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                mSocketBinder = ISocketServiceBinder.Stub.asInterface(iBinder);
                HashMap<String, String> tmp = new HashMap<>(mWaitSend);
                for(Map.Entry<String, String> entry : tmp.entrySet()){
                    request(entry.getKey(), entry.getValue());
                }
                tmp = new HashMap<>(mWaitSendRaw);
                for(Map.Entry<String, String> entry : tmp.entrySet()){
                    uploadRaw(entry.getKey(), entry.getValue());
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                mSocketBinder = null;
            }
        };
    }

    public void init(Context context){
        mContext = context;
        Application.App.bindService(new Intent(context, SocketService.class), mSocketConn, Context.BIND_AUTO_CREATE);
        SocketReceiver.register(context, mReceiveListener);
    }

    public void release(){
        Application.App.unbindService(mSocketConn);
        SocketReceiver.unregister(mContext);
    }

    private SocketReceiver.SocketReceiveListener mReceiveListener = new SocketReceiver.SocketReceiveListener() {
        EventNetwork mEventNetwork = new EventNetwork();

        @Override
        public boolean onReconnected(boolean connected) {
            mEventNetwork.reset();
            if(listener != null){
                listener.onEventNetwork(mEventNetwork);
            }else{
                EventBus.getDefault().post(mEventNetwork);
            }
            return false;
        }

        @Override
        public boolean onError(String key, String error) {
            mEventNetwork.reset();
            if(mRequestHistory.contains(key)){
                mEventNetwork.setMine(true);
            }
            mEventNetwork.setError(error);
            mEventNetwork.setKey(key);
            mEventNetwork.setStep(1);
            if(listener != null){
                listener.onEventNetwork(mEventNetwork);
            }else{
                EventBus.getDefault().post(mEventNetwork);
            }
            return false;
        }

        @Override
        public boolean onParserResponse(String key, ResponseJson json, String info) {
            mEventNetwork.reset();
            if(mRequestHistory.contains(key)){
                mEventNetwork.setMine(true);
                if(!TextUtils.isEmpty(info)) {
                    mEventNetwork.setError(info);
                    mEventNetwork.setKey(key);
                    mEventNetwork.setResponse(json);
                    mEventNetwork.setStep(2);
                    if(listener != null){
                        listener.onEventNetwork(mEventNetwork);
                    }else{
                        EventBus.getDefault().post(mEventNetwork);
                    }
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean onReceiveFile(String key, File file, String info) {
            if(mRequestHistory.contains(key)){
                mEventNetwork.setMine(true);
            }
            mEventNetwork.setError(info);
            mEventNetwork.setKey(key);
            mEventNetwork.setObject(file);
            mEventNetwork.setStep(3);
            if(listener != null){
                listener.onEventNetwork(mEventNetwork);
            }else{
                EventBus.getDefault().post(mEventNetwork);
            }
            return false;
        }

        @Override
        public boolean onParserData(String key, ResponseJson json, Object data, String info) {
            if(mRequestHistory.contains(key)){
                mEventNetwork.setMine(true);
            }
            mEventNetwork.setError(info);
            mEventNetwork.setKey(key);
            mEventNetwork.setObject(data);
            mEventNetwork.setStep(3);
            if(listener != null){
                listener.onEventNetwork(mEventNetwork);
            }else{
                EventBus.getDefault().post(mEventNetwork);
            }
            return false;
        }

        @Override
        public void onOver(String key) {
            mRequestHistory.remove(key);
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

    public void uploadRaw(String key, String request){
        boolean send = false;
        if(mSocketBinder != null){
            try {
                mSocketBinder.request(key, SocketMessage.SOCKET_TYPE_RAW, request);
                send = true;
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        if(send){
            mWaitSendRaw.remove(key);
            mRequestHistory.add(key);
        }else{
            mWaitSendRaw.put(key,request);
        }
    }

    public void download(String key, String fileName){
        boolean send = false;
        if(mSocketBinder != null){
            try {
                mSocketBinder.request(key, SocketMessage.SOCKET_TYPE_JSON_DOWNLOAD_RAW, fileName);
                send = true;
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        if(send){
            mRequestHistory.add(key);
        }
    }

    public String request(RequestJson json){
        String key = json.getRequestId();
        String request = GsonUtils.mGson.toJson(json);
        request(key,request);
        return key;
    }

    public String upload(File file){
        if(!file.exists()) return null;
        File newFile = new File(Application.App.getSocketRawFolder(), file.getName());
        newFile.delete();
        try {
            FileUtils.copyFile(file, newFile);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        String key = IdUtils.make();
        uploadRaw(key, newFile.getName());
        return key;
    }

    public String uploadAndMakeCvs(File file){
        if(!file.exists()) return null;
        File newFile = new File(Application.App.getSocketRawFolder(), file.getName());
        newFile.delete();
        try {
            FileUtils.copyFile(file, newFile);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        String key = IdUtils.make();
        uploadRaw(key, newFile.getName());
        return key;
    }

    public String download(RequestJson json){
        String key = json.getRequestId();
        String request = GsonUtils.mGson.toJson(json);
        download(key,request);
        return key;
    }

    public boolean isServiceAlive(){
        return mSocketBinder != null;
    }
}
