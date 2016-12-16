package com.sun.utils;

import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.sun.personalconnect.Application;

/**
 * Created by guoyao on 2016/12/13.
 */
public class ToastUtils {
    private static Toast mLastToast;
    private static Handler mMainHandler;
    private static String mLastText;
    private static int mLastDuration;
    public static synchronized void show(String text, int duration){
        if(Looper.myLooper() != Looper.getMainLooper()){
            if(mMainHandler == null){
                mMainHandler = new Handler(Looper.getMainLooper());
            }
            mLastText = text;
            mLastDuration = duration;
            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    show(mLastText, mLastDuration);
                }
            });
        }else {
            if (mLastToast != null) {
                //mLastToast.cancel();
                mLastToast.setText(text);
                mLastToast.setDuration(duration);
            } else {
                mLastToast = Toast.makeText(Application.getContext(), text, duration);
            }
            mLastToast.show();
        }
    }

}
