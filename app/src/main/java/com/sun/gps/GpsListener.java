package com.sun.gps;

/**
 * Created by sun on 2017/2/17.
 */

public interface GpsListener {
    void onGpsUpdate(GpsNote gpsNote);
    void onNonePermission();
    void onDeviceClose();
}
