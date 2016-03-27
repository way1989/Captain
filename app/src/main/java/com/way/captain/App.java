package com.way.captain;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;

import com.squareup.leakcanary.LeakCanary;
import com.way.captain.fragment.SettingsFragment;
import com.way.captain.service.ChatHeadService;

import im.fir.sdk.FIR;

/**
 * Created by android on 16-2-4.
 */
public class App extends Application {
    private static Context mContext;

    public static Context getContext() {
        return mContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.FIR_ENABLED)
            FIR.init(this);
        mContext = this;
        LeakCanary.install(this);

        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(SettingsFragment.ATOUCH_KEY, true))
            startService(new Intent(this, ChatHeadService.class));
    }
}
