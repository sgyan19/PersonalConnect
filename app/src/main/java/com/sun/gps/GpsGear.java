package com.sun.gps;

/**
 * Created by guoyao on 2017/3/7.
 */
public enum  GpsGear {
    High(0,1000,0), Normal(1,5000,1), Low(2,10000,5), None(3,-1,-1), Once(4,-1,-1);
    public static final int ID_High = 0;
    public static final int ID_Normal = 1;
    public static final int ID_Low = 2;
    public static final int ID_None = 3;
    public static final int ID_Once = 4;
    GpsGear(int id,int period, float minDistance){
        this.id = id;
        this.period = period;
        this.minDistance = minDistance;
    }
    public int id;
    public int period;
    public float minDistance;

    public static GpsGear get(int id){
        switch (id){
            case ID_High:
                return High;
            case ID_Low:
                return Low;
            case ID_Normal:
                return Normal;
            case ID_Once:
                return Once;
            case ID_None:
                default:
                return None;
        }
    }
}
