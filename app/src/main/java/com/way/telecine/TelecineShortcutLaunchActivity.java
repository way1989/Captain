package com.way.telecine;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.way.captain.fragment.SettingsFragment;

public final class TelecineShortcutLaunchActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CaptureHelper.fireScreenCaptureIntent(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!CaptureHelper.handleActivityResult(this, requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
            PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(SettingsFragment.HIDE_FLOATVIEW_KEY, false).apply();
        }
        finish();
    }

    @Override
    protected void onStop() {
        if (!isFinishing()) {
            finish();
        }
        super.onStop();
    }
}
