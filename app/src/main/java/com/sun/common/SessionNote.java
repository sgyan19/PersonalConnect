package com.sun.common;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by guoyao on 2017/5/17.
 */

public class SessionNote {
    public static final int TYPE_ALL = 0;
    public static final int TYPE_DEVICE = 1;
    public static final int TYPE_USER_NAME = 2;
    public static final int TYPE_LEVEL = 3;

    private int sessionType = 0;

    protected SessionNote(){

    }
    protected SessionNote(SessionNote note){
        this.sessionType = note.sessionType;
        this.sessionCondition.addAll(note.sessionCondition);
    }

    private List<String> sessionCondition = new ArrayList<>();

    public int getSessionType() {
        return sessionType;
    }

    public void setSessionType(int sessionType) {
        this.sessionType = sessionType;
    }

    public List<String> getSessionCondition() {
        return sessionCondition;
    }

    public void addSessionCondition(String condition) {
        sessionCondition.add(condition);
    }

    public void setSession(SessionNote note){
        this.sessionType = note.sessionType;
        this.sessionCondition.addAll(note.sessionCondition);
    }
}
