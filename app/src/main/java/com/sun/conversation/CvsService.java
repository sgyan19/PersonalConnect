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
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.sun.connect.RequestData;
import com.sun.connect.RequestDataHelper;
import com.sun.connect.ResponseData;
import com.sun.connect.SocketService;
import com.sun.connect.SocketTask;
import com.sun.personalconnect.Application;
import com.sun.personalconnect.R;
import com.sun.utils.GsonUtils;

import java.lang.ref.WeakReference;
import java.util.LinkedHashMap;

/**
 * Created by guoyao on 2016/12/23.
 * this is a local service, activity can touch this safety.
 */
public class CvsService extends Service {
    private final static String TAG = "CvsService";
    private CvsService.ServiceBinder serviceBinder;
    private SocketService.ServiceBinder socketBinder;
    private LinkedHashMap<Integer, CvsNote> mRequestHistory = new LinkedHashMap<>();
    private WeakReference<CvsListener> mCvsListenerReference;
    private ServiceConnection socketConn;

    public interface CvsListener {
        void onSendFailed(long key, CvsNote note, String message);
        void onSendSuccess(CvsNote note);
        void onNew(CvsNote note);
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
            String response = intent.getStringExtra(SocketService.KEY_STRING_RESPONSE);
            RequestData noteRequest = null;
            ResponseData responseObj  = null;
            CvsNote note = null;
            if(TextUtils.isEmpty(error) && !TextUtils.isEmpty(response)){
                try {
                    responseObj = GsonUtils.mGson.fromJson(response, ResponseData.class);
                    noteRequest = GsonUtils.mGson.fromJson(responseObj.getData(), RequestData.class);
                    note = GsonUtils.mGson.fromJson(noteRequest.getArgs().get(0), CvsNote.class);
                }catch (Exception e){
                    Log.e(TAG, "Receive:" + response);
                }
            }

            Log.d(TAG, String.format("BroadcastReceiver error:%s,response:%s", error, response));
            if(!TextUtils.isEmpty(error) || noteRequest == null || note == null){
                if(mRequestHistory.containsKey(key)){
                    Application.getInstance().getCvsHistoryManager().saveCache();
                    mRequestHistory.get(key).setSendStatus(CvsNote.STATUS_FAL);
                    mRequestHistory.remove(key);
                    if(( l = getOnCvsListener()) != null){
                        l.onSendFailed(key, note, error);
                    }
                    return;
                }
                return;
            }
            key = responseObj.getRequestId();

            note.setSendStatus(CvsNote.STATUS_SUC);
            if(key == SocketTask.REQUEST_KEY_ANYBODY){
                Application.getInstance().getCvsHistoryManager().insertCache(note);
                Application.getInstance().getCvsHistoryManager().saveCache();
                if(( l = getOnCvsListener()) != null){
                    l.onNew(note);
                }else{
                    showNotification(CvsService.this, note.getUserName(), note.getContent());
                }
            }else if(mRequestHistory.containsKey(key)){
                mRequestHistory.get(key).setSendStatus(CvsNote.STATUS_SUC);
                mRequestHistory.remove(key);
                Application.getInstance().getCvsHistoryManager().saveCache();
                if(( l = getOnCvsListener()) != null){
                    l.onSendSuccess(note);
                }
            }
        }
    };

    public class ServiceBinder extends Binder {
        public CvsService getService() {
            return CvsService.this;
        }

        public void Request(CvsNote note){
            if(socketBinder != null){
                RequestData requestData = new RequestData();
                requestData.setCode(RequestDataHelper.CODE_ConversationNote);
                String arg = GsonUtils.mGson.toJson(note);
                requestData.addArg(arg);
                requestData.setRequestId(requestData.hashCode());
                requestData.setDeviceId(Application.getInstance().getDeviceId());
                socketBinder.request(requestData.getRequestId(), GsonUtils.mGson.toJson(requestData));
                mRequestHistory.put(requestData.getRequestId(), note);

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
                    socketBinder = (SocketService.ServiceBinder) iBinder;
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
        socketBinder.stopReceive();
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
}
