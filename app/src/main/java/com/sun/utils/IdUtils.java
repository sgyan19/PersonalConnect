package com.sun.utils;

import java.util.UUID;

/**
 * Created by sun on 2017/5/13.
 */

public class IdUtils {

    public static String make(){
        return UUID.randomUUID().toString();
    }
}
