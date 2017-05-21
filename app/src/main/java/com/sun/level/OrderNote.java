package com.sun.level;

import com.sun.common.SessionNote;

/**
 * Created by sun on 2017/5/20.
 */

public class OrderNote extends SessionNote {
    private boolean force;

    public boolean isForce() {
        return force;
    }

    public void setForce(boolean force) {
        this.force = force;
    }
}
