package com.sun.camera;

import android.graphics.Bitmap;
import android.graphics.Matrix;

/**
 * Created by guoyao on 2017/2/24.
 */
public class GraphUtils {
    public static Bitmap bitmapScale(Bitmap obj, int width, int height){
        if(obj == null || width <=0 || height <=0){
            return obj;
        }
        Matrix matrix = new Matrix();
        matrix.setScale((float)width / (float) obj.getWidth(), (float)height/ (float)obj.getHeight());
        return Bitmap.createBitmap(obj,0,0,obj.getWidth(), obj.getHeight(),matrix,true);
    }

    public static void rgb_2_Yuv(byte[] rgb, int width, int height, byte[] yuv) {
        int rgbIndex = 0;
        int yIndex = 0;
        int uvIndex = width * height;
        for (int j = 0; j < height; ++j) {
            for (int i = 0; i < width; ++i) {
                final int r = rgb[rgbIndex] & 0xFF;
                final int g = rgb[rgbIndex + 1] & 0xFF;
                final int b = rgb[rgbIndex + 2] & 0xFF;

                final int y = (int) (0.257 * r + 0.504 * g + 0.098 * b + 16);
                final int u = (int) (-0.148 * r - 0.291 * g + 0.439 * b + 128);
                final int v = (int) (0.439 * r - 0.368 * g - 0.071 * b + 128);

                yuv[yIndex++] = (byte) Math.max(0, Math.min(255, y));
                if ((i & 0x01) == 0 && (j & 0x01) == 0) {
                    yuv[uvIndex++] = (byte) Math.max(0, Math.min(255, v));
                    yuv[uvIndex++] = (byte) Math.max(0, Math.min(255, u));
                }

                rgbIndex += 4;
            }
        }
    }

    public static void rgba8888_2_nv21(byte[] rgba8888, int width, int height, byte[] nv21, float[] alpha){
        int size = width * height;
        if(rgba8888 == null || rgba8888.length < size * 4){
            return ;
        }

        if(nv21 == null || nv21.length < size + size /2){
            return;
        }

        int rgbIndex = 0;
        int yIndex = 0;
        int uvIndex = width * height;
        for (int j = 0; j < height; ++j) {
            for (int i = 0; i < width; ++i) {
                final int r = rgba8888[rgbIndex] & 0xFF;
                final int g = rgba8888[rgbIndex + 1] & 0xFF;
                final int b = rgba8888[rgbIndex + 2] & 0xFF;

                final int y = (int) (0.257 * r + 0.504 * g + 0.098 * b + 16);
                final int u = (int) (-0.148 * r - 0.291 * g + 0.439 * b + 128);
                final int v = (int) (0.439 * r - 0.368 * g - 0.071 * b + 128);

                nv21[yIndex++] = (byte) Math.max(0, Math.min(255, y));
                if ((i & 0x01) == 0 && (j & 0x01) == 0) {
                    nv21[uvIndex++] = (byte) Math.max(0, Math.min(255, v));
                    nv21[uvIndex++] = (byte) Math.max(0, Math.min(255, u));
                }
                rgbIndex += 4;
            }
        }

        if(alpha == null || alpha.length < size){
            return;
        }

        for(int i = 3,j = 0; i < size * 4;j++,i += 4){
            alpha[j] = (float)(rgba8888[i] & 0xff) / 255f;
        }
    }

    public static class Nv21 {
        int w;
        int h;
        byte[] data;
        float[] alpha;
        public Nv21(int w, int h){
            this.w = w;
            this.h = h;
            int tmp = w * h;
            data = new byte[tmp * 3 /2];
            alpha = new float[tmp];
        }

        public Nv21(int w, int h, byte[] data,float[] alpha){
            this.w = w;
            this.h = h;
            this.data = data;
            this.alpha = alpha;
        }

