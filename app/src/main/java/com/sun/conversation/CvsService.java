package com.sun.conversation;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.sun.connect.ISocketServiceBinder;
import com.sun.connect.RequestDataHelper;
import com.sun.connect.RequestJson;
import com.sun.connect.ResponseJson;
import com.sun.connect.SocketMessage;
import com.sun.connect.SocketReceiver;
import com.sun.connect.SocketService;
import com.sun.connect.SocketTask;
import com.sun.personalconnect.Application;
import com.sun.personalconnect.R;
import com.sun.power.InputFormat;
import com.sun.utils.GsonUtils;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by guoyao on 2016/12/23.
 * this is a local service, activity can touch this safely.
 */
public class CvsService extends Service {
    private final static String TAG = "CvsService";
    private CvsService.ServiceBinder mServiceBinder;
    private ISocketServiceBinder mSocketBinder;
    private LinkedHashMap<Integer, CvsNote> mRequestHistory = new LinkedHashMap<>();
    private WeakReference<CvsListener> mCvsListenerReference;
    private ServiceConnection mSocketConn;

    public interface CvsListener {
        void onSendFailed(long key, CvsNote note, String message);
        void onSendSuccess(CvsNote note);
        void onNewCvsNote(CvsNote note);
        void onNewFile(File file);
    }

    private CvsListener getOnCvsListener(){
        return mCvsListenerReference == null ? null : mCvsListenerReference.get();
    }

