package com.sun.conversation;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.sun.account.Account;
import com.sun.account.AccountActivity;
import com.sun.level.LocalCmd;
import com.sun.personalconnect.Application;
import com.sun.personalconnect.R;
import com.sun.utils.FileUtils;
import com.sun.utils.FormatUtils;
import com.sun.utils.RequestCode;
import com.sun.utils.ToastUtils;
import com.sun.utils.UriUtils;
import com.sun.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by guoyao on 2017/4/14.
 */
public class CvsFragment extends Fragment implements View.OnClickListener,CvsService.CvsListener,View.OnLayoutChangeListener {
    //region 常量
    public final static String TAG = "CvsActivity";
    public final static int REQUEST_CODE_WRITE_STORAGE = 99;
    //endregion

    //region 私有成员
    private EditText mEditContent;
    private RecyclerView mCvsRcc;
    private CvsService.ServiceBinder mCvsServiceBinder;
    private ServiceConnection mCvsServiceConn;
    private int mKeyBoardHeight;
    private String mLastSubmit;
    private HashMap<String,WeakReference<CvsNote>> mWeakImageNoteMap = new HashMap<>();
    private CvsImageDetailDialog mImgDetailDialog;
    //endregion

    //region 生命周期
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //region 先决条件
        Account account = Application.App.getAccount();
        if(!account.isLogin()){
            startActivity(new Intent(getActivity(), AccountActivity.class));
            getActivity().finish();
        }

        //region 绑定服务，注册eventbus
        mCvsServiceConn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                mCvsServiceBinder = (CvsService.ServiceBinder) iBinder;
                mCvsServiceBinder.setCvsListener(CvsFragment.this);
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                mCvsServiceBinder = null;
            }
        };
        getContext().bindService(new Intent(getContext(), CvsService.class), mCvsServiceConn, Context.BIND_AUTO_CREATE);
        EventBus.getDefault().register(this);
        //endregion
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_conversation, container, false);

