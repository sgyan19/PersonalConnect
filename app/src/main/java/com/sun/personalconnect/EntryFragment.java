package com.sun.personalconnect;

import android.content.Intent;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.gson.Gson;
import com.sun.connect.AppLifeNetworkService;
import com.sun.connect.EventNetwork;
import com.sun.device.AnswerNote;
import com.sun.device.AskNote;
import com.sun.device.UsersFragment;
import com.sun.gps.GaoDeMapActivity;
import com.sun.gps.GpsActivity;
import com.sun.utils.FormatUtils;
import com.sun.utils.GsonUtils;
import com.sun.utils.IdUtils;
import com.sun.utils.PageFragmentActivity;
import com.sun.utils.StatusFragment;
import com.sun.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by guoyao on 2017/4/18.
 */
public class EntryFragment extends Fragment implements OnClickListener{

    private StatusFragment mUserCountFragment;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        mUserCountFragment = new StatusFragment();
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
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id){
            case R.id.btn_entry_users:
                AppLifeNetworkService.getInstance().request(IdUtils.make(), GsonUtils.mGson.toJson(FormatUtils.makeAskRequest(null)));
                PageFragmentActivity.fastJump(getActivity(), mUserCountFragment);
                break;
            case R.id.btn_entry_gps:
                startActivity(new Intent(getActivity(), GaoDeMapActivity.class));
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    public void onEvent(EventNetwork eventNetwork){
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
                AppLifeNetworkService.getInstance().request(IdUtils.make(), GsonUtils.mGson.toJson(FormatUtils.makeAskRequest(null)));
            }
        }
    }
}
