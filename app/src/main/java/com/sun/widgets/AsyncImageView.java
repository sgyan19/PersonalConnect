package com.sun.widgets;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.sun.personalconnect.R;

public class AsyncImageView extends ImageView {

	static final String TAG = "AsyncImageView";
	static Drawable defaultDrawable;

	protected static DisplayImageOptions DEFAULT_DISPLAY_OPTIONS;

	protected ImageLoader mImageLoader = ImageLoader.getInstance();
	protected ImageLoadingListener mExternalListener;
	protected ImageLoadingListener mDefaultListener = new ImageLoadingListener() {
		@Override
		public void onLoadingStarted(String s, View view) {
			if(mExternalListener != null){
				mExternalListener.onLoadingStarted(s, view);
			}
		}

		@Override
		public void onLoadingFailed(String s, View view, FailReason failReason) {
			if(mExternalListener != null){
				mExternalListener.onLoadingFailed(s, view, failReason);
			}
			if(defaultDrawable instanceof AnimationDrawable) {
				((AnimationDrawable) defaultDrawable).start();
			}
			setTag(null);
		}

		@Override
		public void onLoadingComplete(String s, View view, Bitmap bitmap) {
			if(mExternalListener != null){
				mExternalListener.onLoadingComplete(s, view, bitmap);
			}
		}

		@Override
		public void onLoadingCancelled(String s, View view) {
			if(mExternalListener != null){
				mExternalListener.onLoadingCancelled(s, view);
			}
		}
	};

	public AsyncImageView(Context context) {
		super(context);
		init();
	}

	public AsyncImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public AsyncImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init(){
		if( DEFAULT_DISPLAY_OPTIONS == null){
			if(defaultDrawable == null){
				if(Build.VERSION.SDK_INT >= 21){
					defaultDrawable = getResources().getDrawable(R.drawable.progress, getResources().newTheme());
				}else{
					defaultDrawable = getResources().getDrawable(R.drawable.progress);
				}
			}
			DEFAULT_DISPLAY_OPTIONS = new DisplayImageOptions.Builder()
					.showImageForEmptyUri(defaultDrawable)
					.showImageOnFail(defaultDrawable)
					.showImageOnLoading(defaultDrawable).cacheOnDisk(true)
					.imageScaleType(ImageScaleType.LOW_QUALITY)
					.considerExifParams(true)
					.bitmapConfig(Bitmap.Config.RGB_565).build();
		}
	}

	public void setImageAsync(String imageUri) {
		setImageAsync(imageUri, DEFAULT_DISPLAY_OPTIONS, null);
	}

	public void setImageAsync(String imageUri, DisplayImageOptions options) {
		setImageAsync(imageUri, options, null);
	}

	public void setImageAsync(String imageUri, ImageLoadingListener listener) {
		setImageAsync(imageUri, DEFAULT_DISPLAY_OPTIONS, listener);
	}

	public void setImageAsync(String imageUri, DisplayImageOptions options,
			ImageLoadingListener listener) {
		Log.d(TAG,"setImageAsync:" + imageUri);
		mExternalListener = listener;
		String oldUrl = (String)getTag();
		if(oldUrl == imageUri || (oldUrl != null && oldUrl.equals(imageUri))){
			Log.d(TAG, "setImageAsync use old");
			return ;
		}
		Log.d(TAG, "setImageAsync new");
		mImageLoader.displayImage(imageUri, this, options,  mDefaultListener);
		setTag(imageUri);
//		if(defaultDrawable instanceof AnimationDrawable) {
//			post(new Runnable() {
//				@Override
//				public void run() {
//					((AnimationDrawable) defaultDrawable).start();
//				}
//			});
//		}
	}
}
