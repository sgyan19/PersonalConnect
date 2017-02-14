package com.sun.conversation;

/**
 * Created by guoyao on 2017/1/17.
 */
public class EventNote {
    public static final int ACTION_DOWNLOAD_IMAGE = 0;
    public static final int ACTION_NEED_SEND = 1;
    public static final int ACTION_IMG_DETAIL = 2;

    private CvsNote mCurrentNote;
    private int mAction;
    public EventNote(CvsNote note, int ac){
        mCurrentNote = note;
        mAction = ac;
    }

    public CvsNote getCurrentNote() {
        return mCurrentNote;
    }

    public int getAction(){
        return mAction;
    }
}
