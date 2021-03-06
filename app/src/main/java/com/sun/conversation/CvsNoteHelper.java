package com.sun.conversation;

import com.sun.personalconnect.Application;

/**
 * Created by guoyao on 2016/12/22.
 */
public class CvsNoteHelper {
    public static String getStatusText(CvsNote note){
        switch (note.getSendStatus()){
            case CvsNote.STATUS_SENDING:
                return "(正在发送)";
            case CvsNote.STATUS_FAL:
                return "(发送失败)";
            case CvsNote.STATUS_SUC:
            default:
                return "";
        }
    }

    public static int getUserColor(CvsNote note){
        if(Application.App.getAccount().isLoginAccount(note.getUserId())){
            return 0xff008040;
        }else {
            return 0xff0000ff;
        }
    }
}