    private SocketReceiver.SocketReceiveListener mReceiveListener = new SocketReceiver.SocketReceiveListener() {
        @Override
        public boolean onReconnected(boolean connected) {
            if(connected && mSocketBinder != null){
                try {
                    mSocketBinder.request(SocketTask.REQUEST_KEY_NOBODY, SocketMessage.SOCKET_TYPE_JSON, String.format(RequestDataHelper.CvsConnectRequest, Application.App.getCvsHistoryManager().getLastSucNoteId(), Application.App.getDeviceId()));
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            return true;
        }

        @Override
        public boolean onError(int key, String error) {
            if(mRequestHistory.containsKey(key)){
                CvsNote note = mRequestHistory.get(key);
                note.setSendStatus(CvsNote.STATUS_FAL);
                Application.App.getCvsHistoryManager().updateCache(note.getId());
                mRequestHistory.remove(key);
                CvsListener listener;
                if(( listener = getOnCvsListener()) != null){
                    listener.onSendFailed(key, note, error);
                }
                return true;
            }else {
                return false;
            }
        }

        @Override
        public boolean onParserResponse(int key, ResponseJson json, String info) {
            if(!TextUtils.isEmpty(info)){
                return onError(key, info);
            }
            return false;
        }

        @Override
        public boolean onReceiveFile(int key, File file, String info) {
            Log.d(TAG, "BroadcastReceiver SOCKET_TYPE_RAW");
            if(!TextUtils.isEmpty(info)){
                return onError(key, info);
            }
            CvsListener listener;
            if(( listener = getOnCvsListener()) != null){
                Log.d(TAG, "BroadcastReceiver raw path:" + file.getPath());
                if(file.exists()){
                    listener.onNewFile(file);
                }
            }
            return true;
        }

        @Override
        public boolean onParserData(int key, ResponseJson json,Object data, String info) {
            if(data instanceof CvsNote) {
                CvsNote note = (CvsNote) data;
                handleNote(note);
                key = json.getRequestId();
                note.setSendStatus(CvsNote.STATUS_SUC);
                CvsListener listener;
                if (key == SocketTask.REQUEST_KEY_ANYBODY) {
                    Application.App.getCvsHistoryManager().insertCache(note);
//                Application.App.getCvsHistoryManager().saveCache(); // 新策略，已经废弃
                    if ((listener = getOnCvsListener()) != null) {
                        listener.onNewCvsNote(note);
                    } else {
                        showNotification(CvsService.this, note.getUserName(), note.getContent());
                    }
                } else if (mRequestHistory.containsKey(key)) {
                    mRequestHistory.get(key).setSendStatus(CvsNote.STATUS_SUC);
                    CvsNote localNote = mRequestHistory.remove(key);
                    Application.App.getCvsHistoryManager().updateCache(localNote.getId());
                    if ((listener = getOnCvsListener()) != null) {
                        listener.onSendSuccess(localNote);
                    }
                }
                return true;
            }
            return false;
        }
    };

    public class ServiceBinder extends Binder {
        public CvsService getService() {
            return CvsService.this;
        }

        public CvsNote request(File file){
            if(mSocketBinder == null){
                return null;
            }
            Object[] objects = InputFormat.makeRequest(file);
            if(objects == null) return null;
            CvsNote note = (CvsNote)objects[1];
            RequestJson requestJson = (RequestJson)objects[0];
            try {
                mSocketBinder.request(requestJson.getRequestId(), SocketMessage.SOCKET_TYPE_JSON, GsonUtils.mGson.toJson(requestJson));
                mSocketBinder.request(requestJson.getRequestId(), SocketMessage.SOCKET_TYPE_RAW, file.getName());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            if(note != null)
                mRequestHistory.put(requestJson.getRequestId(), note);
            return note;
        }

        public CvsNote request(String content, List<String> cmds){
            if(mSocketBinder == null){
                return null;
            }
            Object[] objects = InputFormat.makeRequest(content, cmds);
            if(objects == null) return null;
            CvsNote note = (CvsNote)objects[1];
            RequestJson requestJson = (RequestJson)objects[0];
            try {
                mSocketBinder.request(requestJson.getRequestId(), SocketMessage.SOCKET_TYPE_JSON, GsonUtils.mGson.toJson(requestJson));
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            if(note != null) {
                mRequestHistory.put(requestJson.getRequestId(), note);
            }
            return note;
        }

        public CvsNote request(CvsNote note){
            RequestJson requestJson = InputFormat.makeRequest(note);
            try {
                mSocketBinder.request(requestJson.getRequestId(), SocketMessage.SOCKET_TYPE_JSON, GsonUtils.mGson.toJson(requestJson));
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            mRequestHistory.put(requestJson.getRequestId(), note);
            return note;
        }

        public void download(String name){
            if(mSocketBinder == null){
                return;
            }
            RequestJson requestJson = InputFormat.makeDownloadRequest(name);
            try {
                mSocketBinder.request(requestJson.getRequestId(), SocketMessage.SOCKET_TYPE_JSON, GsonUtils.mGson.toJson(requestJson));
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        public void setCvsListener(CvsListener l){
            mCvsListenerReference = new WeakReference<>(l);
        }

        public void clearListener(CvsListener l){
            if(mCvsListenerReference != null && mCvsListenerReference.get() == l){
                mCvsListenerReference.clear();
                mCvsListenerReference = null;
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mServiceBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mServiceBinder = new ServiceBinder();
        SocketReceiver.register(this, mReceiveListener);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        if(mSocketBinder == null) {
            mSocketConn = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                    mSocketBinder = ISocketServiceBinder.Stub.asInterface(iBinder);
                    try {
                        if(mSocketBinder.isConnected()){
                            mSocketBinder.request(SocketTask.REQUEST_KEY_NOBODY, SocketMessage.SOCKET_TYPE_JSON, String.format(RequestDataHelper.CvsConnectRequest, Application.App.getCvsHistoryManager().getLastSucNoteId(), Application.App.getDeviceId()));
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onServiceDisconnected(ComponentName componentName) {
                    mSocketBinder = null;
                }
            };
            bindService(new Intent(CvsService.this, SocketService.class), mSocketConn, BIND_AUTO_CREATE);
        }
        return Service. START_STICKY;
    }

    @Override
    public void onDestroy() {
        try {
            mSocketBinder.stopReceive();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        SocketReceiver.unregister(this);
        if(mSocketConn != null){
            unbindService(mSocketConn);
        }
        super.onDestroy();
    }

    private void showNotification(Context context,String title, String text) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Activity.NOTIFICATION_SERVICE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setContentTitle(title);
        builder.setContentText(text);
        builder.setAutoCancel(true);
        Intent intent = new Intent();
        intent.setClass(context, CvsActivity.class);
        PendingIntent intentLive = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentIntent(intentLive)
                .setSmallIcon(R.mipmap.ic_launcher);
        Notification notification = builder.build();
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        notification.defaults = Notification.DEFAULT_SOUND;
        notificationManager.notify(1224, notification);
    }

    private void handleNote(CvsNote note){
        switch (note.getPower()){
            case CvsNote.POWER_RING:
                Application.App.getPowerTaskManger().executeRingNote();
                break;
            default:
                break;
        }
    }
}
