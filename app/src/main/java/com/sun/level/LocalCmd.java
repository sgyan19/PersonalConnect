package com.sun.level;

import com.sun.personalconnect.Application;

import java.util.List;

/**
 * Created by guoyao on 2017/1/5.
 */
public class LocalCmd {

    public static boolean handleCmd(List<String> cmds){
        if(cmds == null || cmds.size() <= 0){
            return false;
        }

        String code = cmds.get(0);
        if(CmdDefine.CMD_STOP_RING.equals(code)){
            Application.App.getLevelCenter().closeRingNote();
            return true;
        }
        return false;
    }
}
