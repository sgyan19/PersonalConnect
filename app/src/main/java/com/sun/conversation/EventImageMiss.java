package com.sun.conversation;

/**
 * Created by guoyao on 2017/1/17.
 */
public class EventImageMiss {
    private CvsNote mCurrentNote;

    public EventImageMiss(CvsNote note){
        mCurrentNote = note;
    }

    public CvsNote getCurrentNote() {
        return mCurrentNote;
    }
}
