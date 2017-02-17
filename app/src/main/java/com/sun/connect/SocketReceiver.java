package com.sun.connect;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.JsonSyntaxException;
import com.sun.personalconnect.Application;
import com.sun.utils.GsonUtils;
import java.io.File;
import java.lang.ref.WeakReference;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static com.sun.connect.SocketMessage.SOCKET_TYPE_JSON;

/**
 * Created by guoyao on 2017/2/17.
 */
public class SocketReceiver extends BroadcastReceiver {
    private static final String TAG = "SocketReceiver";

    private static WeakReference<SocketReceiver> weakReference;
    private static SocketReceiver hardReference;

    private boolean mRegister = false;

    private LinkedHashMap<Context,SocketReceiveListener> listeners = new LinkedHashMap<>();
    public interface SocketReceiveListener{
        boolean onReconnected(boolean connected);
        boolean onError(int key, String error);
        boolean onParserResponse(int key, ResponseJson json, String info);
        boolean onReceiveFile(int key, File file, String info);
        boolean onParserData(int key, ResponseJson json, Object data, String info);
    }

    public static void register(Context context, SocketReceiveListener listener){
        SocketReceiver obj;
        if(hardReference == null) {
            if (weakReference == null || weakReference.get() == null) {
                obj = new SocketReceiver();
                weakReference = new WeakReference<>(obj);
            } else {
                obj = weakReference.get();
            }
            hardReference = obj;
        }else{
            obj = hardReference;
        }
        obj.listeners.put(context, listener);
        if(!obj.mRegister){
            IntentFilter intentFilter = new IntentFilter(SocketService.SocketReceiveBroadcast);
            context.registerReceiver(obj, intentFilter);
            obj.mRegister = true;
        }
    }

    public static void unregister(Context context){
        SocketReceiver obj;
        if(hardReference == null) {
            if (weakReference == null || (obj = weakReference.get()) == null) {
                return;
            }
        }else{
            obj = hardReference;
        }
        context.unregisterReceiver(obj);
        obj.listeners.remove(context);
        Set<Context> sets = obj.listeners.keySet();
        if(!sets.isEmpty()){
            Context ctt = sets.iterator().next();
            IntentFilter intentFilter = new IntentFilter(SocketService.SocketReceiveBroadcast);
            ctt.registerReceiver(obj, intentFilter);
        }else{
            obj.mRegister = false;
            hardReference = null;
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive");

        int key = intent.getIntExtra(SocketService.KEY_INT_REQUESTKEY, SocketTask.REQUEST_KEY_NOBODY);
        if(key == SocketTask.REQUEST_KEY_NOBODY) {
            return;
        }
        String error = intent.getStringExtra(SocketService.KEY_STRING_ERROR);
        int responseType = intent.getIntExtra(SocketService.KEY_INT_RESPONSE_TYPE, SOCKET_TYPE_JSON);
        String response = intent.getStringExtra(SocketService.KEY_STRING_RESPONSE);
        boolean connected = intent.getBooleanExtra(SocketService.KEY_BOOLEAN_CONNECTED, false);
        if(connected) {
            for (Map.Entry<Context, SocketReceiveListener> entry : listeners.entrySet()) {
                if (entry.getValue().onReconnected(true)) {
                    return;
                }
            }
        }
        if(!TextUtils.isEmpty(error)) {
            for (Map.Entry<Context, SocketReceiveListener> entry : listeners.entrySet()) {
                if (entry.getValue().onError(key, error)) {
                    return;
                }
            }
        }

        ResponseJson responseObj  = null;
        if(!TextUtils.isEmpty(response)){
            if(responseType == SocketMessage.SOCKET_TYPE_JSON) {
                String info = null;
                try {
                    responseObj = GsonUtils.mGson.fromJson(response, ResponseJson.class);
                    if(responseObj == null) {
                        info = "null response";
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    info = e.toString();
                }
                if(handleResponse(responseObj)){
                    return;
                }
                for (Map.Entry<Context, SocketReceiveListener> entry : listeners.entrySet()) {
                    if (entry.getValue().onParserResponse(key, responseObj, info)) {
                        return;
                    }
                }
            }else if(responseType == SocketMessage.SOCKET_TYPE_RAW) {
                Log.d(TAG, "BroadcastReceiver SOCKET_TYPE_RAW");
                File file = new File(Application.App.getSocketRawFolder(), response);
                Log.d(TAG, "BroadcastReceiver raw path:" + file.getPath());
                for (Map.Entry<Context, SocketReceiveListener> entry : listeners.entrySet()) {
                    if (entry.getValue().onReceiveFile(key, file, null)) {
                        return;
                    }
                }
            }
            if(responseObj == null){
                return;
            }
            String info = null;
            Object obj = null;
            try {
                Class<?> format = context.getClassLoader().loadClass(responseObj.getFormat());
                obj = GsonUtils.mGson.fromJson(responseObj.getData(), format);
                if(obj == null) {
                    info = "format failed";
                }
            } catch (ClassNotFoundException|JsonSyntaxException e) {
                e.printStackTrace();
                info = e.toString();
            }
            for (Map.Entry<Context, SocketReceiveListener> entry : listeners.entrySet()) {
                if (entry.getValue().onParserData(key, responseObj, obj, info)) {
                    return;
                }
            }
        }
    }

    private boolean handleResponse(ResponseJson response){
        if(response == null)
            return false;
        switch (response.getCode()){
            default:
                break;
        }
        return "success".equals(response.getData());
    }
}
