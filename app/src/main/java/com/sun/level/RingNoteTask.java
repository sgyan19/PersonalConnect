package com.sun.level;

import android.os.Handler;

import com.sun.conversation.CvsNote;
import com.sun.personalconnect.Application;

/**
 * Created by guoyao on 2017/1/5.
 */
public class RingNoteTask {
    private final static int RING_MS = 20 * 1000;
    private Runnable mRingRun;
    private Handler mHandler;

    public RingNoteTask(Handler handler){
        this.mHandler = handler;
    }

    public void execute(){
        if(mRingRun == null){
            mRingRun = new Runnable() {
                @Override
                public void run() {
                    CvsNote note = Application.App.getCvsHistoryManager().getLastSendNote();
                    if(note == null || System.currentTimeMillis() - note.getTimeStamp() > RING_MS){
                        Application.App.getRing().start();
                    }
                }
            };
        }
        //mMainHandler.removeMessages();
        mHandler.removeCallbacks(mRingRun);
        mHandler.postDelayed(mRingRun, RING_MS);
    }

    public void close(){
        mHandler.removeCallbacks(mRingRun);
        Application.App.getRing().stop();
    }
}
