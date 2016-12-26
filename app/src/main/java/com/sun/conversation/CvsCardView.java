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
    public static final String TIP_HAS_SEND = "";//"(已发送)";
    public static final String TIP_NO_SEND = "(正在发送)";
    private TextView mTxtName;
    private TextView mTxtTime;
    private TextView mTxtContent;
    private TextView mTxtSend;
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
    }

    public void update(CvsNote note){
        int color = CvsNoteHelper.getUserColor(note);
        mTxtName.setText(note.getUserName());
        mTxtName.setTextColor(color);
        mTxtTime.setText(note.getTimeFormat());
        mTxtTime.setTextColor(color);
        mTxtContent.setText(note.getContent());
        mTxtSend.setText(CvsNoteHelper.getStatusText(note));
    }
}
