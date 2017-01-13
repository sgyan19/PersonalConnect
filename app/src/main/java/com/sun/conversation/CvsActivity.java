package com.sun.conversation;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.PointF;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnLayoutChangeListener;
import android.widget.EditText;
import android.widget.Toast;
import com.sun.account.Account;
import com.sun.account.AccountActivity;
import com.sun.conversation.CvsService.CvsListener;
import com.sun.personalconnect.Application;
import com.sun.personalconnect.R;
import com.sun.power.InputFormat;
import com.sun.power.LocalCmd;
import com.sun.utils.RequestCode;
import com.sun.utils.ToastUtils;
import com.sun.utils.UriUtils;

import java.io.File;
import java.util.List;

/**
 * Created by guoyao on 2016/12/13.
 */
public class CvsActivity extends AppCompatActivity implements View.OnClickListener,CvsListener,OnLayoutChangeListener {

    //region 私有成员
    private EditText mEditContent;
    private RecyclerView mCvsRcc;
    private CvsService.ServiceBinder serviceBinder;
    private ServiceConnection serviceConn;
    private int mKeyBoardHeight;
    private String mLastSumbit;
    //endregion

    //region 生命周期
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Account account = Application.App.getAccount();
        if(!account.isLogin()){
            startActivity(new Intent(this, AccountActivity.class));
            finish();
            return ;
        }

