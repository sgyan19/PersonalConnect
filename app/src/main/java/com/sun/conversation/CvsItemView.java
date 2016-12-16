package com.sun.conversation;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sun.personalconnect.R;

/**
 * Created by guoyao on 2016/12/15.
 */
public class CvsItemView extends RelativeLayout{
    private TextView mTxtName;
    private TextView mTxtTime;
    private TextView mTxtContent;

    public CvsItemView(Context context) {
        super(context);
        init(context);
    }

    public CvsItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CvsItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context){
        LayoutInflater.from(context).inflate(R.layout.view_conversation_item, this, true);
        mTxtName = (TextView)findViewById(R.id.txt_cvs_name);
        mTxtTime = (TextView)findViewById(R.id.txt_cvs_time);
        mTxtContent = (TextView)findViewById(R.id.txt_cvs_content);
    }
}
