package com.sun.power;

import android.os.Handler;
import android.os.Looper;

import com.sun.conversation.CvsNote;
import com.sun.personalconnect.Application;

/**
 * Created by guoyao on 2017/1/5.
 */
public class RingNoteTask {
    private final static int RING_MS = 20 * 1000;
    private Handler mMainHandler;
    private Runnable mRingRun;
    public void execute(){
        if(mMainHandler == null){
            mMainHandler = new Handler(Looper.getMainLooper());
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
        mMainHandler.removeCallbacks(mRingRun);
        mMainHandler.postDelayed(mRingRun, RING_MS);
    }

    public void close(){
        mMainHandler.removeCallbacks(mRingRun);
        Application.App.getRing().stop();
    }
}
