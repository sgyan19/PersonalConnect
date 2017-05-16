package com.sun.device;

/**
 * Created by guoyao on 2017/3/7.
 */
public class Battery {
    public enum Charge{
        none,usb,ac, unknown
    }

    private Charge charge = Charge.unknown;
    private int level = 0;
    private int scale = 1;
    private int voltage = 0;
    private int temperature = 0;

    public Charge getCharge() {
        return charge;
    }

    public void setCharge(Charge charge) {
        this.charge = charge;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getScale() {
        return scale;
    }

    public void setScale(int scale) {
        this.scale = scale;
    }

    public float getPercent() {
        return level / (float)scale;
    }

    public int getVoltage() {
        return voltage;
    }

    public void setVoltage(int voltage) {
        this.voltage = voltage;
    }

    public int getTemperature() {
        return temperature;
    }

    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }

    @Override
    public String toString(){
        return String.format("%s,%d",charge, level);
    }
}
