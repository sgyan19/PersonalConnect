package com.sun.utils;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.Comparator;

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

    /**
     *
     * @param dir target dir
     * @param count lastKeep file count
     */
    public static int deleteOldFilesByCount(File dir, int count) throws IllegalArgumentException{
        if(dir == null || !dir.exists() || !dir.isDirectory()){
            throw new IllegalArgumentException("dir don't exists or is not a directory");
        }
        if(count <= 0){
            return 0;
        }
        File[] files = dir.listFiles();
        Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(File file, File t1) {
                return file.lastModified() - t1.lastModified() > 0 ? 1 : -1;
            }
        });
        int shouldDelete = files.length - count;
        for(int i = 0; i < shouldDelete ; i++){
            Log.d("socket", "delete file:" + files[i].getName());
            files[i].delete();
        }
        return shouldDelete;
    }
}
