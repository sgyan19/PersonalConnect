package com.sun.power;

import android.media.MediaPlayer;
import android.media.RingtoneManager;

import com.sun.personalconnect.Application;

import java.io.IOException;

/**
 * Created by guoyao on 2017/1/4.
 */
public class Ring {
    private MediaPlayer core;

    public void start(){
        if(core == null){
            core = MediaPlayer.create(Application.getContext(), RingtoneManager.getActualDefaultRingtoneUri(Application.getContext(),
                    RingtoneManager.TYPE_RINGTONE));
        }
        if(!core.isPlaying()) {
            core.setLooping(true);
            try {
                core.prepare();
            } catch (IllegalStateException | IOException e) {
                e.printStackTrace();
            }
            core.seekTo(0);
            core.start();
        }
    }

    public void stop(){
        if(core != null){
            core.pause();
        }
    }
    public void release(){
        if(core != null){
            core.stop();
            core.release();
        }
    }
}
