package com.way.telecine;

import android.app.Activity;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.os.Bundle;

import com.way.captain.R;

public final class TelecineShortcutConfigureActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent launchIntent = new Intent(this, TelecineShortcutLaunchActivity.class);
		ShortcutIconResource icon = ShortcutIconResource.fromContext(this, R.mipmap.ic_launcher);

		Intent intent = new Intent();
		intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.shortcut_name));
		intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);
		intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, launchIntent);

		setResult(RESULT_OK, intent);
		finish();
	}
}
