package com.sun.camera;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.res.Configuration;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraCaptureSession;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.sun.personalconnect.R;

import java.io.File;

/**
 * Created by guoyao on 2017/5/24.
 */

public class TestCameraFragment extends Fragment
        implements View.OnClickListener, ActivityCompat.OnRequestPermissionsResultCallback {

    private Camera2Basic mCamera;
    private AutoFitTextureView mTextureView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_camera2_basic, container, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        view.findViewById(R.id.picture).setOnClickListener(this);
        view.findViewById(R.id.info).setOnClickListener(this);
        mTextureView = (AutoFitTextureView) view.findViewById(R.id.texture);
        mCamera = new Camera2Basic(getActivity());
        mCamera.setCaptureCallback(new Camera2Basic.Callback() {
            @Override
            public void onPreviewSize(Size size) {
                int orientation = getActivity().getResources().getConfiguration().orientation;
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    mTextureView.setAspectRatio(
                            size.getWidth(), size.getHeight());
                } else {
                    mTextureView.setAspectRatio(
                            size.getHeight(), size.getWidth());
                }
            }

            @Override
            public void onConfigured(CameraCaptureSession session) {

            }

            @Override
            public void onCaptureCompleted(File file) {
                notifyInfo("Saved: " + file);
            }
        });
//        mCamera.setDisplay(mTextureView);
        mTextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                mCamera.setDisplay(surface,width ,height);
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
                Matrix matrix = mCamera.configureTransform(width,height);
                if(matrix != null) {
                    mTextureView.setTransform(matrix);
                }
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                return true;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {

            }
        });
    }



    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.picture: {
                mCamera.takePicture();
                break;
            }
            case R.id.info: {
                Activity activity = getActivity();
                if (null != activity) {
                    new AlertDialog.Builder(activity)
                            .setMessage("<![CDATA[\n" +
                                    "        \n" +
                                    "            \n" +
                                    "            This sample demonstrates the basic use of Camera2 API. Check the source code to see how\n" +
                                    "            you can display camera preview and take pictures.\n" +
                                    "            \n" +
                                    "        \n" +
                                    "        ]]>")
                            .setPositiveButton(android.R.string.ok, null)
                            .show();
                }
                break;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mCamera != null) {
            mCamera.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mCamera != null) {
            mCamera.onPause();
        }
    }

    /**
     * Shows a {@link Toast} on the UI thread.
     *
     * @param text The message to show
     */
    private void showToast(final String text) {
        final Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(activity, text, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    /**
     * Shows a {@link Toast} on the UI thread.
     *
     * @param text The message to show
     */
    private void notifyInfo(final String text) {
        // TODO: 2017/5/19 info handle
        final Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(activity, text, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
