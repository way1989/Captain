package com.way.captain.fragment;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;

import com.way.captain.R;
import com.way.captain.service.ChatHeadService;
import com.way.captain.utils.AppUtils;

/**
 * Created by android on 16-2-4.
 */
public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    public static final String HIDE_FLOATVIEW_KEY = "key_hide_floatview";
    public static final String VIDEO_SIZE_KEY = "key_video_size_percentage";
    public static final String VIDEO_STOP_METHOD_KEY = "key_video_stop_method";
    public static final String SHOW_COUNTDOWN_KEY = "key_three_second_countdown";
    public static final String SHOW_TOUCHES_KEY = "key_show_touches";
    public static final String ATOUCH_KEY = "key_use_atouch";
    public static final String BOOT_AUTO_KEY = "key_boot_atuo";
    private static final String VERSION_KEY = "key_version";
    private Activity mContext;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
        mContext = getActivity();
        PackageManager packageManager = mContext.getPackageManager();
        String packageName = mContext.getPackageName();
        // Update the version number
        try {
            final PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
            findPreference(VERSION_KEY).setSummary(packageInfo.versionName);
        } catch (final PackageManager.NameNotFoundException e) {
            findPreference(VERSION_KEY).setSummary("?");
        }
        ListPreference sizeListPreference = (ListPreference) findPreference(VIDEO_SIZE_KEY);
        sizeListPreference.setSummary(sizeListPreference.getEntry());
        if (AppUtils.isMarshmallow()) {
            PreferenceCategory preferenceScreen = (PreferenceCategory) findPreference("key_advance_category");
            preferenceScreen.removePreference(findPreference(SHOW_TOUCHES_KEY));
        }
        PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        String key = preference.getKey();
        switch (key) {
            case ATOUCH_KEY:
                SwitchPreference switchPreference = (SwitchPreference) preference;
                if (switchPreference.isChecked())
                    mContext.startService(new Intent(mContext, ChatHeadService.class));
                else
                    mContext.stopService(new Intent(mContext, ChatHeadService.class));
                break;

        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case VIDEO_SIZE_KEY:
                ListPreference listPreference = (ListPreference) findPreference(key);
                listPreference.setSummary(listPreference.getEntry());
                break;
        }
    }
}
