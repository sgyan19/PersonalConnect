package com.sun.power;

import com.sun.account.Account;
import com.sun.connect.RequestJson;
import com.sun.connect.RequestDataHelper;
import com.sun.conversation.CvsNote;
import com.sun.personalconnect.Application;
import com.sun.utils.GsonUtils;
import com.sun.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by guoyao on 2017/1/4.
 */
public class InputFormat {
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

    public static Object[] makeRequest(String input, List<String> format){
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
        if(format != null && format.size() > 0 &&Application.App.getPowerTaskManger().serverCheck()){
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
            }
        }else{
            note.setContent(input);
            requestJson.setCode(RequestDataHelper.CODE_ConversationNote);
            requestJson.addArg(GsonUtils.mGson.toJson(note));
        }
        result[0] = requestJson;
        result[1] = note;
        return result;
    }

    public static Object[] makeRequest(File file){
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

        result[0] = requestJson;
        result[1] = note;
        return result;
    }
}
