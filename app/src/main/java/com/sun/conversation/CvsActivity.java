package com.sun.conversation;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;
import com.sun.account.Account;
import com.sun.connect.RequestData;
import com.sun.connect.RequestDataHelper;
import com.sun.connect.ResponseData;
import com.sun.connect.SocketCallback;
import com.sun.connect.SocketTask;
import com.sun.personalconnect.Application;
import com.sun.personalconnect.R;
import com.sun.utils.GsonUtils;
import com.sun.utils.ToastUtils;
import com.sun.utils.Utils;
import java.util.LinkedHashMap;

/**
 * Created by guoyao on 2016/12/13.
 */
public class CvsActivity extends AppCompatActivity implements View.OnClickListener {
    private ResponseData mData;
    private EditText mEditContent;
    private RecyclerView mCvsRcc;
    private String mLastRequestArg;
    private LinkedHashMap<String, CvsNote> mRequestHistory = new LinkedHashMap<>();
    SocketCallback callback = new SocketCallback() {
        @Override
        public void onError(int eventId, Throwable e) {
        }

        @Override
        public void onComplete(int eventId, ResponseData data) {
            if(data != null){
                mData = data;
//                CvsActivity.this.runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        ToastUtils.show(mData.getData(), Toast.LENGTH_SHORT);
//                    }
//                });

                if(mRequestHistory.containsKey(data.getRequestId())){
                    ToastUtils.show("发送成功", Toast.LENGTH_SHORT);
                    CvsNote note = mRequestHistory.get(data.getRequestId());
                    note.setIsSend(true);
                    mRequestHistory.remove(data.getRequestId());
                }else{
                    if(data.getData ()!= null){
                        try{
                            RequestData request = GsonUtils.mGson.fromJson(data.getData(), RequestData.class);
                            CvsNote note = GsonUtils.mGson.fromJson(request.getArgs().get(0), CvsNote.class);
                            note.setIsSend(true);
                            Application.getInstance().getCvsHistoryManager().insertCache(note);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }

                mCvsRcc.post(new Runnable() {
                    @Override
                    public void run() {
                        mCvsRcc.getAdapter().notifyDataSetChanged();
                        mCvsRcc.scrollToPosition(mCvsRcc.getAdapter().getItemCount() - 1);
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
        //mCvsRcc.setHasFixedSize(true);
        mCvsRcc.setLayoutManager(new LinearLayoutManager(this));
        mCvsRcc.setAdapter(new CvsRecyclerAdapter(this));
        mEditContent = (EditText)findViewById(R.id.edit_cvs_content);
        SocketTask.getInstance().sendMessage(SocketTask.MSG_RECEIVE, null, callback);
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
        note.setId((int) System.currentTimeMillis());
        note.setUserName(account.getLoginName());
        note.setUserId(account.getLoginId());
        long time = System.currentTimeMillis();
        note.setTimeStamp(time);
        note.setTimeFormat(Utils.getFormatTime(time));
        Application.getInstance().getCvsHistoryManager().insertCache(note);
        mCvsRcc.getAdapter().notifyDataSetChanged();
        asyncScrollEnd();
        RequestData requestData = new RequestData();
        requestData.setCode(RequestDataHelper.CODE_ConversationNote);
        mLastRequestArg = GsonUtils.mGson.toJson(note);
        requestData.addArg(mLastRequestArg);
        requestData.setRequestId(String.valueOf(SocketTask.getInstance().sendMessage(SocketTask.MSG_REQUEST, requestData, null)));
        mRequestHistory.put(requestData.getRequestId(), note);

        mEditContent.setText("");
        ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow( mEditContent.getApplicationWindowToken( ) , 0);
    }

    private void asyncScrollEnd() {
        mCvsRcc.post(new Runnable() {
            @Override
            public void run() {
                mCvsRcc.scrollToPosition(mCvsRcc.getAdapter().getItemCount() - 1);
            }
        });
    }
}
