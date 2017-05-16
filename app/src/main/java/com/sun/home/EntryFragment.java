package com.sun.home;

import android.content.Intent;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.gson.Gson;
import com.sun.account.AccountActivity;
import com.sun.connect.AppLifeNetworkService;
import com.sun.connect.EventNetwork;
import com.sun.device.AnswerNote;
import com.sun.device.AskNote;
import com.sun.device.UsersFragment;
import com.sun.gps.GaoDeMapActivity;
import com.sun.gps.GpsActivity;
import com.sun.personalconnect.Application;
import com.sun.personalconnect.InfoKeeper;
import com.sun.personalconnect.R;
import com.sun.utils.FormatUtils;
import com.sun.utils.GsonUtils;
import com.sun.utils.IdUtils;
import com.sun.utils.PageFragmentActivity;
import com.sun.utils.StatusFragment;
import com.sun.utils.ToastUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by guoyao on 2017/4/18.
 */
public class EntryFragment extends Fragment implements OnClickListener{
    private static final String TAG = "EntryFragment";

    private StatusFragment mUserCountFragment;
    private HashSet<String> mRequestKeys;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        mUserCountFragment = new StatusFragment();
        mRequestKeys = new HashSet<>();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_entry, container, false);
//        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.btn_entry_users).setOnClickListener(this);
        view.findViewById(R.id.btn_entry_gps).setOnClickListener(this);
        view.findViewById(R.id.btn_logout).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id){
            case R.id.btn_entry_users:
                String key = IdUtils.make();
                mRequestKeys.add(key);
                AppLifeNetworkService.getInstance().request(key, GsonUtils.mGson.toJson(FormatUtils.makeAskRequest(null)));
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    public void onEvent(EventNetwork eventNetwork){
        if(mRequestKeys.contains(eventNetwork.getKey())){
            mRequestKeys.remove(eventNetwork.getKey());
            if(TextUtils.isEmpty(eventNetwork.getError())){
                ToastUtils.show("请求错误 error:" + eventNetwork.getError() , Toast.LENGTH_SHORT);
                Log.d(TAG, String.format("请求错误 error:%s,step:%d",eventNetwork.getError(),eventNetwork.getStep()));
            }else{
                Log.d(TAG, "请求设备信息成功");
            }
            return;
        }
        if(eventNetwork.getObject() instanceof AnswerNote){
            if(!TextUtils.isEmpty(eventNetwork.getError())){
                ToastUtils.show("请求用户数error:" + eventNetwork.getError() , Toast.LENGTH_SHORT);
            }else{
                InfoKeeper.getInstance().putAnswer((AnswerNote) eventNetwork.getObject());
                List<String> info = new ArrayList<>();
                for(AnswerNote item :InfoKeeper.getInstance().getAnswers()){
                    info.add(item.toString());
                }
                mUserCountFragment.setMessages(info);
            }
        }else if(eventNetwork.getObject() instanceof AskNote){
//            if(eventNetwork.getKey() != ){
//
//            }
            if(TextUtils.isEmpty(eventNetwork.getError())){
                AppLifeNetworkService.getInstance().request(IdUtils.make(), GsonUtils.mGson.toJson(FormatUtils.makeAnswerRequest(null)));
            }
        }
    }
}