//        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //region ui控制
        view.findViewById(R.id.btn_cvs_text).setOnClickListener(this);
        view.findViewById(R.id.btn_cvs_last).setOnClickListener(this);
        view.findViewById(R.id.btn_cvs_img).setOnClickListener(this);

        mCvsRcc = (RecyclerView)view.findViewById(R.id.rcr_cvs_content);
        mCvsRcc.setItemViewCacheSize(8);
        //mCvsRcc.setHasFixedSize(true);
        mCvsRcc.setLayoutManager(new ScrollSpeedLinearLayoutManger(getActivity()).setSpeedSlow());
        mCvsRcc.setAdapter(new CvsRecyclerAdapter(getActivity()));
        mCvsRcc.addOnLayoutChangeListener(this);
        mCvsRcc.post(new Runnable() {
            @Override
            public void run() {
                mKeyBoardHeight = mCvsRcc.getMeasuredHeight() / 3;
            }
        });
        mEditContent = (EditText)view.findViewById(R.id.edit_cvs_content);
        if(((CvsRecyclerAdapter)mCvsRcc.getAdapter()).removeTooMoreCache() > 0){
            mCvsRcc.getAdapter().notifyDataSetChanged();
        }
        scrollEnd();
        //endregion
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mCvsServiceBinder != null){
            mCvsServiceBinder.clearListener(this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mCvsServiceBinder != null){
            mCvsServiceBinder.setCvsListener(this);
        }
    }

    @Override
    public void onDestroy() {
        if(mCvsServiceConn != null) {
            getActivity().unbindService(mCvsServiceConn);
        }
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }
    //endregion

    //region activity复写
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
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
    public void onSendFailed(String key, CvsNote note, String message) {
        ToastUtils.show(message, Toast.LENGTH_SHORT);
        ((CvsRecyclerAdapter) mCvsRcc.getAdapter()).notifyItemChanged(note);
    }

    @Override
    public void onSendSuccess(CvsNote note) {
        ((CvsRecyclerAdapter)mCvsRcc.getAdapter()).notifyItemChanged(note);
        animationScrollEnd();
    }

    @Override
    public void onNewCvsNote(CvsNote note) {
        Log.d(TAG, "onNewCvsNote");
        ((CvsRecyclerAdapter)mCvsRcc.getAdapter()).removeTooMoreCache();
//        ((CvsRecyclerAdapter)mCvsRcc.getAdapter()).notifyDataSetChangedLog();
        mCvsRcc.getAdapter().notifyItemRangeInserted(mCvsRcc.getAdapter().getItemCount() ,1);
        animationScrollEnd();
    }

    @Override
    public void onNewFile(File file) {
        Log.d(TAG, "onNewFile");
        WeakReference<CvsNote> noteReference = mWeakImageNoteMap.get(file.getName());
        if(noteReference != null){
            CvsNote note = noteReference.get();
            if(note != null){
                Log.d(TAG, "onNewFile notifyItemChanged");
                ((CvsRecyclerAdapter)mCvsRcc.getAdapter()).notifyItemChanged(note);
                animationScrollEnd();
//                scrollEnd();
            }
            mWeakImageNoteMap.remove(file.getName());
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id){
            case R.id.btn_cvs_text:
                submit();
                break;
            case R.id.btn_cvs_last:
                if(!TextUtils.isEmpty(mLastSubmit)){
                    mEditContent.setText(mLastSubmit);
                    mEditContent.setSelection(mEditContent.getText() == null ?0:mEditContent.getText().toString().length());
                }
                break;
            case R.id.btn_cvs_img:
                choseImage();
            default:
                break;
        }
    }

    public void onEvent(EventNote event){
        CvsNote note = event.getCurrentNote();
        int act = event.getAction();
        switch (act){
            case EventNote.ACTION_DOWNLOAD_IMAGE:
                if(!mWeakImageNoteMap.containsKey(note.getContent())) {
                    mWeakImageNoteMap.put(note.getContent(), new WeakReference<>(note));
                    downloadImage(note.getContent());
                }
                break;
            case EventNote.ACTION_NEED_SEND:
                if(checkService()) {
                    mCvsServiceBinder.request(note);
                }
                break;
            case EventNote.ACTION_IMG_DETAIL:
                if(mImgDetailDialog == null){
                    mImgDetailDialog = new CvsImageDetailDialog();
                }
                mImgDetailDialog.setImagePath(new File(Application.App.getSocketRawFolder(), note.getContent()));
                mImgDetailDialog.show(getFragmentManager(), note.getContent());
                break;
        }
    }

    public void onEvent(EventImageLoaderComplete event){
        int curtPosition = event.getPosition();
        Log.d(TAG, "onEvent EventImageLoaderComplete curtPosition = " + curtPosition);
        if(curtPosition == (mCvsRcc.getAdapter().getItemCount() - 1)){
            int position = ((LinearLayoutManager)mCvsRcc.getLayoutManager()).findLastVisibleItemPosition();
            Log.d(TAG, "onEvent EventImageLoaderComplete VisibleItemPosition = " + position);
            if(curtPosition == position){
                animationScrollEnd();
            }
        }
    }
    //endregion

    //region 私有内部类。ScrollSpeedLinearLayoutManger，RecyclerView滑动速度
    private static class ScrollSpeedLinearLayoutManger extends LinearLayoutManager {
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
        mLastSubmit = content;
        List<String> cmds = FormatUtils.format(content);
        if(!LocalCmd.handleCmd(cmds) && checkService()){
            CvsNote note = mCvsServiceBinder.request(content, cmds);
            if(note != null){
                Application.App.getCvsHistoryManager().insertCache(note);
                mCvsRcc.getAdapter().notifyItemRangeInserted(mCvsRcc.getAdapter().getItemCount(), 1);
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
        String path = UriUtils.getPath(getContext(), uri);
        File file = new File(path);
        if(!file.exists()) return;
        File newFile = new File(Application.App.getSocketRawFolder(), Utils.makeSoleName());
        if(newFile.exists()) newFile.delete();
        try {
            FileUtils.copyFile(file, newFile);
            Log.d(TAG, "copyFile:"+ newFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(checkService()) {
            CvsNote note = mCvsServiceBinder.request(newFile);
            if (note != null) {
                note.setContent(newFile.getName());
                Application.App.getCvsHistoryManager().insertCache(note);
                mCvsRcc.getAdapter().notifyItemRangeInserted(mCvsRcc.getAdapter().getItemCount(), 1);
                animationScrollEnd(500);
                Application.App.getCvsHistoryManager().keepLastSendNote(note);
            }
        }
    }

    private void downloadImage(String name){
        if(checkService()) {
            mCvsServiceBinder.download(name);
        }
    }

    private void animationScrollEnd(long delayMillis) {
        mCvsRcc.postDelayed(new Runnable() {
            @Override
            public void run() {
                int position = mCvsRcc.getAdapter().getItemCount() - 1;
                Log.d(TAG, "smoothScrollToPosition " + position);
                if(position >= 0) {
                    mCvsRcc.smoothScrollToPosition(position);
                }
            }
        }, delayMillis);
    }

    private void animationScrollEnd() {
        mCvsRcc.post(new Runnable() {
            @Override
            public void run() {
                int position = mCvsRcc.getAdapter().getItemCount() - 1;
                Log.d(TAG, "smoothScrollToPosition " + position);
                if(position >= 0) {
                    mCvsRcc.smoothScrollToPosition(position);
                }
            }
        });
    }
    private void scrollEnd(){
        mCvsRcc.post(new Runnable() {
            @Override
            public void run() {
                int position = mCvsRcc.getAdapter().getItemCount() - 1;
                Log.d(TAG, "scrollEnd " + position);
                if(position >= 0) {
                    mCvsRcc.smoothScrollToPosition(position);
                    mCvsRcc.scrollToPosition(position);
                }
            }
        });
    }

    private boolean checkService(){
        if(mCvsServiceBinder == null){
            ToastUtils.show("服务未启动", Toast.LENGTH_SHORT);
            return false;
        }
        return true;
    }
    //endregion
}
