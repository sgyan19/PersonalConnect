package com.sun.utils;

import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by guoyao on 2016/12/13.
 */
public class Utils {
    /**
     * md5
     *
     * @param str
     * @return
     */
    public static String md5(String str) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            messageDigest.update(str.getBytes("UTF-8"));
            byte[] byteArray = messageDigest.digest();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < byteArray.length; i++) {
                if (Integer.toHexString(0xFF & byteArray[i]).length() == 1) {
                    sb.append("0").append(Integer.toHexString(0xFF & byteArray[i]));
                } else {
                    sb.append(Integer.toHexString(0xFF & byteArray[i]));
                }
            }

            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return str;
    }

    public static String generatePlayTime(long time) {
        if (time % 1000 >= 500) {
            time += 1000;
        }
        int totalSeconds = (int) (time / 1000);
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        return hours > 0 ? String.format(Locale.CHINA, "%02d:%02d:%02d", hours, minutes, seconds) : String.format(
                Locale.CHINA, "%02d:%02d", minutes, seconds);
    }

    private static SimpleDateFormat simpleDateFormat;
    public static String getFormatTime(long time){
        if(simpleDateFormat == null){
            simpleDateFormat = new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH);
        }
        return simpleDateFormat.format(time);
    }
}
