package com.sun.connect;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.JsonSyntaxException;
import com.sun.personalconnect.Application;
import com.sun.personalconnect.BaseReceiver;
import com.sun.utils.GsonUtils;
import java.io.File;
import java.util.Map;

import static com.sun.connect.SocketMessage.SOCKET_TYPE_JSON;

/**
 * Created by guoyao on 2017/2/17.
 */
public class SocketReceiver extends BaseReceiver<SocketReceiver.SocketReceiveListener> {
    private static final String TAG = "SocketReceiver";

    @Override
    protected IntentFilter getIntentFilter() {
        return new IntentFilter(SocketService.SocketReceiveBroadcast);
    }

    public interface SocketReceiveListener{
        boolean onReconnected(boolean connected);
        boolean onError(int key, String error);
        boolean onParserResponse(int key, ResponseJson json, String info);
        boolean onReceiveFile(int key, File file, String info);
        boolean onParserData(int key, ResponseJson json, Object data, String info);
    }

    public static void register(Context context, SocketReceiveListener listener){
        BaseReceiver.register(context, SocketReceiver.class, listener);
    }

    public static void unregister(Context context){
        BaseReceiver.unregister(context, SocketReceiver.class);
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
            for (Map.Entry<Context, SocketReceiveListener> entry : getListeners().entrySet()) {
                if (entry.getValue().onReconnected(true)) {
                    return;
                }
            }
        }
        if(!TextUtils.isEmpty(error)) {
            for (Map.Entry<Context, SocketReceiveListener> entry : getListeners().entrySet()) {
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
                for (Map.Entry<Context, SocketReceiveListener> entry : getListeners().entrySet()) {
                    if (entry.getValue().onParserResponse(key, responseObj, info)) {
                        return;
                    }
                }
            }else if(responseType == SocketMessage.SOCKET_TYPE_RAW) {
                Log.d(TAG, "BroadcastReceiver SOCKET_TYPE_RAW");
                File file = new File(Application.App.getSocketRawFolder(), response);
                Log.d(TAG, "BroadcastReceiver raw path:" + file.getPath());
                for (Map.Entry<Context, SocketReceiveListener> entry : getListeners().entrySet()) {
                    if (entry.getValue().onReceiveFile(key, file, null)) {
                        return;
                    }
                }
            }
            if(responseObj == null){
                return;
            }
            key = responseObj.getRequestId();
            String info = null;
            Object obj = null;
            if(responseObj.getFormat() != null){
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
            }else{
                info = "response format == null";
            }
            for (Map.Entry<Context, SocketReceiveListener> entry : getListeners().entrySet()) {
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
