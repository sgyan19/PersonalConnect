package com.sun.personalconnect;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.sun.conversation.CvsFragment;

/**
 * Created by guoyao on 2017/4/14.
 */
public class HomeActivity extends BaseActivity{

    private CvsFragment mCvsFragment;
    private EntryFragment mEntryFragment;
//    private

    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        mCvsFragment = new CvsFragment();
        mEntryFragment = new EntryFragment();


        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.container_home_main,mCvsFragment);
        fragmentTransaction.replace(R.id.container_home_left_drawer,mEntryFragment);
        fragmentTransaction.commitAllowingStateLoss();
    }
}
