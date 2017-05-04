package com.sun.personalconnect;

import com.sun.device.AnswerNote;
import java.util.Collection;
import java.util.HashMap;

/**
 * Created by guoyao on 2017/5/3.
 */
public class InfoKeeper {

    private HashMap<String, AnswerNote> mAnswers;
    public static InfoKeeper getInstance(){
        return Application.App.getInfoKeeper();
    }

    public InfoKeeper(){
        mAnswers = new HashMap<>();
    }

    public Collection<AnswerNote> getAnswers(){
        return mAnswers.values();
    }

    public void putAnswer(AnswerNote answerNote){
        mAnswers.put(answerNote.getDeviceId(), answerNote);
    }
}
