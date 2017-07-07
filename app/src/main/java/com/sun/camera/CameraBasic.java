package com.sun.camera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.util.Size;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class CameraBasic {
    
    private static final String TAG = CameraBasic.class.getSimpleName();

    public interface Callback {
        void onPreviewSize(Size size);
        void onConfigured();
        void onCaptureCompleted(File file);
    }

    public static int DEFAULT_NUMBER_OF_CAMERAS = 1;

    public static int CAMERA_FACING_BACK = CameraInfo.CAMERA_FACING_BACK;

    public static int CAMERA_FACING_FRONT = CameraInfo.CAMERA_FACING_FRONT;
    
    private int MIN_SIZE_NORMAL = 720;
    
    private int MIN_SIZE_MI = 480;
    
    private int MIN_SIZE = MIN_SIZE_NORMAL;

//    private static CameraBasic sInstance = new CameraBasic();
    
    private Camera mCamera;

//    public static CameraBasic getInstance() {
//        return sInstance;
//    }

    private AsyncTask<Void, Void, Camera> mCameraOpeningTask;

    private SurfaceHolder mSurfaceHolder;

    private SurfaceTexture mSurfaceTexture;

    private static final int DEFAULT_WIDTH = 800;
    private static final int DEFAULT_HEIGHT = 480;

    private  int _width = 800;
    private  int _height = 480;

    private boolean mSetDisplay = false;

    private Context mContext;

    private boolean mMuteShutterSound = false;
    private int mFacing = CAMERA_FACING_BACK;
    private Callback mOutCallback;
    private boolean mAutoFocus;
    private boolean mInitialized = false;
    private SensorManager mSensorManager;
    private Sensor mAccel;
    private float mLastX = 0;
    private float mLastY = 0;
    private float mLastZ = 0;


    public void CameraBasic(Context context){
        mContext = context;
    }

    public void setDisplay(SurfaceHolder holder){
        mSurfaceHolder = holder;
        mSetDisplay = true;
        onResume();
    }

    public void setDisplay(){
        setDisplay(null, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    public void setDisplay(SurfaceTexture surfaceTexture){
        setDisplay(surfaceTexture, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    public void setDisplay(SurfaceTexture surfaceTexture, int width, int height){
        mSurfaceTexture = surfaceTexture;
        mSetDisplay = true;
        _width = width;
        _height = height;
        onResume();
    }

    public void onResume(){
        if (null == mSurfaceHolder) {
            if(mSurfaceTexture == null) {
                if(!mSetDisplay){
                    return;
                }
                mSurfaceTexture = new SurfaceTexture(10);
            }
        }
        if(mCamera == null){
            open();
        }
        if(mCamera == null){
            return;
        }

        mSensorManager = (SensorManager) mContext.getSystemService(Context.
                SENSOR_SERVICE);
        mSensorManager.registerListener(mSensorHandler, mAccel,
                SensorManager.SENSOR_DELAY_UI);
        try {
            if(mSurfaceHolder != null) {
                mCamera.setPreviewDisplay(mSurfaceHolder);
            }else{
                mCamera.setPreviewTexture(mSurfaceTexture);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void onPause(){

    }

    public void muteShutterSound(boolean isMute){
        mMuteShutterSound = isMute;
    }

    public void setFacing(int facing){
        mFacing = facing;
    }

    public void setCaptureCallback(Callback callback){
        mOutCallback = callback;
    }

    /**
     * Initiate a still image capture.
     */
    public void takePicture(){

    }

    private Camera.AutoFocusCallback mAutoFocusCallback = new Camera.AutoFocusCallback() {

        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            // TODO Auto-generated method stub

            mAutoFocus = true;

            if (mCamera != null) {
                try {
                    mCamera.cancelAutoFocus();
                }catch (Exception e){
                    Log.e(TAG , e.toString());
                }
            }

            if(success) {
                Log.w(TAG, "myAutoFocusCallback: success...");
            } else {
                Log.w(TAG, "myAutoFocusCallback: failed...");
            }
        }
    };

    private SensorEventListener mSensorHandler = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            if (!mInitialized){
                mLastX = x;
                mLastY = y;
                mLastZ = z;
                mInitialized = true;
            }
            float deltaX  = Math.abs(mLastX - x);
            float deltaY = Math.abs(mLastY - y);
            float deltaZ = Math.abs(mLastZ - z);

            if (deltaX > .5 && mAutoFocus){ //AUTOFOCUS (while it is not autofocusing)
                mAutoFocus = false;
                setCameraFocus();
            }
            if (deltaY > .5 && mAutoFocus){ //AUTOFOCUS (while it is not autofocusing)
                mAutoFocus = false;
                setCameraFocus();
            }
            if (deltaZ > .5 && mAutoFocus){ //AUTOFOCUS (while it is not autofocusing) */
                mAutoFocus = false;
                setCameraFocus();
            }

            mLastX = x;
            mLastY = y;
            mLastZ = z;
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    public void setCameraFocus(){
        if(mCamera == null){
            return;
        }
        try {
            if (mCamera.getParameters().getFocusMode().equals(mCamera.getParameters().FOCUS_MODE_AUTO) ||
                    mCamera.getParameters().getFocusMode().equals(mCamera.getParameters().FOCUS_MODE_MACRO)) {
                mCamera.autoFocus(mAutoFocusCallback);
            }
        }catch (Exception e){
            Log.e(TAG, "setCameraFocus()," + e.toString());
        }
    }

    private void open() {
        Camera camera = null;

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {

            int numberOfCameras = Camera.getNumberOfCameras();
            CameraInfo cameraInfo = new CameraInfo();
            for (int cameraId = 0; cameraId < numberOfCameras; ++cameraId) {
                Camera.getCameraInfo(cameraId, cameraInfo);

                if (mFacing != cameraInfo.facing) {
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
    }

    public void release(){
        if(mCamera != null){
            try {
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public Camera getOpenedCamera(){
        return mCamera;
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
                open();
                return mCamera;
            }

            @Override
            protected void onCancelled(Camera camera) {
                if (null != camera) {
                    camera.release();
                }
            }

            @Override
            protected void onPostExecute(Camera camera) {

                if (null != mOutCallback) {
                    mOutCallback.onConfigured();
                }
                if(listener != null){
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