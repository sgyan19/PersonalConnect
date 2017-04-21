package com.sun.utils;

import com.sun.account.Account;
import com.sun.connect.RequestJson;
import com.sun.connect.RequestDataHelper;
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

    public static Object[] makeCvsRequest(String input, List<String> format){
        Object[] result = new Object[2];
        RequestJson requestJson = new RequestJson();
        requestJson.setDeviceId(Application.App.getDeviceId());
        requestJson.setRequestId(requestJson.hashCode());

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
                requestJson.setCode(RequestDataHelper.CODE_ConversationNote);
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
                requestJson.addArg(GsonUtils.mGson.toJson(note));
                requestJson.addArg(CvsNote.class.getName());
                requestJson.addArg(String.valueOf(note.getId()));
            }
        }else{
            note.setContent(input);
            requestJson.setCode(RequestDataHelper.CODE_ConversationNote);
            requestJson.addArg(GsonUtils.mGson.toJson(note));
            requestJson.addArg(CvsNote.class.getName());
            requestJson.addArg(String.valueOf(note.getId()));
        }
        result[0] = requestJson;
        result[1] = note;
        return result;
    }

    public static Object[] makeCvsRequest(File file){
        Object[] result = new Object[2];
        RequestJson requestJson = new RequestJson();
        requestJson.setDeviceId(Application.App.getDeviceId());
        requestJson.setRequestId(requestJson.hashCode());

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

        requestJson.setCode(RequestDataHelper.CODE_ConversationNote);
        requestJson.addArg(GsonUtils.mGson.toJson(note));
        requestJson.addArg(CvsNote.class.getName());
        requestJson.addArg(String.valueOf(note.getId()));

        result[0] = requestJson;
        result[1] = note;
        return result;
    }

    public static RequestJson makeCvsRequest(CvsNote note){
        RequestJson requestJson = new RequestJson();
        requestJson.setDeviceId(Application.App.getDeviceId());
        requestJson.setRequestId(requestJson.hashCode());
        requestJson.setCode(RequestDataHelper.CODE_ConversationNote);
        requestJson.addArg(GsonUtils.mGson.toJson(note));
        requestJson.addArg(CvsNote.class.getName());
        requestJson.addArg(String.valueOf(note.getId()));
        return requestJson;
    }

    public static RequestJson makeAskRequest(AskNote note){
        if(note == null) {
            note = new AskNote();
        }
        RequestJson requestJson = new RequestJson();
        requestJson.setDeviceId(Application.App.getDeviceId());
        requestJson.setRequestId(requestJson.hashCode());
        requestJson.setCode(RequestDataHelper.CODE_ConversationNote);
        requestJson.addArg(GsonUtils.mGson.toJson(note));
        requestJson.addArg(AnswerNote.class.getName());
        return requestJson;
    }

    public static RequestJson makeAnswerRequest(AnswerNote note){
        if(note == null) {
            note = new AnswerNote();
            note.setDeviceId(Application.App.getDeviceId());
            note.setUserId(Application.App.getAccount().getLoginId());
            note.setUserName(Application.App.getAccount().getLoginName());
        }
        RequestJson requestJson = new RequestJson();
        requestJson.setDeviceId(Application.App.getDeviceId());
        requestJson.setRequestId(requestJson.hashCode());
        requestJson.setCode(RequestDataHelper.CODE_ConversationNote);
        requestJson.addArg(GsonUtils.mGson.toJson(note));
        requestJson.addArg(AnswerNote.class.getName());
        return requestJson;
    }

    public static Object[] makeGpsRequest(GpsGear gpsGear){
        Object[] result = new Object[2];
        Account account = Application.App.getAccount();
        GpsRequest gpsRequest = new GpsRequest();
        gpsRequest.setId(System.currentTimeMillis());
        gpsRequest.setUserId(account.getLoginId());
        gpsRequest.setUserName(account.getLoginName());
        gpsRequest.setGpsGear(gpsGear);

        RequestJson requestJson = new RequestJson();
        requestJson.setDeviceId(Application.App.getDeviceId());
        requestJson.setRequestId(requestJson.hashCode());
        requestJson.setCode(RequestDataHelper.CODE_ConversationNote);
        requestJson.addArg(GsonUtils.mGson.toJson(gpsRequest));
        requestJson.addArg(GpsRequest.class.getName());
        requestJson.addArg(String.valueOf(gpsRequest.getId()));
        result[0] = requestJson;
        result[1] = gpsRequest;
        return result;
    }

    public static RequestJson makeGpsReponseRequest(GpsResponse gpsResponse){
        RequestJson requestJson = new RequestJson();
        requestJson.setDeviceId(Application.App.getDeviceId());
        requestJson.setRequestId(requestJson.hashCode());
        requestJson.setCode(RequestDataHelper.CODE_ConversationNote);
        requestJson.addArg(GsonUtils.mGson.toJson(gpsResponse));
        requestJson.addArg(GpsResponse.class.getName());
        requestJson.addArg(String.valueOf(gpsResponse.getId()));
        return requestJson;
    }

    public static RequestJson makeGpsRequest(GpsResponse gpsResponse){
        RequestJson requestJson = new RequestJson();
        requestJson.setDeviceId(Application.App.getDeviceId());
        requestJson.setRequestId(requestJson.hashCode());
        requestJson.setCode(RequestDataHelper.CODE_ConversationNote);
        requestJson.addArg(GsonUtils.mGson.toJson(gpsResponse));
        requestJson.addArg(GpsResponse.class.getName());
        requestJson.addArg(String.valueOf(gpsResponse.getId()));
        return requestJson;
    }

    public static RequestJson makeDownloadRequest(String name){
        RequestJson requestJson = new RequestJson();
        requestJson.setDeviceId(Application.App.getDeviceId());
        requestJson.setRequestId(requestJson.hashCode());
        requestJson.setCode(RequestDataHelper.CODE_ConversationImage);
        requestJson.addArg(name);
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
}
