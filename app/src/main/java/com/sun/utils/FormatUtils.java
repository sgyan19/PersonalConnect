package com.sun.utils;

import android.content.Context;
import android.text.TextUtils;

import com.sun.account.Account;
import com.sun.connect.RequestJson;
import com.sun.connect.RequestDataHelper;
import com.sun.connect.ResponseJson;
import com.sun.conversation.CvsNote;
import com.sun.device.AnswerNote;
import com.sun.gps.GpsGear;
import com.sun.gps.GpsResponse;
import com.sun.gps.GpsRequest;
import com.sun.level.CmdDefine;
import com.sun.personalconnect.Application;
import com.sun.device.AskNote;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by guoyao on 2017/1/4.
 */
public class FormatUtils {
    public final static String Power_format_gap = "！？";

    public static List<String> format(String input){
        List<String> result = new ArrayList<>();
        if(input == null){
            return null;
        }
        if(input.indexOf(Power_format_gap) == 0){
            String[] strs = input.split(Power_format_gap);
            if(strs.length  < 1){
                return null;
            }
            for(int i = 1; i < strs.length; i++){
                result.add(strs[i]);
            }
        }
        return result;
    }

    public static RequestJson makeCvsRequest(String key, BoxObject box, String input, List<String> format){
        if(TextUtils.isEmpty(key)){
            key = IdUtils.make();
        }
        RequestJson requestJson = new RequestJson();
        requestJson.setDeviceId(Application.App.getDeviceId());
        requestJson.setRequestId(key);
        requestJson.setCode(RequestDataHelper.MobileTerminalJson);

        CvsNote note = new CvsNote();
        Account account = Application.App.getAccount();
        note.setId((int) System.currentTimeMillis());
        note.setUserName(account.getLoginName());
        note.setUserId(account.getLoginId());
        long time = System.currentTimeMillis();
        note.setTimeStamp(time);
        note.setTimeFormat(Utils.getFormatTime(time));
        if(format != null && format.size() > 0 &&Application.App.getLevelCenter().serverCheck()){
            if(CmdDefine.CMD_REMOTE_RING_NOTE.equalsIgnoreCase(format.get(0))){
                note.setPower(CvsNote.POWER_RING);
                if(format.size()  > 1) {
                    note.setContent(format.get(1));
                }else{
                    note.setContent("");
                }
                if(format.size()  > 2){
                    note.setExtend(format.get(2));
                }
                requestJson.clearArgs();
                requestJson.addArg(CvsNote.class.getName());
                requestJson.addArg(GsonUtils.mGson.toJson(note));
            }
        }else{
            note.setContent(input);
            requestJson.clearArgs();
            requestJson.addArg(CvsNote.class.getName());
            requestJson.addArg(GsonUtils.mGson.toJson(note));
        }
        if(box != null){
            box.data = note;
        }
        return requestJson;
    }

    public static RequestJson makeCvsRequest(String key, CvsNote note){
        if(TextUtils.isEmpty(key)){
            key = IdUtils.make();
        }
        RequestJson requestJson = new RequestJson();
        requestJson.setDeviceId(Application.App.getDeviceId());
        requestJson.setRequestId(key);

        requestJson.setCode(RequestDataHelper.MobileTerminalJson);
        requestJson.addArg(CvsNote.class.getName());
        requestJson.addArg(GsonUtils.mGson.toJson(note));
        return requestJson;
    }

    public static RequestJson makeCvsRequest(String key, BoxObject box,File file){
        if(TextUtils.isEmpty(key)){
            key = IdUtils.make();
        }
        RequestJson requestJson = new RequestJson();
        requestJson.setDeviceId(Application.App.getDeviceId());
        requestJson.setRequestId(key);

        CvsNote note = new CvsNote();
        Account account = Application.App.getAccount();
        note.setId((int) System.currentTimeMillis());
        note.setUserName(account.getLoginName());
        note.setUserId(account.getLoginId());
        long time = System.currentTimeMillis();
        note.setTimeStamp(time);
        note.setTimeFormat(Utils.getFormatTime(time));

        note.setContent(file.getName());
        note.setType(CvsNote.TYPE_IMAGE);
        note.setExtend(String.valueOf(file.length()));

        requestJson.setCode(RequestDataHelper.MobileTerminalJson);
        requestJson.addArg(CvsNote.class.getName());
        requestJson.addArg(GsonUtils.mGson.toJson(note));
        if(box != null){
            box.data = note;
        }
        return requestJson;
    }

    public static RequestJson makeRequest(String key, Object object){
        if(TextUtils.isEmpty(key)){
            key = IdUtils.make();
        }
        RequestJson requestJson = new RequestJson();
        requestJson.setDeviceId(Application.App.getDeviceId());
        requestJson.setRequestId(key);
        requestJson.setCode(RequestDataHelper.MobileTerminalJson);
        requestJson.addArg(object.getClass().getName());
        requestJson.addArg(GsonUtils.mGson.toJson(object));
        return requestJson;
    }

    public static RequestJson makeGpsRequest(String key,BoxObject box, GpsGear gpsGear){
        if(TextUtils.isEmpty(key)){
            key = IdUtils.make();
        }
        Account account = Application.App.getAccount();
        GpsRequest gpsRequest = new GpsRequest();
        gpsRequest.setId(System.currentTimeMillis());
        gpsRequest.setUserId(account.getLoginId());
        gpsRequest.setUserName(account.getLoginName());
        gpsRequest.setGpsGear(gpsGear);

        RequestJson requestJson = new RequestJson();
        requestJson.setDeviceId(Application.App.getDeviceId());
        requestJson.setRequestId(key);
        requestJson.setCode(RequestDataHelper.MobileTerminalJson);
        requestJson.addArg(GpsRequest.class.getName());
        requestJson.addArg(GsonUtils.mGson.toJson(gpsRequest));
        if(box != null){
            box.data = gpsRequest;
        }
        return requestJson;
    }

    public static RequestJson makeDownloadRequest(String key, String name){
        if(TextUtils.isEmpty(key)){
            key = IdUtils.make();
        }
        RequestJson requestJson = new RequestJson();
        requestJson.setDeviceId(Application.App.getDeviceId());
        requestJson.setRequestId(key);
        requestJson.setCode(RequestDataHelper.MoboleTerminalRaw);
        requestJson.addArg(name);
        File file = new File(Application.App.getSocketRawFolder(), name);
        if(file.exists()){
            requestJson.addArg(Utils.md5(file));
        }
        return requestJson;
    }

    public static GpsResponse fillCommonArgs(GpsResponse note){
        if(note != null){
//            note.setTime(System.currentTimeMillis());
            note.setDevice(Application.App.getDeviceId());
            note.setUserId(Application.App.getAccount().getLoginId());
            note.setUserName(Application.App.getAccount().getLoginName());
            note.setErrInfo("");
        }
        return note;
    }

    public static RequestJson setSession(RequestJson json,String... devices){
        json.addArg(Application.App.getDeviceId());
        json.addArgs(devices);
        return json;
    }

    public static Object getFormatData(Context context , ResponseJson responseJson) throws Exception{
        if(context == null || responseJson == null){
            return null;
        }
        ClassLoader loader = context.getClassLoader();
        if(responseJson.getData() == null || responseJson.getData().length < 2){
            return responseJson.getData()[0];
        }
        String format = responseJson.getData()[0];
        Class clazz = loader.loadClass(format);
        return GsonUtils.mGson.fromJson(responseJson.getData()[1], clazz);
    }
}
