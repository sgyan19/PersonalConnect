package com.sun.home;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Toast;

import com.sun.account.AccountActivity;
import com.sun.camera.CameraActivity;
import com.sun.common.SessionNote;
import com.sun.connect.NetworkChannel;
import com.sun.connect.EventNetwork;
import com.sun.device.AnswerNote;
import com.sun.device.AskNote;
import com.sun.device.DeviceInfo;
import com.sun.utils.NoteHelper;
import com.sun.gps.GaoDeMapActivity;
import com.sun.level.UpdateOrderNote;
import com.sun.personalconnect.Application;
import com.sun.personalconnect.InfoKeeper;
import com.sun.personalconnect.R;
import com.sun.utils.FormatUtils;
import com.sun.utils.PageFragmentActivity;
import com.sun.utils.StatusFragment;
import com.sun.utils.ToastUtils;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by guoyao on 2017/4/18.
 */

@EFragment(R.layout.fragment_entry)
public class EntryFragment extends Fragment implements OnClickListener{
    private static final String TAG = "EntryFragment";

    private StatusFragment mUserCountFragment;
    private List<AnswerNote> mAnswerNotes;
    private enum AskStatus{
        Info,Update,Upgrade,Gps
    }
    private AskStatus mAskStatus = AskStatus.Info;
    private UpdateOrderNote mUpdateOrderNote;

    @ViewById(R.id.btn_entry_camera)
    protected Button mCameraBtn;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        mUserCountFragment = new StatusFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        return inflater.inflate(R.layout.fragment_entry, container, false);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.btn_entry_users).setOnClickListener(this);
        view.findViewById(R.id.btn_entry_gps).setOnClickListener(this);
        view.findViewById(R.id.btn_logout).setOnClickListener(this);

        mUserCountFragment.setOnItemclickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(mAnswerNotes == null){
                    return;
                }
                AnswerNote note = mAnswerNotes.get(position);
                if(note == null) return;
                switch (mAskStatus){
                    case Info:
                        askDeviceInfo(note);
                        break;
                    case Update:
                        askNewAnswerNote(note);
                        break;
                    case Upgrade:
                        File file = new File(Application.App.getPackageResourcePath());
                        NetworkChannel.getInstance().upload(file);
                        mUpdateOrderNote = NoteHelper.makeUpdateOrderNote(note);
                        mUpdateOrderNote.setApkName(file.getName());
                        break;
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id){
            case R.id.btn_entry_users:
                mAskStatus = AskStatus.Info;
                AskNote ask = new AskNote();
                NetworkChannel.getInstance().request(FormatUtils.makeRequest(null,ask));
                InfoKeeper.getInstance().putAnswer((AnswerNote) NoteHelper.makeAnswer(ask));
                updateAnswerNoteList();
                PageFragmentActivity.fastJump(getActivity(), mUserCountFragment);
                break;
            case R.id.btn_entry_gps:
                startActivity(new Intent(getActivity(), GaoDeMapActivity.class));
                break;
            case R.id.btn_logout:
                Application.App.getAccount().logout();
                startActivity(new Intent(getActivity(), AccountActivity.class));
                getActivity().finish();
                break;
        }
    }
    @Click({R.id.btn_entry_camera,R.id.btn_entry_upgrade})
    public void click(View view){
        switch(view.getId()){
            case R.id.btn_entry_camera:  //
                startActivity(new Intent(getActivity(), CameraActivity.class));
                break;
            case R.id.btn_entry_upgrade:
                mAskStatus = AskStatus.Upgrade;
                AskNote ask = new AskNote();
                NetworkChannel.getInstance().request(FormatUtils.makeRequest(null,ask));
                InfoKeeper.getInstance().putAnswer((AnswerNote) NoteHelper.makeAnswer(ask));
                updateAnswerNoteList();
                PageFragmentActivity.fastJump(getActivity(), mUserCountFragment);
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    public void onEvent(EventNetwork eventNetwork){
        if(eventNetwork.isMine()){
            if(!TextUtils.isEmpty(eventNetwork.getError())){
                ToastUtils.show("请求错误 error:" + eventNetwork.getError() , Toast.LENGTH_SHORT);
                Log.d(TAG, String.format("请求错误 error:%s,step:%d",eventNetwork.getError(),eventNetwork.getStep()));
            }else{
                Log.d(TAG, "请求设备信息成功");
                if(mUpdateOrderNote != null) {
                    if (eventNetwork.getObject() instanceof String && eventNetwork.getObject().equals(mUpdateOrderNote.getApkName())) {
                        ToastUtils.show("上传apk成功:" + mUpdateOrderNote.getApkName() , Toast.LENGTH_SHORT);
                        NetworkChannel.getInstance().request(FormatUtils.makeRequest(null, mUpdateOrderNote));
                    }else if(eventNetwork.getObject() instanceof UpdateOrderNote){
                        mUpdateOrderNote = null;
                    }
                }
            }
            return;
        }
        if(eventNetwork.getObject() instanceof AnswerNote){
            if(!TextUtils.isEmpty(eventNetwork.getError())){
                ToastUtils.show("请求用户数error:" + eventNetwork.getError() , Toast.LENGTH_SHORT);
            }else{
                InfoKeeper.getInstance().putAnswer((AnswerNote) eventNetwork.getObject());
                updateAnswerNoteList();
            }
        }else if(eventNetwork.getObject() instanceof DeviceInfo){
            if(TextUtils.isEmpty(eventNetwork.getError())){
                showDeviceInfo((DeviceInfo)eventNetwork.getObject());
            }
        }
    }

    private void askNewAnswerNote(AnswerNote note){
        AskNote askNote = new AskNote(AskNote.TYPE_EASY);
        askNote.setSessionType(SessionNote.TYPE_DEVICE);
        askNote.addSessionCondition(note.getDeviceId());
        askNote.addSessionCondition(Application.App.getDeviceId());
        if(note.getDeviceId().equals(Application.App.getDeviceId())){
            // 如果请求自己就不去访问网络了
            NoteHelper.makeAnswer(askNote);
            showDeviceInfo((DeviceInfo) NoteHelper.makeAnswer(askNote));
        }else{
            NetworkChannel.getInstance().request(FormatUtils.makeRequest(null, askNote));
        }
    }

    private void askDeviceInfo(AnswerNote note){
        AskNote askNote = new AskNote(AskNote.TYPE_DETAIL);
        askNote.setSessionType(SessionNote.TYPE_DEVICE);
        askNote.addSessionCondition(note.getDeviceId());
        askNote.addSessionCondition(Application.App.getDeviceId());
        if(note.getDeviceId().equals(Application.App.getDeviceId())){
            // 如果请求自己就不去访问网络了
            NoteHelper.makeAnswer(askNote);
            showDeviceInfo((DeviceInfo) NoteHelper.makeAnswer(askNote));
        }else{
            NetworkChannel.getInstance().request(FormatUtils.makeRequest(null, askNote));
        }
    }

    private void orderUpgrade(AnswerNote note){

    }

    private void showDeviceInfo(DeviceInfo info){
        ToastUtils.show( info.toString(),Toast.LENGTH_LONG);
    }

    private void updateAnswerNoteList(){
        if(mAnswerNotes == null) {
            mAnswerNotes = new ArrayList<>();
        }
        mAnswerNotes.clear();
        mAnswerNotes.addAll(InfoKeeper.getInstance().getAnswers());

        List<String> info = new ArrayList<>();
        for(AnswerNote item :mAnswerNotes){
            info.add(item.toString());
        }
        mUserCountFragment.setMessages(info);
    }
}
