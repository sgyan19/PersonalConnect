package com.sun.camera;

import android.annotation.SuppressLint;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CameraManager {
    
    private static final String TAG = CameraManager.class.getSimpleName();

    public static int DEFAULT_NUMBER_OF_CAMERAS = 1;

    public static int CAMERA_FACING_BACK = CameraInfo.CAMERA_FACING_BACK;

    public static int CAMERA_FACING_FRONT = CameraInfo.CAMERA_FACING_FRONT;
    
    private int MIN_SIZE_NORMAL = 720;
    
    private int MIN_SIZE_MI = 480;
    
    private int MIN_SIZE = MIN_SIZE_NORMAL;

//    private static CameraManager sInstance = new CameraManager();
    
    private Camera mCamera;

//    public static CameraManager getInstance() {
//        return sInstance;
//    }

    private AsyncTask<Void, Void, Camera> mCameraOpeningTask;

    private Lock mCameraLock;

    @SuppressLint("DefaultLocale") private CameraManager() {
//    	String model = android.os.Build.MODEL;
//    	if (StringUtil.isNullOrEmpty(model)) {
//    		MIN_SIZE = MIN_SIZE_MI;
//    	} else {
//    		if (model.toUpperCase().contains("MI")) {
//    			if (StringUtil.getFirstNumber(model) < 3) {
//    				MIN_SIZE = MIN_SIZE_MI;
//    			}
//    		}
//    	}

        mCameraLock = new ReentrantLock();
    };
    private static Object mUser;
    public Camera open(Object user , final int facing) {
        Camera camera = null;

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {

            int numberOfCameras = Camera.getNumberOfCameras();
            CameraInfo cameraInfo = new CameraInfo();
            for (int cameraId = 0; cameraId < numberOfCameras; ++cameraId) {
                Camera.getCameraInfo(cameraId, cameraInfo);

                if (facing != cameraInfo.facing) {
                    continue;
                }
                try {
                	camera = Camera.open(cameraId);
                } catch (Exception e) {
                	if (mCamera != null){
                		mCamera.stopPreview();
                		mCamera.release();
                		mCamera = null;
                		camera = Camera.open(cameraId);
                	}
                }
            }
//        }

        if (null == camera) {
            camera = Camera.open();
        }
        mCamera = camera;
        mUser = user;
        return camera;
    }

    public void releaseCamera(Object user, boolean force){
        if(mUser != user && !force) {
            return;
        }
        if(mCamera != null){
            mCameraLock.lock();
            try {
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            }finally {
                mCameraLock.unlock();
            }
        }
        mUser = null;
    }

    public void unAllowRelease(){
        mCameraLock.lock();
    }

    public void allowRelease(){
        mCameraLock.unlock();
    }

    public Camera getOpenedCamera(){
        return mCamera;
    }

    public boolean isCameraMine(Object obj){
        return mUser == obj;
    }

    public boolean isCameraReady(){
        return mCamera != null;
    }

    public void openAsync(final int facing, final CameraOpeningListener listener) {
        if (null != mCameraOpeningTask) {
            if (!mCameraOpeningTask.cancel(true)) {
                return;
            }
        }

        mCameraOpeningTask = new AsyncTask<Void, Void, Camera>() {

            @Override
            protected Camera doInBackground(Void... params) {
                Camera camera = open(this,facing);

                return camera;
            }

            @Override
            protected void onCancelled(Camera camera) {
                if (null != camera) {
                    camera.release();
                }
            }

            @Override
            protected void onPostExecute(Camera camera) {

                if (null != listener) {
                    listener.onOpeningComplete(camera);
                }

                mCameraOpeningTask = null;
            }
        };
        mCameraOpeningTask.execute();
    }

    public int getNumberOfCameras() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD ? Camera.getNumberOfCameras() : DEFAULT_NUMBER_OF_CAMERAS;
    }

    public Camera.Size getMinSize(Parameters params) {
        List<Camera.Size> sizes = params.getSupportedPreviewSizes();

        Camera.Size ms = sizes.get(0);
        ms.height = 0;
        ms.width = 0;
        for (Camera.Size s : sizes) {
            Log.w(TAG, "s width: " + s.width + "; height: " + s.height);
            if((s.width > ms.width && s.width <= 800 && s.height<=480) || (s.height > ms.height && s.height <= 480)){
                ms = s;
            }
        }
        
        Log.w(TAG, "Size width: " + ms.width + "; height: " + ms.height);
        
        return ms;
    }

    public Camera.Size getMinSize(Parameters params, int width, int height) {
        List<Camera.Size> sizes = params.getSupportedPreviewSizes();

        Camera.Size ms = sizes.get(0);
        ms.height = 0;
        ms.width = 0;
        for (Camera.Size s : sizes) {
            Log.w(TAG, "s width: " + s.width + "; height: " + s.height);
            if((s.width > ms.width && s.width <= width && s.height<= height) || (s.height > ms.height && s.height <= height)){
                ms = s;
            }
        }

        Log.w(TAG, "Size width: " + ms.width + "; height: " + ms.height);

        return ms;
    }

    private Camera.Size getNear480P(Parameters params){
        List<Camera.Size> sizes = params.getSupportedPreviewSizes();

        Camera.Size ms = sizes.get(0);
        for (Camera.Size s : sizes) {
            Log.w(TAG, "s width: " + s.width + "; height: " + s.height);
            if((s.width > ms.width && s.width <= 800) || (s.height > ms.height && s.height <= 480)){
                ms = s;
            }
        }

        Log.w(TAG, "Size width: " + ms.width + "; height: " + ms.height);

        return ms;
    }

    private Camera.Size getNear720P(Parameters params){
        List<Camera.Size> sizes = params.getSupportedPreviewSizes();

        Camera.Size ms = sizes.get(0);
        for (Camera.Size s : sizes) {
            Log.w(TAG, "s width: " + s.width + "; height: " + s.height);
            if((s.width > ms.width && s.width <= 1280) || (s.height > ms.height && s.height <= 720)){
                ms = s;
            }
        }

        Log.w(TAG, "Size width: " + ms.width + "; height: " + ms.height);

        return ms;
    }

    
    public Camera.Size getMinSize(Parameters params, int size) {
        List<Camera.Size> sizes = params.getSupportedPreviewSizes();

        Camera.Size minLarger = sizes.get(0);
        Camera.Size maxLess = sizes.get(0);
        
        for (Camera.Size s : sizes) {
            Log.w(TAG, "s width: " + s.width + "; height: " + s.height);
            int frameSize = s.width * s.height;
            
            if (s.width >= MIN_SIZE && (minLarger.width < MIN_SIZE || s.width < minLarger.width || s.height < minLarger.height) && frameSize <= size) {
                minLarger = s;
            }
            
            if (s.width <= MIN_SIZE && (maxLess.width > MIN_SIZE || s.width > maxLess.width || s.height > maxLess.height) && frameSize <= size) {
            	maxLess = s;
            }
        }
        
        Log.w(TAG, "MinLarger Size width: " + minLarger.width + "; height: " + minLarger.height);
        Log.w(TAG, "MaxLess Size width: " + maxLess.width + "; height: " + maxLess.height);
        
        Camera.Size ms = maxLess.width >= minLarger.width ? maxLess : minLarger;
        Log.w(TAG, "Size width: " + maxLess.width + "; height: " + maxLess.height);
        
        return ms;
    }

    interface CameraOpeningListener {

        void onOpeningComplete(Camera camera);
    }
}