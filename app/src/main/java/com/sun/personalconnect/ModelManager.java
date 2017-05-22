package com.sun.personalconnect;

import com.sun.gps.Gps;
import com.sun.gps.GpsGear;
import com.sun.gps.GpsListener;

/**
 * Created by guoyao on 2017/5/22.
 */

public class ModelManager {

    private Gps mGps;

    public ModelManager(){
        mGps = new Gps();
    }

    public static ModelManager getInstance(){
        return Application.App.getModelManager();
    }

    public void setGpsStatus(GpsGear gpsGear){
        mGps.changeGpsGear(gpsGear);
    }

    public void addGpsWeakListener(GpsListener listener){
        mGps.addWeakListener(listener);
    }

    public void addGpsHardListener(GpsListener listener){
        mGps.addHardListener(listener);
    }
}
