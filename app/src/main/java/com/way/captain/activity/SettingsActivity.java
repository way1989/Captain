package com.way.captain.activity;

import android.os.Bundle;

import com.way.captain.R;
import com.way.captain.fragment.SettingsFragment;

/**
 * Created by android on 16-2-4.
 */
public class SettingsActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_layout);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction().replace(R.id.settings_fragment, new SettingsFragment()).commit();
        }
    }
}
