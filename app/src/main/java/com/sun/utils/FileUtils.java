package com.sun.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * Created by guoyao on 2017/1/16.
 */
public class FileUtils {

    /**
     * 拷贝文件
     *
     * @param src
     * @param dst
     *            存放文件夹地址,由"/"结尾
     * @return
     */
    public static void copyFile(File src, File dst) throws IOException{
        FileInputStream fi = null;
        FileOutputStream fo = null;
        FileChannel in = null;
        FileChannel out = null;
        try {
            fi = new FileInputStream(src);
            fo = new FileOutputStream(dst);
            in = fi.getChannel();
            out = fo.getChannel();
            in.transferTo(0, in.size(), out);
        }  finally {
            try {
                if(fi != null) {
                    fi.close();
                }
                if(in != null) {
                    in.close();
                }
                if(fo != null) {
                    fo.close();
                }
                if(out != null) {
                    out.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
