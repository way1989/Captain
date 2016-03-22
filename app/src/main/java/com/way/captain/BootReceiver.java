package com.way.captain;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;

import com.way.captain.fragment.SettingsFragment;
import com.way.captain.service.ChatHeadService;

/**
 * Created by android on 16-2-4.
 */
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(SettingsFragment.ATOUCH_KEY, true)
                    && PreferenceManager.getDefaultSharedPreferences(context).getBoolean(SettingsFragment.BOOT_AUTO_KEY, true))
                context.startService(new Intent(context, ChatHeadService.class));
        }

    }
}
