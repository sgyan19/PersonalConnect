package com.sun.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.sun.personalconnect.Application;

/**
 * shared perference的工具箱，直接用
 *
 * @author sunxiao
 */
public class SharedPreferencesUtil {

    private final static String mDefaultName = "SharedPreferencesUtil";

    /*
     * ===================== put functions =====================
     */

    public static void putInt(String key, int value) {
        putValue(mDefaultName, key, value);
    }

    public static void putBoolean(String key, boolean value) {
        putValue(mDefaultName, key, value);
    }

    public static void putString(String key, String value) {
        putValue(mDefaultName, key, value);
    }

    public static void putLong(String key, long value) {
        putValue(mDefaultName, key, value);
    }

    public static void putFloat(String key, float value) {
        putValue(mDefaultName, key, value);
    }

    public static void putValue(String name, String key, int value) {
        Editor sp = Application.getContext().getSharedPreferences(name, Context.MODE_PRIVATE).edit();
        sp.putInt(key, value);
        sp.apply();
    }

    public static void putValue(String name, String key, boolean value) {
        Editor sp = Application.getContext().getSharedPreferences(name, Context.MODE_PRIVATE).edit();
        sp.putBoolean(key, value);
        sp.apply();
    }

    public static void putValue(String name, String key, String value) {
        Editor sp = Application.getContext().getSharedPreferences(name, Context.MODE_PRIVATE).edit();
        sp.putString(key, value);
        sp.apply();
    }

    public static void putValue(String name, String key, long value) {
        Editor sp = Application.getContext().getSharedPreferences(name, Context.MODE_PRIVATE).edit();
        sp.putLong(key, value);
        sp.apply();
    }

    public static void putValue(String name, String key, float value) {
        Editor sp = Application.getContext().getSharedPreferences(name, Context.MODE_PRIVATE).edit();
        sp.putFloat(key, value);
        sp.apply();
    }

    /*
     * ===================== get functions =====================
     */
    public static int getInt(String key) {
        return getValue(mDefaultName, key, -1);
    }

    public static int getValue(String name, String key, int defValue) {
        SharedPreferences sp = Application.getContext().getSharedPreferences(name, Context.MODE_PRIVATE);
        int value = sp.getInt(key, defValue);
        return value;
    }

    public static boolean getBoolean(String key) {
        return getValue(mDefaultName, key, false);
    }

    public static boolean getBoolean(String key, boolean defValue) {
        return getValue(mDefaultName, key, defValue);
    }

    public static boolean getValue(String name, String key, boolean defValue) {
        SharedPreferences sp = Application.getContext().getSharedPreferences(name, Context.MODE_PRIVATE);
        boolean value = sp.getBoolean(key, defValue);
        return value;
    }

    public static String getString(String key) {
        return getValue(mDefaultName, key, null);
    }

    public static String getValue(String name, String key, String defValue) {
        SharedPreferences sp = Application.getContext().getSharedPreferences(name, Context.MODE_PRIVATE);
        String value = sp.getString(key, defValue);
        return value;
    }

    public static long getLong(String key) {
        return getValue(mDefaultName, key, -1L);
    }

    public static long getValue(String name, String key, long defValue) {
        SharedPreferences sp = Application.getContext().getSharedPreferences(name, Context.MODE_PRIVATE);
        long value = sp.getLong(key, defValue);
        return value;
    }

    public static float getFloat(String key) {
        return getValue(mDefaultName, key, 0.0f);
    }

    public static float getValue(String name, String key, float defValue) {
        SharedPreferences sp = Application.getContext().getSharedPreferences(name, Context.MODE_PRIVATE);
        float value = sp.getFloat(key, defValue);
        return value;
    }

    public static void clearDefault() {
        clear(mDefaultName);
    }

    public static void clear(String name) {
        SharedPreferences sp = Application.getContext().getSharedPreferences(name, Context.MODE_PRIVATE);
        Editor editor = sp.edit();
        editor.clear();
        editor.apply();
    }

    public static void ObserverSharedPreferenceChange(final String k, final OnPreferencesChangedListener l, final boolean once){
        SharedPreferences.OnSharedPreferenceChangeListener listener = new SharedPreferences.OnSharedPreferenceChangeListener(){
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if(k.equals(key)) {
                    if (once) {
                        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
                    }
                    String value = sharedPreferences.getString(key, "");
                    l.onChanged(key, value);
                }
            }
        };
        Application.getContext().getSharedPreferences(mDefaultName,Context.MODE_PRIVATE).registerOnSharedPreferenceChangeListener(listener);
    }

    public interface OnPreferencesChangedListener{
        void onChanged(String key, String value);
    }
}
