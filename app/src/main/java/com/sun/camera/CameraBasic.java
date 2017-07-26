package com.sun.camera;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.graphics.YuvImage;
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
import android.view.Surface;
import android.view.SurfaceHolder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class CameraBasic {
    
    private static final String TAG = "CameraBasic";

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

    private static final int DEFAULT_WIDTH = 1920;
    private static final int DEFAULT_HEIGHT = 1080;

    private  int _width = 1920;
    private  int _height = 1080;

    private boolean mSetDisplay = false;

    private Context mContext;

    private boolean mMuteShutterSound = false;
    private int mFacing = CAMERA_FACING_BACK;
    private Callback mOutCallback;
    private boolean mAutoFocus = true;
    private boolean mInitialized = false;
    private SensorManager mSensorManager;
    private Sensor mAccel;
    private float mLastX = 0;
    private float mLastY = 0;
    private float mLastZ = 0;
    private Size mPreviewSize;
    private int mAutoTakePicture = 0;
    private int mPreviewFormat;
    private boolean mAutoClose = false;
    private boolean mAutoFocusTake = false;

    private boolean mAutoFocusSupported;
    private boolean mZoomSupported;
    private boolean mSmoothZoomSupported;
    private int mMaxZoom;
    private File mFile;
    private CameraInfo mCameraInfo;

    private int mDegree;
    private byte[] mRotateBuffer;

    private Camera.PreviewCallback mTakePicturePreviewCallback = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            int w = mPreviewSize.getWidth();
            int h = mPreviewSize.getHeight();
            byte[] d = data;
            if(mDegree != 0) {
                if (mRotateBuffer == null || mRotateBuffer.length < data.length) {
                    mRotateBuffer = new byte[data.length];
                }
                int tmp = w;
                switch (mDegree){
                    case 90:
                        GraphUtils.yuv420pRotate90(data, mRotateBuffer, w,h);
                        w = h;h = tmp;
                        break;
                    case 180:
                        GraphUtils.yuv420pRotate180(data, mRotateBuffer, w,h);
                        break;
                    case 270:
                        GraphUtils.yuv420pRotate270(data, mRotateBuffer, w,h);
                        w = h;h = tmp;
                        break;
                    default:
                        mRotateBuffer = data;
                        break;
                }
                d = mRotateBuffer;
            }

            YuvImage yuvImage  = new YuvImage(d, mPreviewFormat,w ,h, null);
            OutputStream outputStream = null;
            try {
                outputStream = new FileOutputStream(mFile);
                yuvImage.compressToJpeg( new Rect(0,0,w,h),100,outputStream);
            }catch (IOException e){
                e.printStackTrace();
            }finally {
                try{
                    outputStream.close();
                }catch (Exception e){

                }
            }
            complete();
        }
    };

    private Camera.ShutterCallback mShutterCallback = new Camera.ShutterCallback() {
        @Override
        public void onShutter() {
            Log.d(TAG, "mShutterCallback onShutter");
        }
    };
    private Camera.PictureCallback mRawImageCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.d(TAG, "mRawImageCallback onPictureTaken");
        }
    };

    private Camera.PictureCallback mPostviewCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.d(TAG, "mPostviewCallback onPictureTaken");
        }
    };

    private Camera.PictureCallback mJpegCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.d(TAG, "mJpegCallback onPictureTaken");
            OutputStream outputStream = null;
            try {
                outputStream = new FileOutputStream(mFile);
                outputStream.write(data);
                outputStream.flush();

                complete();
            }catch (IOException e){
                e.printStackTrace();
            }finally {
                try{
                    outputStream.close();
                }catch (Exception e){

                }
            }

        }
    };

    private Camera.OnZoomChangeListener mZoomChangeListener = new Camera.OnZoomChangeListener() {
        @Override
        public void onZoomChange(int zoomValue, boolean stopped, Camera camera) {
            Log.d(TAG, "mZoomChangeListener onZoomChange");
        }
    };

    public CameraBasic(Context context){
        mContext = context;
        mFile = new File(context.getExternalFilesDir(null), "pic1.jpg");
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
        initCamera();

        mSensorManager = (SensorManager) mContext.getSystemService(Context.
                SENSOR_SERVICE);
        mAccel = mSensorManager.getDefaultSensor(Sensor.
                TYPE_ACCELEROMETER);
        mSensorManager.registerListener(mSensorHandler, mAccel,
                SensorManager.SENSOR_DELAY_UI);
        try {
            if(mSurfaceHolder != null) {
                mCamera.setPreviewDisplay(mSurfaceHolder);
            }else{
                mCamera.setPreviewTexture(mSurfaceTexture);
            }
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
            release();
            return;
        }

        if(mOutCallback != null){
            mOutCallback.onConfigured();
        }
        if(mAutoTakePicture > 0){
            takePictureWhenFoced();
        }
    }

    public void onPause(){
        release();
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

    public void takePictureWhenFoced(){
        if(mCamera != null){
            mAutoFocusTake = true;
            mCamera.autoFocus(mAutoFocusCallback);
        }
    }

    /**
     * Initiate a still image capture.
     */
    public void takePicture(){
        if(mCamera == null){
            return;
        }
        if( mMuteShutterSound &&  !mCameraInfo.canDisableShutterSound) {
            mCamera.setOneShotPreviewCallback(mTakePicturePreviewCallback);
            //mCamera.setPreviewCallback(mTakePicturePreviewCallback);
        }else{
            mCamera.takePicture(mShutterCallback, mRawImageCallback, mPostviewCallback, mJpegCallback);
        }
    }

    private Camera.AutoFocusCallback mAutoFocusCallback = new Camera.AutoFocusCallback() {

        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            // TODO Auto-generated method stub
            if(mAutoFocusTake){
                takePicture();
                mAutoFocusTake = false;
            }
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
            mCameraInfo = new CameraInfo();
            for (int cameraId = 0; cameraId < numberOfCameras; ++cameraId) {
                Camera.getCameraInfo(cameraId, mCameraInfo);

                if (mFacing != mCameraInfo.facing) {
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

    private void release(){
        if(mCamera != null){
            try {
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        if(mSensorManager != null) {
            mSensorManager.unregisterListener(mSensorHandler);
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

    private int[] getNearSupport(List<int[]> supportFpss,int matchFps){
        int[] result = null;
        supportFpss.iterator();
        for(int i = supportFpss.size() - 1; i >= 0; i--){
            if(supportFpss.get(i)[1] >= matchFps){
                result = supportFpss.get(i);
            }
        }
        return result;
    }

    protected void initCamera() {

        if (mCamera != null) {
            Log.w(TAG, "initCamera");
            Camera.Parameters params = mCamera.getParameters();
            Camera.Size size = getMinSize(params, _width,_height);
            mPreviewSize = new Size(size.width,size.height);

            params.setPreviewSize(size.width, size.height);
            mPreviewFormat = params.getPreviewFormat();
            if(mOutCallback != null){
                mOutCallback.onPreviewSize(mPreviewSize);
            }
            if(mCameraInfo.canDisableShutterSound) {
                boolean muteSuc = mCamera.enableShutterSound(!mMuteShutterSound);
                Log.d(TAG, "mute shutter sound " + (muteSuc ? "success" : "failed"));
            }

            List<String> focuses = params.getSupportedFocusModes();
            if (focuses.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                mAutoFocusSupported = true;
            }
            mCamera.cancelAutoFocus();
            mCamera.setParameters(params);
            mZoomSupported = params.isZoomSupported();

            mSmoothZoomSupported = params.isSmoothZoomSupported();

            mMaxZoom = params.getMaxZoom();


            mCamera.setZoomChangeListener(mZoomChangeListener);
        }
    }

    /**
     * Configures the necessary {@link Matrix} transformation to `mTextureView`.
     * This method should be called after the camera preview size is determined in
     * setUpCameraOutputs and also the size of `mTextureView` is fixed.
     *
     * @param viewWidth  The width of `mTextureView`
     * @param viewHeight The height of `mTextureView`
     */
    public Matrix configureTransform(int viewWidth, int viewHeight) {
        if (null == mPreviewSize) {
            return null;
        }
        int rotation = ((Activity)mContext).getWindowManager().getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, mPreviewSize.getWidth(), mPreviewSize.getHeight());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_180 == rotation || Surface.ROTATION_0 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) viewHeight / mPreviewSize.getHeight(),
                    (float) viewWidth / mPreviewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate( 90 * (rotation + 1) , centerX, centerY);
        } else if (Surface.ROTATION_90 == rotation) {
            matrix.postRotate(0, centerX, centerY);
        }else if (Surface.ROTATION_270 == rotation){
            matrix.postRotate(180, centerX, centerY);
        }
        return matrix;
    }

    public void focusOnTouch(int x, int y) {
        Log.d(TAG, "手动对焦");
        if(mCamera == null) return;
        Rect focusRect = calculateTapArea(x, y, 1f); // 对焦区域
        Rect meteringRect = calculateTapArea(x, y, 1.5f); // 测光区域

        Camera.Parameters parameters = mCamera.getParameters();
        if (parameters.getMaxNumFocusAreas() > 0) {
            List<Camera.Area> focusAreas = new ArrayList<Camera.Area>();
            focusAreas.add(new Camera.Area(focusRect, 2));
            parameters.setFocusAreas(focusAreas);
        }

        if (parameters.getMaxNumMeteringAreas() > 0) {
            List<Camera.Area> meteringAreas = new ArrayList<Camera.Area>();
            meteringAreas.add(new Camera.Area(meteringRect, 1000));

            parameters.setMeteringAreas(meteringAreas);
        }


        try {
            mCamera.cancelAutoFocus();
            mCamera.setParameters(parameters);
            mCamera.autoFocus(mAutoFocusCallback);

        }catch (Exception e){
            Log.e(TAG,"手动对焦异常");
            e.printStackTrace();
        }
    }

    /**
     * Convert touch position x:y to {@link Camera.Area} position -1000:-1000 to 1000:1000.
     */
    private Rect calculateTapArea(float x, float y, float coefficient) {
        float focusAreaSize = 300;
        int areaSize = Float.valueOf(focusAreaSize * coefficient).intValue();

        int centerX = (int) (x / mPreviewSize.getWidth() * 2000 - 1000);
        int centerY = (int) (y / mPreviewSize.getHeight() * 2000 - 1000);

        int left = clamp(centerX - areaSize / 2, -1000, 1000);
        int right = clamp(left + areaSize, -1000, 1000);
        int top = clamp(centerY - areaSize / 2, -1000, 1000);
        int bottom = clamp(top + areaSize, -1000, 1000);

        return new Rect(left, top, right, bottom);
    }

    private int clamp(int x, int min, int max) {
        if (x > max) {
            return max;
        }
        if (x < min) {
            return min;
        }
        return x;
    }

    public Size getPreviewSize(){
        return mPreviewSize;
    }

    private void complete(){
        mAutoTakePicture--;
        if(mAutoTakePicture > 0){
            takePictureWhenFoced();
        }else {
            if (mOutCallback != null) {
                mOutCallback.onCaptureCompleted(mFile);
            }
            if(mAutoClose){
                onPause();
            }
        }
    }

    public void setAutoTakePicture(int count){
        mAutoTakePicture = count;
    }

    public void setAutoClose(boolean is){
        mAutoClose = is;
    }

    public void rotate(int degree, boolean display){
        if(display && mCamera != null){
            mCamera.setDisplayOrientation(degree);
        }
        mDegree = degree;
    }

    public void rotateByOrientation(int orientation, boolean display) {
        switch (orientation) {
            case Surface.ROTATION_0:
                mDegree = 90;
                break;
            case Surface.ROTATION_90:
                mDegree = 0;
                break;
            case Surface.ROTATION_180:
                mDegree = 270;
                break;
            case Surface.ROTATION_270:
                mDegree = 180;
                break;
            default:
                mDegree = 0;
                Log.e(TAG, "Display rotation is invalid: " + orientation);
        }
        if(display && mCamera != null){
            mCamera.setDisplayOrientation(mDegree);
        }
    }
}