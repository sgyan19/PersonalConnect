package com.sun.conversation;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.TextView;

import com.sun.personalconnect.R;
import com.sun.widgets.AsyncImageView;

/**
 * Created by guoyao on 2016/12/16.
 */
public class CvsCardView extends CardView {
    public static final String TIP_HAS_SEND = "";//"(已发送)";
    public static final String TIP_NO_SEND = "(正在发送)";
    private TextView mTxtName;
    private TextView mTxtTime;
    private TextView mTxtContent;
    private TextView mTxtSend;
    private AsyncImageView mImgContent;
    public CvsCardView(Context context) {
        super(context);
        init(context);
    }

    public CvsCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context){
        LayoutInflater.from(context).inflate(R.layout.view_conversation_item, this, true);
        mTxtName = (TextView)findViewById(R.id.txt_cvs_name);
        mTxtTime = (TextView)findViewById(R.id.txt_cvs_time);
        mTxtContent = (TextView)findViewById(R.id.txt_cvs_content);
        mTxtSend = (TextView) findViewById(R.id.txt_cvs_is_send);
        mImgContent = (AsyncImageView) findViewById(R.id.img_cvs_content);
    }

    public void update(CvsNote note){
        int color = CvsNoteHelper.getUserColor(note);
        mTxtName.setText(note.getUserName());
        mTxtName.setTextColor(color);
        mTxtTime.setText(note.getTimeFormat());
        mTxtTime.setTextColor(color);
        mTxtSend.setText(CvsNoteHelper.getStatusText(note));

        if(note.getType() == CvsNote.TYPE_TEXT) {
            mTxtContent.setVisibility(VISIBLE);
            mImgContent.setVisibility(GONE);
            mTxtContent.setText(note.getContent());
        }else if(note.getType() == CvsNote.TYPE_IMAGE){
            mTxtContent.setVisibility(GONE);
            mImgContent.setVisibility(VISIBLE);
            mImgContent.setImageAsync(note.getContent());
        }
    }
}
