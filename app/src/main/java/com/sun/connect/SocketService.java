package com.sun.connect;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.FileObserver;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import com.sun.personalconnect.Application;
import com.sun.utils.FileUtils;

import java.io.File;

/**
 * Created by guoyao on 2016/12/21.
 * this service maybe a remote service.Activity should not touch this.
 */
public class SocketService extends Service {
    public static final String TAG = "SocketService";
    public static final String SocketReceiveBroadcast = "com.sun.connect.SocketService.Receive";
    public static final String KEY_STRING_REQUESTKEY = "requestKey";
    public static final String KEY_INT_RESPONSE_TYPE = "responseType";
    public static final String KEY_STRING_RESPONSE = "response";
    public static final String KEY_STRING_ERROR = "error";
    public static final String KEY_BOOLEAN_CONNECTED= "connected";

    private ServiceBinder mBinder;

    private SocketTask jsonSocketTask;
    private SocketTask rawSocketTask;

    public SocketTask getMainSocketTask(){
        return jsonSocketTask;
    }

    public SocketTask getRawSocketTask(){
        return rawSocketTask;
    }

    private FileObserver mRawDirObserver;

    private SocketCallback mReceiveCallback = new SocketCallback() {
        @Override
        public void onError(String requestKey, Throwable e) {
            Intent intent = new Intent(SocketReceiveBroadcast);
            intent.putExtra(KEY_STRING_REQUESTKEY, requestKey);
            intent.putExtra(KEY_STRING_ERROR, e == null ? "unknown" : e.toString());
            SocketService.this.sendBroadcast(intent);
        }

        @Override
        public void onComplete(String requestKey, SocketMessage response) {
            if(response != null){
                Intent intent = new Intent(SocketReceiveBroadcast);
                intent.putExtra(KEY_STRING_REQUESTKEY, requestKey);
                intent.putExtra(KEY_STRING_RESPONSE, response.data);
                intent.putExtra(KEY_INT_RESPONSE_TYPE, response.type);
                SocketService.this.sendBroadcast(intent);
            }
        }

        @Override
        public void onConnected(String requestKey) {
            Intent intent = new Intent(SocketReceiveBroadcast);
            intent.putExtra(KEY_STRING_REQUESTKEY, SocketTask.REQUEST_KEY_ANYBODY);
            intent.putExtra(KEY_BOOLEAN_CONNECTED, true);
            SocketService.this.sendBroadcast(intent);
//            SocketService.this.getSocketTask().sendMessage(SocketTask.MSG_REQUEST, SocketTask.REQUEST_KEY_NOBODY, SocketMessage.SOCKET_TYPE_JSON,String.format(RequestDataHelper.CvsConnectRequest, Application.App.getDeviceId()), null);
            SocketService.this.getMainSocketTask().sendMessage(SocketTask.MSG_CONNECT_CHECK, SocketTask.REQUEST_KEY_NOBODY, null, null);
//            SocketService.this.getRawSocketTask().sendMessage(SocketTask.MSG_CONNECT_CHECK, SocketTask.REQUEST_KEY_NOBODY, null, null);
        }
    };
    public class ServiceBinder extends Binder {
        public SocketService getService() {
            return SocketService.this;
        }

        public void request(String key, int type, String request){
            if(type == SocketMessage.SOCKET_TYPE_JSON) {
                SocketService.this.getMainSocketTask().sendMessage(SocketTask.MSG_REQUEST, key, type, request, null);
            }else if(type == SocketMessage.SOCKET_TYPE_RAW || type == SocketMessage.SOCKET_TYPE_JSON_DOWNLOAD_RAW){
                SocketService.this.getRawSocketTask().sendMessage(SocketTask.MSG_REQUEST, key, type, request, mReceiveCallback);
            }
        }

        public void stopReceive(){
            SocketService.this.getMainSocketTask().stopReceive();
        }
    }

    public ISocketServiceBinder.Stub stub = new ISocketServiceBinder.Stub() {
        @Override
        public void request(String key, int type, String request) throws RemoteException {
            if(type == SocketMessage.SOCKET_TYPE_JSON) {
                SocketService.this.getMainSocketTask().sendMessage(SocketTask.MSG_REQUEST, key, type, request, null);
            }else if(type == SocketMessage.SOCKET_TYPE_RAW || type == SocketMessage.SOCKET_TYPE_JSON_DOWNLOAD_RAW){
                SocketService.this.getRawSocketTask().sendMessage(SocketTask.MSG_REQUEST, key, type, request, mReceiveCallback);
            }
        }

        @Override
        public boolean isConnected(){
            return SocketService.this.getMainSocketTask().isConnected();
        }

        public void stopReceive(){
            SocketService.this.getMainSocketTask().stopReceive();
        }
    };

    private class RawFolderObserver extends FileObserver{

        public RawFolderObserver(String path) {
            super(path);
        }

        public RawFolderObserver(String path, int mask) {
            super(path, mask);
        }
        @Override
        public void onEvent(int event , String s) {
            int action = event & FileObserver.ALL_EVENTS;
            switch (action){
                case FileObserver.CREATE:
                    Log.d(TAG, "event: FileObserver.CREATE path: " + s);
                    int c = FileUtils.deleteOldFilesByCount(new File(Application.App.getSocketRawFolder()), 10);
                    Log.d(TAG,"deleteOldFilesByCount:" + c);
                    break;
                case FileObserver.ACCESS:
                    Log.d(TAG, "event: FileObserver.ACCESS path: " + s);
                    break;
                case FileObserver.DELETE:
                    Log.d(TAG,"event: FileObserver.DELETE path: " + s);
                    break;
                case FileObserver.OPEN:
                    Log.d(TAG,"event: FileObserver.OPEN path: " + s);
                    break;
                case FileObserver.MODIFY:
                    Log.d(TAG,"event: FileObserver.MODIFY path: " + s);
                    int count = FileUtils.deleteOldFilesByCount(new File(Application.App.getSocketRawFolder()), 10);
                    Log.d(TAG,"deleteOldFilesByCount:" + count);
                    break;
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mBinder = new ServiceBinder();
        jsonSocketTask = new SocketTask();
        jsonSocketTask.startWithReceive(mReceiveCallback);
        Log.d(TAG,"启动json socketTask:" +   jsonSocketTask.hashCode());
        jsonSocketTask.setRawFolder(Application.App.getSocketRawFolder());
        rawSocketTask = new SocketTask();
        rawSocketTask.setRawFolder(Application.App.getSocketRawFolder());
        rawSocketTask.setRetryTimes(5);
        rawSocketTask.start();
        Log.d(TAG,"启动raw socketTask:" +   rawSocketTask.hashCode());

        mRawDirObserver = new RawFolderObserver(Application.App.getSocketRawFolder(), FileObserver.CREATE );
        mRawDirObserver.startWatching();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return stub;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("SocketService","onStartCommand");
        return Service. START_STICKY;
    }

    @Override
    public void onDestroy() {
        jsonSocketTask.quitThreadLooper();
        mRawDirObserver.stopWatching();
        super.onDestroy();
    }
}
