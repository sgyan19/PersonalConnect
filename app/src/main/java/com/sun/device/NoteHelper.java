package com.sun.device;

import com.sun.common.SessionNote;
import com.sun.personalconnect.Application;
import com.sun.utils.Utils;

/**
 * Created by guoyao on 2017/5/17.
 */

public class NoteHelper {

    public static SessionNote makeAnswer(AskNote ask){
        SessionNote answer = null;
        if (ask.getType() == AskNote.TYPE_EASY ) {
            answer = new AnswerNote();
            ((AnswerNote)answer).setTime(Utils.getFormatTime(System.currentTimeMillis()));
//            ((AnswerNote)answer).setSession(ask);
            ((AnswerNote)answer).setDeviceId(Application.App.getDeviceId());
            ((AnswerNote)answer).setUserId(Application.App.getAccount().getLoginId());
            ((AnswerNote)answer).setUserName(Application.App.getAccount().getLoginName());
        }else if(ask.getType() == AskNote.TYPE_DETAIL){
            answer = DeviceDumper.dump();
            answer.setSession(ask);
        }
        return answer;
    }
}
