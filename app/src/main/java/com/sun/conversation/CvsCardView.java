package com.sun.conversation;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.TextView;

import com.sun.personalconnect.R;

/**
 * Created by guoyao on 2016/12/16.
 */
public class CvsCardView extends CardView {
    private TextView mTxtName;
    private TextView mTxtTime;
    private TextView mTxtContent;
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
    }

    public void update(CvsNote note){
        mTxtName.setText(note.getUserName());
        mTxtContent.setText(note.getContent());
        mTxtTime.setText(note.getTimeFormat());
    }
}
