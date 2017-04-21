package com.sun.gps;

import android.location.Location;
import android.location.LocationManager;

import java.util.LinkedList;

/**
 * Created by guoyao on 2017/4/17.
 */
public class LocationTrajectory  {
    private static final int mMaxSize = 50;
    private static final int mUpdateMini = 10 * 1000;
    private LinkedList<Location> mNetwork;
    private LinkedList<Location> mGps;
    private LinkedList<Location> mLocations;

    public LocationTrajectory(){
        mNetwork = new LinkedList<>();
        mGps = new LinkedList<>();
        mLocations = new LinkedList<>();
    }

    @Override
    protected void finalize() throws Throwable {
        mNetwork.clear();
        mGps.clear();
        mLocations.clear();
        super.finalize();
    }

    public boolean update(Location location){
        if(location == null){
            return false;
        }
        if(LocationManager.GPS_PROVIDER.equals(location.getProvider())){
            mGps.addLast(location);
            mLocations.add(location);
        }else if(LocationManager.NETWORK_PROVIDER.equals(location.getProvider())){
            if(mLocations.size() > 0) {
                if (LocationManager.GPS_PROVIDER.equals(mLocations.getLast().getProvider())) {
                    if (location.getTime() - mLocations.getLast().getTime() < mUpdateMini) {
                        return false;
                    }
                }
            }
            mNetwork.addLast(location);
            mLocations.add(location);
        }
        while(mLocations.size() > mMaxSize){
            removeFirst();
        }
        return true;
    }

    public void remove(Location location){
        mLocations.remove(location);
        if(LocationManager.GPS_PROVIDER.equals(location.getProvider())){
            mGps.remove(location);
        }else if(LocationManager.NETWORK_PROVIDER.equals(location.getProvider())){
            mNetwork.remove(location);
        }
    }

    public void removeFirst(){
        Location location = mLocations.removeFirst();
        if(LocationManager.GPS_PROVIDER.equals(location.getProvider())){
            mGps.remove(location);
        }else if(LocationManager.NETWORK_PROVIDER.equals(location.getProvider())){
            mNetwork.remove(location);
        }
    }
}
