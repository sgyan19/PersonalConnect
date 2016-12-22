package com.sun.conversation;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
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
import com.sun.connect.SocketService;
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
    private EditText mEditContent;
    private RecyclerView mCvsRcc;
    private SocketService.ServiceBinder serviceBinder;
    private LinkedHashMap<Integer, CvsNote> mRequestHistory = new LinkedHashMap<>();

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int key = intent.getIntExtra(SocketService.KEY_INT_REQUESTKEY, SocketTask.REQUEST_KEY_NOBODY);
            if(key == SocketTask.REQUEST_KEY_NOBODY) {
                return;
            }
            String error = intent.getStringExtra(SocketService.KEY_STRING_ERROR);
            String response = intent.getStringExtra(SocketService.KEY_STRING_RESPONSE);
            RequestData noteRequest = null;
            CvsNote note = null;
            if(TextUtils.isEmpty(error) && !TextUtils.isEmpty(response)){
                try {
                    ResponseData responseObj = GsonUtils.mGson.fromJson(response, ResponseData.class);
                    noteRequest = GsonUtils.mGson.fromJson(responseObj.getData(), RequestData.class);
                    note = GsonUtils.mGson.fromJson(noteRequest.getArgs().get(0), CvsNote.class);
                }catch (Exception e){}
            }
            if(!TextUtils.isEmpty(error) || noteRequest == null || note == null){
                if(mRequestHistory.containsKey(key)){
                    mRequestHistory.get(key).setSendStatus(CvsNote.STATUS_FAL);
                    mRequestHistory.remove(key);
                    mCvsRcc.getAdapter().notifyDataSetChanged();
                    return;
                }
                return;
            }
            key = noteRequest.getRequestId();

            note.setSendStatus(CvsNote.STATUS_SUC);
            if(key == SocketTask.REQUEST_KEY_ANLYBODY){
                Application.getInstance().getCvsHistoryManager().insertCache(note);
                Application.getInstance().getCvsHistoryManager().saveCache();
                mCvsRcc.getAdapter().notifyDataSetChanged();
                mCvsRcc.scrollToPosition(mCvsRcc.getAdapter().getItemCount() - 1);
            }else if(mRequestHistory.containsKey(key)){
                mRequestHistory.get(key).setSendStatus(CvsNote.STATUS_SUC);
                mRequestHistory.remove(key);
                Application.getInstance().getCvsHistoryManager().saveCache();
                mCvsRcc.getAdapter().notifyDataSetChanged();
                ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(mEditContent.getApplicationWindowToken(), 0);
            }
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
        IntentFilter intentFilter = new IntentFilter(SocketService.SocketReceiveBroadcast);
        registerReceiver(receiver, intentFilter);
        bindService(new Intent(CvsActivity.this, SocketService.class), new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                serviceBinder = (SocketService.ServiceBinder) iBinder;
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                serviceBinder = null;
            }
        }, BIND_AUTO_CREATE);
        asyncScrollEnd();
        //SocketTask.getInstance().sendMessage(SocketTask.MSG_RECEIVE, null, callback);
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
        String arg = GsonUtils.mGson.toJson(note);
        requestData.addArg(arg);
        requestData.setRequestId(requestData.hashCode());
        serviceBinder.request(requestData.getRequestId(), GsonUtils.mGson.toJson(requestData));
        mRequestHistory.put(requestData.getRequestId(), note);
        mEditContent.setText("");
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