        setContentView(R.layout.activity_conversation);
        mCvsRcc = (RecyclerView)findViewById(R.id.rcr_cvs_content);
        //mCvsRcc.setHasFixedSize(true);
        mCvsRcc.setLayoutManager(new ScrollSpeedLinearLayoutManger(this).setSpeedSlow());
        mCvsRcc.setAdapter(new CvsRecyclerAdapter(this));
        mCvsRcc.addOnLayoutChangeListener(this);
        mCvsRcc.post(new Runnable() {
            @Override
            public void run() {
                mKeyBoardHeight = mCvsRcc.getMeasuredHeight() / 3;
            }
        });
        mEditContent = (EditText)findViewById(R.id.edit_cvs_content);
        serviceConn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                serviceBinder = (CvsService.ServiceBinder) iBinder;
                serviceBinder.setCvsListener(CvsActivity.this);
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                serviceBinder = null;
            }
        };
        bindService(new Intent(CvsActivity.this, CvsService.class), serviceConn, BIND_AUTO_CREATE);
        if(((CvsRecyclerAdapter)mCvsRcc.getAdapter()).removeTooMoreCache() > 0){
            mCvsRcc.getAdapter().notifyDataSetChanged();
        }
        scrollEnd();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(serviceBinder != null){
            serviceBinder.clearMyListener(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(serviceBinder != null){
            serviceBinder.setCvsListener(this);
        }
    }

    @Override
    protected void onDestroy() {
        if(serviceConn != null) {
            unbindService(serviceConn);
        }
        super.onDestroy();
    }
    //endregion

    //region activity回调
    @Override
    public void onLayoutChange(View view, int left, int top, int right,
                               int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
        //现在认为只要控件将Activity向上推的高度超过了1/3屏幕高，就认为软键盘弹起
        if(oldBottom != 0 && bottom != 0 &&(oldBottom - bottom > mKeyBoardHeight)){
            scrollEnd();
        }else if(oldBottom != 0 && bottom != 0 &&(bottom - oldBottom > mKeyBoardHeight)){
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case RequestCode.CHOSE_IMAGE:
                if(data != null) {
                    submitImage(data.getData());
                }
                break;
            default:
                break;
        }
    }

    //endregion

    //region 继承接口实现
    @Override
    public void onSendFailed(long key, CvsNote note, String message) {
        mCvsRcc.getAdapter().notifyDataSetChanged();
    }

    @Override
    public void onSendSuccess(CvsNote note) {
        mCvsRcc.getAdapter().notifyDataSetChanged();
    }

    @Override
    public void onNew(CvsNote note) {
        ((CvsRecyclerAdapter)mCvsRcc.getAdapter()).removeTooMoreCache();
        mCvsRcc.getAdapter().notifyDataSetChanged();
        animationScrollEnd();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id){
            case R.id.btn_cvs_text:
                submit();
                break;
            case R.id.btn_cvs_last:
                if(!TextUtils.isEmpty(mLastSumbit)){
                    mEditContent.setText(mLastSumbit);
                }
                break;
            case R.id.btn_cvs_img:
                choseImage();
            default:
                break;
        }
    }
    //endregion

    //region 私有内部类。ScrollSpeedLinearLayoutManger，RecyclerView滑动速度
    private static class ScrollSpeedLinearLayoutManger extends LinearLayoutManager{
        private float MILLISECONDS_PER_INCH = 0.03f;
        private Context context;

        public ScrollSpeedLinearLayoutManger(Context context) {
            super(context);
            this.context = context;
        }

        @Override
        public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
            LinearSmoothScroller linearSmoothScroller =
                    new LinearSmoothScroller(recyclerView.getContext()) {
                        @Override
                        public PointF computeScrollVectorForPosition(int targetPosition) {
                            return ScrollSpeedLinearLayoutManger.this
                                    .computeScrollVectorForPosition(targetPosition);
                        }

                        //This returns the milliseconds it takes to
                        //scroll one pixel.
                        @Override
                        protected float calculateSpeedPerPixel
                        (DisplayMetrics displayMetrics) {
                            return MILLISECONDS_PER_INCH / displayMetrics.density;
                            //返回滑动一个pixel需要多少毫秒
                        }
                    };
            linearSmoothScroller.setTargetPosition(position);
            startSmoothScroll(linearSmoothScroller);
        }


        public ScrollSpeedLinearLayoutManger setSpeedSlow() {
            //自己在这里用density去乘，希望不同分辨率设备上滑动速度相同
            //0.3f是自己估摸的一个值，可以根据不同需求自己修改
            MILLISECONDS_PER_INCH = context.getResources().getDisplayMetrics().density * 0.3f;
            return this;
        }

        public ScrollSpeedLinearLayoutManger setSpeedFast() {
            MILLISECONDS_PER_INCH = context.getResources().getDisplayMetrics().density * 0.03f;
            return this;
        }
    }
    //endregion

    //region 私有方法
    private void submit(){
        String content = mEditContent.getText().toString();
        if(TextUtils.isEmpty(content)){
            ToastUtils.show("输入为空？",Toast.LENGTH_SHORT);
            return;
        }
        mLastSumbit = content;
        List<String> cmds = InputFormat.format(content);
        if(!LocalCmd.handleCmd(cmds)){
            CvsNote note = serviceBinder.request(content, cmds);
            if(note != null){
                Application.App.getCvsHistoryManager().insertCache(note);
                mCvsRcc.getAdapter().notifyDataSetChanged();
                animationScrollEnd();
                Application.App.getCvsHistoryManager().keepLastSendNote(note);
            }
        }

        mEditContent.setText("");
    }

    private void choseImage(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "选择图片"), RequestCode.CHOSE_IMAGE);
    }

    private void submitImage(Uri uri){
        if ( null == uri ) return;
        String path = UriUtils.getPath(this, uri);
        File file = new File(path);
        if(!file.exists()) return;
        CvsNote note = serviceBinder.request(file);
        if(note != null){
            note.setContent(uri.toString());
            Application.App.getCvsHistoryManager().insertCache(note);
            mCvsRcc.getAdapter().notifyDataSetChanged();
            animationScrollEnd(400);
            Application.App.getCvsHistoryManager().keepLastSendNote(note);
        }
    }

    private void animationScrollEnd(long delayMillis) {
        mCvsRcc.postDelayed(new Runnable() {
            @Override
            public void run() {
                mCvsRcc.smoothScrollToPosition(mCvsRcc.getAdapter().getItemCount() - 1);
            }
        }, delayMillis);
    }

    private void animationScrollEnd() {
        mCvsRcc.post(new Runnable() {
            @Override
            public void run() {
                mCvsRcc.smoothScrollToPosition(mCvsRcc.getAdapter().getItemCount() - 1);
            }
        });
    }
    private void scrollEnd(){
        mCvsRcc.post(new Runnable() {
            @Override
            public void run() {
                mCvsRcc.scrollToPosition(mCvsRcc.getAdapter().getItemCount() - 1);
            }
        });
    }
    //endregion
}
