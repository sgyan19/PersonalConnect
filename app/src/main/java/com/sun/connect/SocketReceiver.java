package com.sun.connect;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;
import android.util.Log;

import com.sun.account.Account;
import com.sun.common.SessionNote;
import com.sun.personalconnect.Application;
import com.sun.personalconnect.BaseReceiver;
import com.sun.utils.FormatUtils;
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
        boolean onError(String key, String error);
        boolean onParserResponse(String key, ResponseJson json, String info);
        boolean onReceiveFile(String key, File file, String info);
        boolean onParserData(String key, ResponseJson json, Object data, String info);
        void onOver(String key);
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

        String key = intent.getStringExtra(SocketService.KEY_STRING_REQUESTKEY);
        if(SocketTask.REQUEST_KEY_NOBODY.equals(key)) {
            return;
        }

        String error = intent.getStringExtra(SocketService.KEY_STRING_ERROR);
        int responseType = intent.getIntExtra(SocketService.KEY_INT_RESPONSE_TYPE, SOCKET_TYPE_JSON);
        String response = intent.getStringExtra(SocketService.KEY_STRING_RESPONSE);
        boolean connected = intent.getBooleanExtra(SocketService.KEY_BOOLEAN_CONNECTED, false);
        if(connected) {
            for (Map.Entry<Context, SocketReceiveListener> entry : getListeners().entrySet()) {
                if (entry.getValue().onReconnected(true)) {
                    overParser(key);
                    return;
                }
            }
        }
        if(!TextUtils.isEmpty(error)) {
            for (Map.Entry<Context, SocketReceiveListener> entry : getListeners().entrySet()) {
                if (entry.getValue().onError(key, error)) {
                    overParser(key);
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
                if(responseObj != null){
                    if(TextUtils.isEmpty(key)){
                        key = responseObj.getRequestId();
                    }else if(TextUtils.isEmpty(responseObj.getRequestId())) {
                        responseObj.setRequestId(key);
                    }
                }
                if(handleResponse(responseObj, response)){
                    overParser(key);
                    return;
                }
                for (Map.Entry<Context, SocketReceiveListener> entry : getListeners().entrySet()) {
                    if (entry.getValue().onParserResponse(key, responseObj, info)) {
                        overParser(key);
                        return;
                    }
                }
            }else if(responseType == SocketMessage.SOCKET_TYPE_RAW) {
                Log.d(TAG, "BroadcastReceiver SOCKET_TYPE_RAW");
                File file = new File(Application.App.getSocketRawFolder(), response);
                Log.d(TAG, "BroadcastReceiver raw path:" + file.getPath());
                for (Map.Entry<Context, SocketReceiveListener> entry : getListeners().entrySet()) {
                    if (entry.getValue().onReceiveFile(key, file, null)) {
                        overParser(key);
                        return;
                    }
                }
                overParser(key);
                return;
            }
            if(responseObj == null){
                overParser(key);
                return;
            }
            key = responseObj.getRequestId();
            String info = null;
            Object obj = null;
            try{
                obj = FormatUtils.getFormatData(context, responseObj);
            }catch (Exception e) {
                e.printStackTrace();
                info = e.toString();
            }
            if(handleData(responseObj, obj)){
                overParser(key);
                return;
            }
            for (Map.Entry<Context, SocketReceiveListener> entry : getListeners().entrySet()) {
                if (entry.getValue().onParserData(key, responseObj, obj, info)) {
                    overParser(key);
                    return;
                }
            }
        }
    }

    private void overParser(String key){
        for (Map.Entry<Context, SocketReceiveListener> entry : getListeners().entrySet()) {
            entry.getValue().onOver(key);
        }
    }

    private boolean handleResponse(ResponseJson response, String responseStr){
        if(response == null)
            return false;
        switch (response.getCode()){
            default:
                break;
        }

        if(!SocketTask.REQUEST_KEY_ANYBODY.equals(response.getRequestId())){
            ResponseHistoryManager responseHistoryManager = Application.App.getResponseHistoryManager();
            boolean neverHandleThis = responseHistoryManager.insert(new ResponseNote(response.getRequestId(),responseStr));
            if(!neverHandleThis){
                return true;
            }
        }

        return response.getData().length > 0 && "success".equals(response.getData()[0]);
    }

    private boolean handleData(ResponseJson response, Object data){
        if(data == null)
            return false;
        boolean intercept = false;
        if(data instanceof SessionNote){
            switch (((SessionNote) data).getSessionType()){
                case SessionNote.TYPE_ALL:
                    break;
                case SessionNote.TYPE_DEVICE:
                    String did = Application.App.getDeviceId();
                    intercept = did == null || !((SessionNote) data).getSessionCondition().contains(did);
                    break;
                case SessionNote.TYPE_USER_NAME:
                    String name = Application.App.getAccount().getLoginName();
                    intercept = name == null || !((SessionNote) data).getSessionCondition().contains(name);
                    break;
                case SessionNote.TYPE_LEVEL:
                    int id = Application.App.getAccount().getLoginId();
                    intercept = id == Account.NONE || !((SessionNote) data).getSessionCondition().contains(String.valueOf(id));
                    break;
            }
        }
        return intercept;
    }
}
