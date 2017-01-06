package com.sun.power;

import com.sun.account.Account;
import com.sun.connect.RequestData;
import com.sun.connect.RequestDataHelper;
import com.sun.conversation.CvsNote;
import com.sun.personalconnect.Application;
import com.sun.utils.GsonUtils;
import com.sun.utils.Utils;

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
        RequestData requestData = new RequestData();
        requestData.setDeviceId(Application.App.getDeviceId());
        requestData.setRequestId(requestData.hashCode());

        CvsNote note = new CvsNote();
        Account account = Application.App.getAccount();
        note.setId((int) System.currentTimeMillis());
        note.setUserName(account.getLoginName());
        note.setUserId(account.getLoginId());
        long time = System.currentTimeMillis();
        note.setTimeStamp(time);
        note.setTimeFormat(Utils.getFormatTime(time));
        if(format != null && format.size() > 0 &&Application.App.getPowerTaskManger().serverCheck()){
            for(int i = 1; i <format.size(); i++){
                requestData.addArg(format.get(i));
            }
            if(CmdDefine.CMD_REMOTE_RING_NOTE.equalsIgnoreCase(format.get(0))){
                requestData.setCode(RequestDataHelper.POWER_ConversationNote_RING);
                if(format.size()  >= 1) {
                    note.setContent(format.get(1));
                }else{
                    note.setContent("");
                }
                requestData.clearArgs();
                requestData.addArg(GsonUtils.mGson.toJson(note));
            }
        }else{
            note.setContent(input);
            requestData.setCode(RequestDataHelper.CODE_ConversationNote);
            requestData.addArg(GsonUtils.mGson.toJson(note));
        }
        result[0] = requestData;
        result[1] = note;
        return result;
    }
}
