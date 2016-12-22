package com.sun.conversation;

/**
 * Created by guoyao on 2016/12/22.
 */
public class CvsNoteHelper {
    public static String getStatusText(CvsNote note){
        switch (note.getSendStatus()){
            case CvsNote.STATUS_INIT:
                return "(正在发送)";
            case CvsNote.STATUS_FAL:
                return "(发送失败)";
            case CvsNote.STATUS_SUC:
            default:
                return "";
        }
    }
}
