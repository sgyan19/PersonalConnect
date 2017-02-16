package com.sun.conversation;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

import static com.sun.connect.SocketMessage.SOCKET_TYPE_JSON;
import static com.sun.connect.SocketMessage.SOCKET_TYPE_RAW;

/**
 * Created by guoyao on 2016/12/23.
 * this is a local service, activity can touch this safely.
 */
public class CvsService extends Service {
    private final static String TAG = "CvsService";
    private CvsService.ServiceBinder serviceBinder;
    private ISocketServiceBinder socketBinder;
    private LinkedHashMap<Integer, CvsNote> mRequestHistory = new LinkedHashMap<>();
    private WeakReference<CvsListener> mCvsListenerReference;
    private ServiceConnection socketConn;

    public interface CvsListener {
        void onSendFailed(long key, CvsNote note, String message);
        void onSendSuccess(CvsNote note);
        void onNew(CvsNote note);
        void onRaw(File file);
    }

    private CvsListener getOnCvsListener(){
        return mCvsListenerReference == null ? null : mCvsListenerReference.get();
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "BroadcastReceiver");
            int key = intent.getIntExtra(SocketService.KEY_INT_REQUESTKEY, SocketTask.REQUEST_KEY_NOBODY);
            if(key == SocketTask.REQUEST_KEY_NOBODY) {
                return;
            }
            CvsListener l;
            String error = intent.getStringExtra(SocketService.KEY_STRING_ERROR);
            int responseType = intent.getIntExtra(SocketService.KEY_INT_RESPONSE_TYPE, SOCKET_TYPE_JSON);
            String response = intent.getStringExtra(SocketService.KEY_STRING_RESPONSE);
            boolean connected = intent.getBooleanExtra(SocketService.KEY_BOOLEAN_CONNECTED, false);
            if(connected && socketBinder != null){
                try {
                    socketBinder.request(SocketTask.REQUEST_KEY_NOBODY, SocketMessage.SOCKET_TYPE_JSON, String.format(RequestDataHelper.CvsConnectRequest, Application.App.getCvsHistoryManager().getLastSucNoteId(),Application.App.getDeviceId()));
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                return;
            }
            ResponseJson responseObj  = null;
            if(TextUtils.isEmpty(error) && !TextUtils.isEmpty(response)){
                if(responseType == SOCKET_TYPE_JSON) {
                    try {
                        responseObj = GsonUtils.mGson.fromJson(response, ResponseJson.class);
                    } catch (Exception e) {
                        Log.e(TAG, "Receive:" + response);
                    }
                }else if(responseType == SOCKET_TYPE_RAW){
                    Log.d(TAG, "BroadcastReceiver SOCKET_TYPE_RAW");
                    if(( l = getOnCvsListener()) != null){
                        File file = new File(Application.App.getSocketRawFolder(), response);
                        Log.d(TAG, "BroadcastReceiver raw path:" + file.getPath());
                        if(file.exists()){
                            l.onRaw(file);
                        }
                    }
                }
            }
            if(responseObj == null){
                if(mRequestHistory.containsKey(key)){
                    CvsNote note = mRequestHistory.get(key);
                    note.setSendStatus(CvsNote.STATUS_FAL);
                    Application.App.getCvsHistoryManager().updateCache(note.getId());
                    mRequestHistory.remove(key);
                    if(( l = getOnCvsListener()) != null){
                        l.onSendFailed(key, note, error);
                    }
                }
                return;
            }
            Log.d(TAG, String.format("BroadcastReceiver error:%s,response:%s", error, response));

            if(handleResponse(responseObj) || "success".equals(responseObj.getData())){
                return;
            }
            Class<?> formater;
            try {
                formater = getClassLoader().loadClass(responseObj.getFormat());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                return;
            }
            Object object = null;
            try {
                object = GsonUtils.mGson.fromJson(responseObj.getData(), formater);
            }catch (Exception e){
                e.printStackTrace();
            }
            if(object == null){
                return;
            }
            if(object instanceof CvsNote) {
                CvsNote note = (CvsNote) object;
                handleNote(note);
                key = responseObj.getRequestId();
                note.setSendStatus(CvsNote.STATUS_SUC);
                if (key == SocketTask.REQUEST_KEY_ANYBODY) {
                    Application.App.getCvsHistoryManager().insertCache(note);
//                Application.App.getCvsHistoryManager().saveCache(); // 新策略，已经废弃
                    if ((l = getOnCvsListener()) != null) {
                        l.onNew(note);
                    } else {
                        showNotification(CvsService.this, note.getUserName(), note.getContent());
                    }
                } else if (mRequestHistory.containsKey(key)) {
                    mRequestHistory.get(key).setSendStatus(CvsNote.STATUS_SUC);
                    CvsNote localNote = mRequestHistory.remove(key);
                    Application.App.getCvsHistoryManager().updateCache(localNote.getId());
                    if ((l = getOnCvsListener()) != null) {
                        l.onSendSuccess(localNote);
                    }
                }
            }
        }
    };

    public class ServiceBinder extends Binder {
        public CvsService getService() {
            return CvsService.this;
        }

        public CvsNote request(File file){
            if(socketBinder == null){
                return null;
            }
            Object[] objects = InputFormat.makeRequest(file);
            if(objects == null) return null;
            CvsNote note = (CvsNote)objects[1];
            RequestJson requestJson = (RequestJson)objects[0];
            try {
                socketBinder.request(requestJson.getRequestId(), SocketMessage.SOCKET_TYPE_JSON, GsonUtils.mGson.toJson(requestJson));
                socketBinder.request(requestJson.getRequestId(), SocketMessage.SOCKET_TYPE_RAW, file.getName());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            if(note != null)
                mRequestHistory.put(requestJson.getRequestId(), note);
            return note;
        }

        public CvsNote request(String content, List<String> cmds){
            if(socketBinder == null){
                return null;
            }
            Object[] objects = InputFormat.makeRequest(content, cmds);
            if(objects == null) return null;
            CvsNote note = (CvsNote)objects[1];
            RequestJson requestJson = (RequestJson)objects[0];
            try {
                socketBinder.request(requestJson.getRequestId(), SocketMessage.SOCKET_TYPE_JSON,GsonUtils.mGson.toJson(requestJson));
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
                socketBinder.request(requestJson.getRequestId(), SocketMessage.SOCKET_TYPE_JSON,GsonUtils.mGson.toJson(requestJson));
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            mRequestHistory.put(requestJson.getRequestId(), note);
            return note;
        }

        public void download(String name){
            if(socketBinder == null){
                return;
            }
            RequestJson requestJson = InputFormat.makeDownloadRequest(name);
            try {
                socketBinder.request(requestJson.getRequestId(), SocketMessage.SOCKET_TYPE_JSON,GsonUtils.mGson.toJson(requestJson));
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        public void setCvsListener(CvsListener l){
            mCvsListenerReference = new WeakReference<>(l);
        }

        public void clearMyListener(CvsListener l){
            if(mCvsListenerReference != null && mCvsListenerReference.get() == l){
                mCvsListenerReference.clear();
                mCvsListenerReference = null;
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return serviceBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        serviceBinder = new ServiceBinder();
        IntentFilter intentFilter = new IntentFilter(SocketService.SocketReceiveBroadcast);
        registerReceiver(mReceiver, intentFilter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        if(socketBinder == null) {
            socketConn = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                    socketBinder = ISocketServiceBinder.Stub.asInterface(iBinder);
                    try {
                        if(socketBinder.isConnected()){
                            socketBinder.request(SocketTask.REQUEST_KEY_NOBODY, SocketMessage.SOCKET_TYPE_JSON, String.format(RequestDataHelper.CvsConnectRequest, Application.App.getCvsHistoryManager().getLastSucNoteId(), Application.App.getDeviceId()));
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onServiceDisconnected(ComponentName componentName) {
                    socketBinder = null;
                }
            };
            bindService(new Intent(CvsService.this, SocketService.class), socketConn, BIND_AUTO_CREATE);
        }
        return Service. START_STICKY;
    }

    @Override
    public void onDestroy() {
        try {
            socketBinder.stopReceive();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        unregisterReceiver(mReceiver);
        if(socketConn != null){
            unbindService(socketConn);
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

    private boolean handleResponse(ResponseJson response){
        switch (response.getCode()){
            default:
                break;
        }
        return false;
    }

    private boolean handleRequest(RequestJson request){
        switch (request.getCode()){
            default:
                break;
        }
        return false;
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
