package com.sun.device;

import com.sun.common.SessionNote;
import com.sun.level.OrderNote;
import com.sun.level.UpdateOrderNote;
import com.sun.personalconnect.Application;
import com.sun.utils.Utils;

import java.io.File;

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
            ((AnswerNote)answer).setVersionCode(Application.App.VersionCode);
            ((AnswerNote)answer).setDeviceId(Application.App.getDeviceId());
            ((AnswerNote)answer).setUserId(Application.App.getAccount().getLoginId());
            ((AnswerNote)answer).setUserName(Application.App.getAccount().getLoginName());
        }else if(ask.getType() == AskNote.TYPE_DETAIL){
            answer = DeviceDumper.dump();
            answer.setSession(ask);
        }
        return answer;
    }

    public static UpdateOrderNote makeUpdateOrderNote(AnswerNote... notes){
        UpdateOrderNote note = new UpdateOrderNote();
        note.setVersionCode(Application.App.VersionCode);
        File apkFile = new File(Application.App.getPackageResourcePath());
        note.setApkName(apkFile.getName());
        note.setForce(false);
        note.setSessionType(SessionNote.TYPE_DEVICE);
        note.addSessionCondition(Application.App.getDeviceId());
        for(AnswerNote item : notes){
            note.addSessionCondition(item.getDeviceId());
        }
        return note;
    }
}