        public void setData(byte[] data){
            this.data = data;
        }
    }
    public static class Nv21MergeContext {
        Nv21 s;
        Nv21 d;
        int sYPos;
        int dYPos;
        int sUVPos;
        int dUVPos;
        int xCount;
        int yCount;
        Nv21MergeContext(Nv21 s, Nv21 d, int xMargin, int yMargin){
            if(xMargin % 2 != 0) xMargin += 1;
            if(yMargin % 2 != 0) yMargin += 1;
            this.s = s;
            this.d = d;
            int ss = s.w * s.h;
            int ds = d.w * d.h;
            if( xMargin < 0){
                dYPos = 0;
                sYPos = -xMargin;
                xCount = s.w + xMargin;
            }else if(xMargin + s.w > d.w){
                dYPos = xMargin;
                sYPos = 0;
                xCount = d.w - xMargin;
            }else{
                dYPos = xMargin;
                sYPos = 0;
                xCount = s.w;
            }
            dUVPos = dYPos;
            sUVPos = sYPos;

            if(yMargin < 0){
                // dYPos += 0; // 不变
                int tmp = (-yMargin) * s.w;
                sYPos += tmp;
                yCount = s.h + yMargin;
                dUVPos += ds; // 不变
                sUVPos +=  tmp / 2 + ss;
            }else if(yMargin + s.h > d.h){
                int tmp = yMargin * d.w;
                dYPos += tmp;
                //sYPos +=0 ;// 不变
                yCount = d.h - yMargin;
                dUVPos += tmp / 2 + ds;
                sUVPos += ss;
            }else{
                int tmp = yMargin * d.w;
                dYPos += tmp;
                //sYPos += 0;// 不变
                yCount = s.h;
                dUVPos += tmp / 2 + ds;
                sUVPos += ss;
            }
        }

        public void setDstData(byte[] data){
            d.data = data;
        }
    }

    public static byte[] nv21_2_nv12(byte[] nv21,int width ,int height){
        int size = width * height ;
//        int maxSize = size / 2 + size;
        // uv分量互换
        if(nv21 == null) return null;
        byte tmp;
        for(int i = size; i < nv21.length - 1; i +=2){
            tmp = nv21[i];
            nv21[i] = nv21[i+1];
            nv21[i + 1] = tmp;
        }
        return nv21;
    }

