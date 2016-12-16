package com.sun.conversation;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.sun.account.Account;
import com.sun.connect.ResponseData;
import com.sun.connect.SocketCallback;
import com.sun.personalconnect.Application;
import com.sun.personalconnect.R;
import com.sun.utils.ToastUtils;
import com.sun.utils.Utils;

/**
 * Created by guoyao on 2016/12/13.
 */
public class CvsActivity extends AppCompatActivity implements View.OnClickListener {
    private ResponseData mData;
    private EditText mEditContent;
    private RecyclerView mCvsRcc;
    SocketCallback callback= new SocketCallback() {
        @Override
        public void onError(int eventId, Throwable e) {

        }

        @Override
        public void onComplete(int eventId, ResponseData data) {
            if(data != null){
                mData = data;
                CvsActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ToastUtils.show(mData.getData(), Toast.LENGTH_SHORT);
                    }
                });
            }
        }

        @Override
        public void onConnect(int eventId) {

        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);
        mCvsRcc = (RecyclerView)findViewById(R.id.rcr_cvs_content);
        mCvsRcc.setHasFixedSize(true);
        mCvsRcc.setLayoutManager(new LinearLayoutManager(this));
        mCvsRcc.setAdapter(new CvsRecyclerAdapter(this));
        mEditContent = (EditText)findViewById(R.id.edit_cvs_content);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id){
            case R.id.btn_cvs_submit:
                submit();
                break;
            default:
                break;
        }
    }

    private void submit(){
        String content = mEditContent.getText().toString();
        if(TextUtils.isEmpty(content)){
            ToastUtils.show("输入为空？",Toast.LENGTH_SHORT);
            return;
        }
        CvsNote note = new CvsNote();
        note.setContent(content);
        Account account = Application.getInstance().getAccount();
        note.setUserName(account.getLoginName());
        note.setUserId(account.getLoginId());
        long time = System.currentTimeMillis();
        note.setTimeStamp(time);
        note.setTimeFormat(Utils.generatePlayTime(time));
        Application.getInstance().getCvsHistoryManager().insertCache(note);
        mCvsRcc.getAdapter().notifyDataSetChanged();
    }
}
