package com.sun.device;

import com.sun.utils.Utils;

/**
 * Created by guoyao on 2017/5/17.
 */

public class NoteHelper {

    public static AnswerNote fillAnswerTime(AnswerNote note){
        note.setTime(Utils.getFormatTime(System.currentTimeMillis()));
        return note;
    }
}
