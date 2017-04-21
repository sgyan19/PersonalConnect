package com.sun.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.sun.personalconnect.R;

/**
 * Created by guoyao on 2017/4/17.
 */
public class SettingFragment extends PreferenceFragment {
    private SharedPreferences mSharedPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
        addPreferencesFromResource(R.xml.settings);
    }


}
