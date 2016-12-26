package com.sun.connect;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by guoyao on 2016/12/21.
 * this service maybe a remote service.Activity should not touch this.
 */
public class SocketService extends Service {
    public static final String SocketReceiveBroadcast = "com.sun.connect.SocketService.Receive";
    public static final String KEY_INT_REQUESTKEY = "requestKey";
    public static final String KEY_STRING_RESPONSE = "response";
    public static final String KEY_STRING_ERROR = "error";

    private ServiceBinder mBinder;
    private SocketCallback mReceiveCallback = new SocketCallback() {
        @Override
        public void onError(int requestKey, Throwable e) {
            Intent intent = new Intent(SocketReceiveBroadcast);
            intent.putExtra(KEY_INT_REQUESTKEY, requestKey);
            intent.putExtra(KEY_STRING_ERROR, e.toString());
            SocketService.this.sendBroadcast(intent);
        }

        @Override
        public void onComplete(int requestKey, String response) {
            if(response != null){
                Intent intent = new Intent(SocketReceiveBroadcast);
                intent.putExtra(KEY_INT_REQUESTKEY, requestKey);
                intent.putExtra(KEY_STRING_RESPONSE, response);
                SocketService.this.sendBroadcast(intent);
            }
        }

        @Override
        public void onConnected(int requestKey) {
            SocketService.this.getSocketTask().sendMessage(SocketTask.MSG_REQUEST, SocketTask.REQUEST_KEY_NOBODY, RequestDataHelper.CvsConnectRequest, null);
        }
    };
    public class ServiceBinder extends Binder {
        public SocketService getService() {
            return SocketService.this;
        }

        public void request(int key, String request){
            SocketService.this.getSocketTask().sendMessage(SocketTask.MSG_REQUEST, key, request, null);
        }

        public void stopReceive(){
            SocketService.this.getSocketTask().stopReceive();
        }
    }

    public ISocketServiceBinder.Stub stub = new ISocketServiceBinder.Stub() {
        @Override
        public void request(int key, String request) throws RemoteException {
            SocketService.this.getSocketTask().sendMessage(SocketTask.MSG_REQUEST, key, request, null);
        }
    };

    private SocketTask socketTask;

    public SocketTask getSocketTask(){
        return socketTask;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mBinder = new ServiceBinder();
        socketTask = new SocketTask();
        socketTask.startWithReceive(mReceiveCallback);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("SocketService","onStartCommand");
        return Service. START_STICKY;
    }

    @Override
    public void onDestroy() {
        socketTask.quitThreadLooper();
        super.onDestroy();
    }
}
