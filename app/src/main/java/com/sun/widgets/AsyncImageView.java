package com.sun.widgets;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.sun.personalconnect.R;

public class AsyncImageView extends ImageView {

	static final String TAG = "AsyncImageView";
	static Drawable defaultDrawable;

	protected static DisplayImageOptions DEFAULT_DISPLAY_OPTIONS;

	protected ImageLoader mImageLoader = ImageLoader.getInstance();

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
					.bitmapConfig(Bitmap.Config.RGB_565).build();
		}
	}

	public void setImageAsync(String imageUri) {
		String oldurl = (String)getTag();
		if(oldurl == imageUri || (oldurl != null && oldurl.equals(imageUri))){
			return ;
		}
		setImageAsync(imageUri, DEFAULT_DISPLAY_OPTIONS, null);
		setTag(imageUri);
	}

	public void setImageAsync(String imageUri, DisplayImageOptions options) {
		setImageAsync(imageUri, options, null);
	}

	public void setImageAsync(String imageUri, ImageLoadingListener listener) {
		setImageAsync(imageUri, DEFAULT_DISPLAY_OPTIONS, listener);
	}

	public void setImageAsync(String imageUri, DisplayImageOptions options,
			ImageLoadingListener listener) {

		mImageLoader.displayImage(imageUri, this, options, listener);

		if(defaultDrawable instanceof AnimationDrawable) {
			post(new Runnable() {
				@Override
				public void run() {
					((AnimationDrawable) defaultDrawable).start();
				}
			});
		}
	}
}
