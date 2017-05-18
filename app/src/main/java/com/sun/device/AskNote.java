package com.sun.device;

import com.sun.common.SessionNote;

/**
 * Created by guoyao on 2017/4/21.
 */
public class AskNote extends SessionNote {
    public static final int TYPE_EASY = 0;
    public static final int TYPE_DETAIL = 1;

    private int type;

    public AskNote(){
        this(TYPE_EASY);
    }

    public AskNote(int type){
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
