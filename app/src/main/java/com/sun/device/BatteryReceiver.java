package com.sun.device;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;

import com.sun.personalconnect.Application;
import com.sun.personalconnect.BaseReceiver;

import java.util.Map;

/**
 * Created by guoyao on 2017/3/7.
 */
public class BatteryReceiver extends BaseReceiver<BatteryReceiver.BatteryListener>{

    private static Battery mBattery = new Battery();

    public interface BatteryListener{
        void onHighPower();
        void onLowPower();
    }

    public static Battery getBattery(){
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = Application.App.registerReceiver(null, ifilter);
        update(batteryStatus);
        return mBattery;
    }

    public static BatteryReceiver getInstance(){
        return BaseReceiver.getInstance(BatteryReceiver.class);
    }

    @Override
    public IntentFilter getIntentFilter(){
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        filter.addAction(Intent.ACTION_BATTERY_OKAY);
        filter.addAction(Intent.ACTION_BATTERY_LOW);
        return filter;
    }

    public static void register(Context context, BatteryListener listener){
        BaseReceiver.register(context, BatteryReceiver.class, listener);
    }

    public static void unregister(Context context){
        BaseReceiver.unregister(context, BatteryReceiver.class);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        update(intent);
        notifyListener();
    }

    public static void update(Intent intent){
        int extraStatus = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging  = extraStatus == BatteryManager.BATTERY_STATUS_CHARGING ||
                extraStatus == BatteryManager.BATTERY_STATUS_FULL;
        if(isCharging) {
            int chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
            boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
            boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;
            if(usbCharge)mBattery.setCharge(Battery.Charge.usb);
            if(acCharge) mBattery.setCharge(Battery.Charge.ac);
        }else{
            mBattery.setCharge(Battery.Charge.none);
        }
        //当前剩余电量
        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        mBattery.setLevel(level);
        //电量最大值
        int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        mBattery.setScale(scale);

        mBattery.setVoltage(intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0));  //电池电压
        mBattery.setTemperature(intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0));  //电池温度
    }


    public static Battery getBattery2(){
        Battery battery = null;
        if(Build.VERSION.SDK_INT >= 21) {
            battery = new Battery();
            Context context = Application.App.getApplicationContext();
            BatteryManager batteryManager = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
            batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER);
            batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_AVERAGE);
            batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW);

        }
        return battery;
    }

    private void notifyListener(){
        float percent = mBattery.getPercent();
        if(percent > 0.75f){
            for (Map.Entry<Context, BatteryListener> entry : getListeners().entrySet()) {
                entry.getValue().onHighPower();
            }
        }else if(mBattery.getCharge() == Battery.Charge.usb && percent> 4.5f){
            for (Map.Entry<Context, BatteryListener> entry : getListeners().entrySet()) {
                entry.getValue().onHighPower();
            }
        }else if(mBattery.getCharge() == Battery.Charge.ac && percent > 2.5){
            for (Map.Entry<Context, BatteryListener> entry : getListeners().entrySet()) {
                entry.getValue().onHighPower();
            }
        }else{
            for (Map.Entry<Context, BatteryListener> entry : getListeners().entrySet()) {
                entry.getValue().onLowPower();
            }
        }
    }
}
