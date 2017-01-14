package com.sun.account;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import com.sun.conversation.CvsActivity;
import com.sun.connect.SocketService;
import com.sun.personalconnect.Application;
import com.sun.personalconnect.R;
import com.sun.settings.Config;
import com.sun.utils.ToastUtils;

/**
 * Created by guoyao on 2016/12/13.
 */
public class AccountActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText mEditPassword;
    private CheckBox mCkbPassword;

    private CheckBox mCkbDebug;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        mEditPassword = (EditText) findViewById(R.id.edit_password);
        mCkbPassword = (CheckBox) findViewById(R.id.btn_password_show);

        (mCkbDebug = (CheckBox) findViewById(R.id.btn_debug_address)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Config.Debug = mCkbDebug.isChecked();
            }
        });
        mCkbDebug.setChecked(Config.Debug);

        if(Application.App.getAccount().isLogin()){
            loginJump();
        }
    }


    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id){
            case R.id.btn_submit:
                if(TextUtils.isEmpty(mEditPassword.getText().toString())){
                    ToastUtils.show("凭据不能为空", Toast.LENGTH_SHORT);
                }else{
                    boolean rlt = Application.App.getAccount().Login(mEditPassword.getText().toString());
                    if(rlt){
                        loginJump();
                    }else{
                        ToastUtils.show("凭据无效", Toast.LENGTH_SHORT);
                    }
                }
                break;
            case R.id.btn_password_show:
                mEditPassword.setInputType(mCkbPassword.isChecked() ?  InputType.TYPE_CLASS_TEXT |InputType.TYPE_TEXT_VARIATION_PASSWORD :InputType.TYPE_CLASS_TEXT |InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                break;
        }
    }

    private void loginJump(){
        startActivity(new Intent(AccountActivity.this, CvsActivity.class));
        finish();
        /*
        SocketTask.getInstance().Ready(new Runnable() {
            @Override
            public void run() {
                SocketTask.getInstance().sendMessage(SocketTask.MSG_REQUEST, RequestDataHelper.CvsConnect, new SocketCallback() {
                    @Override
                    public void onError(int eventId, Throwable e) {
                        ToastUtils.show("连接失败", Toast.LENGTH_SHORT);
                    }

                    @Override
                    public void onComplete(int eventId, ResponseData data) {
                        ToastUtils.show("连接成功", Toast.LENGTH_SHORT);
                        startActivity(new Intent(AccountActivity.this, CvsActivity.class));
                        finish();
                    }

                    @Override
                    public void onConnect(int eventId) {
                    }
                });
            }
        });
        */
    }
}
