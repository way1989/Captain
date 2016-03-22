package com.way.captain;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;

import com.bugtags.library.Bugtags;
import com.squareup.leakcanary.LeakCanary;
import com.way.captain.fragment.SettingsFragment;
import com.way.captain.service.ChatHeadService;

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
        mContext = this;
        LeakCanary.install(this);

        //在这里初始化
//        BTGInvocationEventNone    // 静默模式，只收集 Crash 信息（如果允许）
//        BTGInvocationEventShake   // 通过摇一摇呼出 Bugtags
//        BTGInvocationEventBubble  // 通过悬浮小球呼出 Bugtags
        if (BuildConfig.BUGTAG_ENABLED)
            Bugtags.start(BuildConfig.BUGTAG_APPKEY, this, Bugtags.BTGInvocationEventBubble);
        else
            Bugtags.start(BuildConfig.BUGTAG_APPKEY, this, Bugtags.BTGInvocationEventNone);

        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(SettingsFragment.ATOUCH_KEY, true))
            startService(new Intent(this, ChatHeadService.class));
    }
}