    public static void nv21_merge_nv21_core(byte[] s, byte[] d,  int sw,int dw,int sYPos, int dYPos, int sUVPos, int dUVPos, int xCount, int yCount, float[] alpha)
    {
        float alphaYU;
        for(int i = 0; i < yCount; i += 2){
            for(int j = 0; j < xCount; j += 2){
                if(alpha != null) {
                    d[dYPos + j] = alpha(s[sYPos + j], d[dYPos + j], alpha[sYPos + j]);
                    d[dYPos + j + 1] = alpha(s[sYPos + j + 1], d[dYPos + j + 1], alpha[sYPos + j + 1]);
                    d[dYPos + dw + j] = alpha(s[sYPos + sw + j], d[dYPos + dw + j], alpha[sYPos + sw + j]);
                    d[dYPos + dw + j + 1] = alpha(s[sYPos + sw + j + 1], d[dYPos + dw + j + 1], alpha[sYPos + sw + j + 1]);

                    alphaYU = alpha[sYPos + j] + alpha[sYPos + j + 1] + alpha[sYPos + sw + j] + alpha[sYPos + sw + j + 1];
                    if(alphaYU  > 0.8f){
                        d[dUVPos + j] = s[sUVPos + j];
                        d[dUVPos + j + 1] = s[sUVPos + j + 1];
                    }else{
//                        d[dUVPos + j] = d[dUVPos + j];
//                        d[dUVPos + j + 1] = d[dUVPos + j + 1];
                    }
//                    d[dUVPos + j] = alpha(s[sUVPos + j], d[dUVPos + j], alphaYU/4 );
//                    d[dUVPos + j + 1] = alpha(s[sUVPos + j + 1], d[dUVPos + j + 1], alphaYU/4);
                }else{
                    d[dYPos + j] = s[sYPos + j];
                    d[dYPos + j + 1] = s[sYPos + j + 1];
                    d[dYPos + dw + j] = s[sYPos + sw + j];
                    d[dYPos + dw + j + 1] = s[sYPos + sw + j + 1];

                    d[dUVPos + j] = s[sUVPos + j];
                    d[dUVPos + j + 1] = s[sUVPos + j + 1];
                }
            }
            dYPos += dw + dw;
            sYPos += sw + sw;
            dUVPos += dw;
            sUVPos += sw;
        }
    }
    public static void nv21_merge_nv21_Context(Nv21MergeContext context){
        nv21_merge_nv21_core(context.s.data, context.d.data, context.s.w, context.d.w, context.sYPos, context.dYPos, context.sUVPos, context.dUVPos, context.xCount, context.yCount, context.s.alpha);
    }
    public static Nv21MergeContext nv21_merge_nv21_Context(Nv21 s, Nv21 d, int xMargin, int yMargin){
        Nv21MergeContext context= new Nv21MergeContext(s,d,xMargin,yMargin);
        nv21_merge_nv21_core(context.s.data, context.d.data, context.s.w, context.d.w, context.sYPos, context.dYPos, context.sUVPos, context.dUVPos, context.xCount, context.yCount, context.s.alpha);
        return context;
    }
    public static Nv21MergeContext nv21_merge_nv21_Context(byte[] s, int sw, int sh, byte[] d, int dw, int dh, int xMargin, int yMargin, float[] alpha){
        if( s == null || d == null){
            return null;
        }
        int ss = sw * sh;
        int sbl = ss + ss / 2;
        int ds = dw * dh;
        int dbl = ds + ds / 2;
        if(s.length < sbl || d.length < dbl){
            return null;
        }
        Nv21 sNv21 = new Nv21(sw,sh, s, alpha);
        Nv21 dNv21 = new Nv21(dw,dh, d, null);
        return nv21_merge_nv21_Context(sNv21, dNv21, xMargin, yMargin);
    }

    public static void nv21_merge_nv21(byte[] s, int sw, int sh, byte[] d, int dw, int dh, int xMargin, int yMargin, float[] alpha){
        if(s == null || d == null ){
            return;
        }
        int ss = sw * sh;
        int sbl = ss + ss / 2;
        int ds = dw * dh;
        int dbl = ds + ds / 2;
        if(s.length < sbl || d.length < dbl|| (alpha != null && alpha.length < ss)){
            return;
        }
        if(xMargin % 2 != 0) xMargin += 1;
        if(yMargin % 2 != 0) yMargin += 1;

        int dYPos  = 0;
        int sYPos  = 0;
        int xCount;
        int yCount;
        int dUVPos;
        int sUVPos;

        if( xMargin < 0){
            dYPos = 0;
            sYPos += -xMargin;
            xCount = sw + xMargin;
        }else if(xMargin + sw > dw){
            dYPos += xMargin;
            sYPos = 0;
            xCount = dw - xMargin;
        }else{
            dYPos += xMargin;
            sYPos = 0;
            xCount = sw;
        }
        dUVPos = dYPos;
        sUVPos = sYPos;

        if(yMargin < 0){
            // dYPos += 0; // 不变
            int tmp = (-yMargin) * sw;
            sYPos += tmp;
            yCount = sh + yMargin;
            dUVPos += ds; // 不变
            sUVPos +=  tmp / 2 + ss;
        }else if(yMargin + sh > dh){
            int tmp = yMargin * dw;
            dYPos += tmp;
            //sYPos +=0 ;// 不变
            yCount = dh - yMargin;
            dUVPos += tmp / 2 + ds;
            sUVPos += ss;
        }else{
            int tmp = yMargin * dw;
            dYPos += tmp;
            //sYPos += 0;// 不变
            yCount = sh;
            dUVPos += tmp / 2 + ds;
            sUVPos += ss;
        }
        // region
        for(int i = 0; i < yCount; i += 2){
            for(int j = 0; j < xCount; j += 2){
                if(alpha != null) {
                    d[dYPos + j] = alpha(s[sYPos + j], d[dYPos + j], alpha[sYPos + j]);
                    d[dYPos + j + 1] = alpha(s[sYPos + j + 1], d[dYPos + j + 1], alpha[sYPos + j + 1]);
                    d[dYPos + dw + j] = alpha(s[sYPos + sw + j], d[dYPos + dw + j], alpha[sYPos + sw + j]);
                    d[dYPos + dw + j + 1] = alpha(s[sYPos + sw + j + 1], d[dYPos + dw + j + 1], alpha[sYPos + sw + j + 1]);
                }else{
                    d[dYPos + j] = s[sYPos + j];
                    d[dYPos + j + 1] = s[sYPos + j + 1];
                    d[dYPos + dw + j] = s[sYPos + sw + j];
                    d[dYPos + dw + j + 1] = s[sYPos + sw + j + 1];
                }
                d[dUVPos + j] = s[sUVPos + j];
                d[dUVPos + j + 1] = s[sUVPos + j + 1];
            }
            dYPos += dw + dw;
            sYPos += sw + sw;
            dUVPos += dw;
            sUVPos += sw;
        }
        //endregion
    }

