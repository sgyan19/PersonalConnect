package com.sun.conversation;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.sun.personalconnect.Application;
import com.sun.personalconnect.R;
import com.sun.utils.UriUtils;
import com.sun.widgets.AsyncImageView;

import java.io.File;

import de.greenrobot.event.EventBus;

/**
 * Created by guoyao on 2016/12/16.
 */
public class CvsCardView extends CardView {
    private static final String TAG = "CvsCardView";

    private TextView mTxtName;
    private TextView mTxtTime;
    private TextView mTxtContent;
    private TextView mTxtSend;
    private ImageView mImgFail;
    private ImageView mImgSending;
    private AsyncImageView mImgContent;
    private CvsNote mNote;
    private int mType;
    private int mPosition;
    private static Animation mRotateAnimation;
    private boolean mInitAnimation = false;

    private OnClickListener mListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            int id = view.getId();
            switch (id){
                case R.id.img_send_fail:
                    EventBus.getDefault().post(new EventNote(mNote, EventNote.ACTION_NEED_SEEND));
                    mNote.setSendStatus(CvsNote.STATUS_SENDING);
                    update(mPosition, mNote);
                    break;
            }
        }
    };

    protected ImageLoadingListener mImageLoadingListener = new ImageLoadingListener() {
        @Override
        public void onLoadingStarted(String s, View view) {
        }

        @Override
        public void onLoadingFailed(String s, View view, FailReason failReason) {
            if(!TextUtils.isEmpty(s)){
                String path = UriUtils.getPath(getContext(), Uri.parse(s));
                if(!TextUtils.isEmpty(path) && !(new File(path)).exists()){
                    EventBus.getDefault().post(new EventNote(mNote, EventNote.ACTION_DOWNLOAD_IMAGE));
                }
            }
        }

        @Override
        public void onLoadingComplete(String s, View view, Bitmap bitmap) {
            EventBus.getDefault().post(new EventImageLoaderComplete(mPosition));
        }

        @Override
        public void onLoadingCancelled(String s, View view) {
        }
    };

    public CvsCardView(Context context) {
        super(context);
    }

    public CvsCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void init(int type){
        Context context = getContext();
        mType = type;
        if(type == CvsNote.TYPE_TEXT) {
            LayoutInflater.from(context).inflate(R.layout.view_conversation_txt, this, true);
        }else if(type == CvsNote.TYPE_IMAGE){
            LayoutInflater.from(context).inflate(R.layout.view_conversation_img, this, true);
        }
        mTxtName = (TextView)findViewById(R.id.txt_cvs_name);
        mTxtTime = (TextView)findViewById(R.id.txt_cvs_time);
        mTxtContent = (TextView)findViewById(R.id.txt_cvs_content);
        mTxtSend = (TextView) findViewById(R.id.txt_cvs_is_send);
        mImgContent = (AsyncImageView) findViewById(R.id.img_cvs_content);
        mImgFail = (ImageView) findViewById(R.id.img_send_fail);
        mImgSending = (ImageView)findViewById(R.id.img_sending);
        if(!mInitAnimation) {
            if(mRotateAnimation == null){
                mRotateAnimation = new RotateAnimation(0.0f,360f,Animation.RELATIVE_TO_SELF,
                        0.5f,Animation.RELATIVE_TO_SELF,0.5f);
                mRotateAnimation.setRepeatCount(-1);
                mRotateAnimation.setInterpolator(new LinearInterpolator());
                mRotateAnimation.setDuration(1000);//设置动画持续时间
            }
            mInitAnimation = true;
        }
        mImgFail.setOnClickListener(mListener);
    }

    public void update(int position, CvsNote note){
        mPosition = position;
        int color = CvsNoteHelper.getUserColor(note);
        mTxtName.setText(note.getUserName());
        mTxtName.setTextColor(color);
        mTxtTime.setText(note.getTimeFormat());
        mTxtTime.setTextColor(color);
        mTxtSend.setText(CvsNoteHelper.getStatusText(note));
        if(note.getSendStatus() == CvsNote.STATUS_FAL){
            mImgFail.setVisibility(VISIBLE);
            mImgSending.setVisibility(GONE);
        }else if(note.getSendStatus() == CvsNote.STATUS_SENDING){
            mImgFail.setVisibility(GONE);
            mImgSending.setVisibility(VISIBLE);
            mImgSending.startAnimation(mRotateAnimation);
        }else{
            mImgFail.setVisibility(GONE);
            mImgSending.setVisibility(GONE);
        }
        Log.d(TAG, String.format("view type:%d, note type:%d", mType, note.getType()));
        if(note.getType() == CvsNote.TYPE_TEXT) {
            mTxtContent.setText(note.getContent());
        }else if(note.getType() == CvsNote.TYPE_IMAGE){
            mImgContent.setImageAsync(
                    Uri.fromFile(new File(Application.App.getSocketRawFolder(), note.getContent())).toString(),
                    mImageLoadingListener );
        }
        mNote = note;
    }
}
