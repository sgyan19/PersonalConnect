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
            core.setLooping(true);
        }
        if(!core.isPlaying()) {
            core.start();
        }
    }

    public void stop(){
        if(core != null){
            core.stop();
            core.release();
            core = null;
        }
    }
}