    private static byte alpha(byte s, byte d, float a){
        return (byte)(((s &0xff) * a) + (d & 0xff)*(1- a));
    }

    public static void yuv420pRotate90(byte[] s, byte[] d, int width, int height){
        int wh ,uvHeight;
        wh = width * height;
        uvHeight = height / 2;

        int p = 0;
        for(int i = 0; i< width; i++){
            int nPos = wh - width;
            for(int j = 0; j < height;j ++){
//                d[p ++] = s[(height -1 - j)*width + i ] = s[wh - width -j*width + i]; // 旋转90度技巧是，j坐标取对称，然后交换ij
                d[p ++] = s[nPos + i];
                nPos -= width;
            }
        }
        for(int i = 0; i < width; i+=2){
            int nPos = wh - width + wh  /2;
            for(int j = 0; j < uvHeight; j++) {
                d[p] = s[nPos + i];
                d[p + 1] = s[nPos + i + 1];
                p += 2;
                nPos -= width;
            }
        }
    }

    public static void yuv420pRotate180(byte[] s, byte[] d, int width, int height){
        int wh ,uvHeight;
        wh = width * height;
        uvHeight = height / 2;

        int p = 0;
        int nPos = wh - 1;
        for(int i = 0; i< height; i++){
            for(int j = 0; j < width;j ++){
//                d[p++] = s[(height - 1 -i)* width + (width -1 -j)] = s[wh- i* width - 1 -j];// 旋转180度技巧是，i,j坐标都各自取对称，不交换ij
                d[p ++] = s[nPos - j];
            }
            nPos -= width;
        }
        nPos = wh - 2 + wh/2;
        for(int i = 0; i < uvHeight; i++){
            for(int j = 0; j < width; j+=2) {
                d[p] = s[nPos - j];
                d[p + 1] = s[nPos - j + 1];
                p += 2;
            }
            nPos -= width;
        }
    }

    public static void yuv420pRotate270(byte[] s, byte[] d, int width, int height){
        int wh ,uvHeight;
        wh = width * height;
        uvHeight = height / 2;

        int p = 0;
        for(int i = 0; i< width; i++){
            int nPos = width -1;
            for(int j = 0; j < height;j ++){
//                d[p++] = s[width * j + (width - 1 -i)] = s[width * j + width - 1 - i];// 旋转90度技巧是，i坐标取对称，然后交换ij
                d[p ++] = s[nPos - i];
                nPos += width;
            }
        }
        for(int i = 0; i < width; i+=2){
            int nPos = width - 2 + wh;
            for(int j = 0; j < uvHeight; j++) {
                d[p] = s[nPos - i];
                d[p + 1] = s[nPos - i + 1];
                p += 2;
                nPos += width;
            }
        }
    }
}
