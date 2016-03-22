package com.way.screenshot;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import com.way.captain.R;


public class DeleteScreenshot extends BroadcastReceiver {
    // Intent extra fields
    public static final String SCREENSHOT_URI = "com.android.systemui.SCREENSHOT_URI";

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle extras = intent.getExtras();

        if (extras == null) {
            // We have nothing, abort
            return;
        }

        Uri screenshotUri = Uri.parse(extras.getString(SCREENSHOT_URI));
        if (screenshotUri != null) {
            context.getContentResolver().delete(screenshotUri, null, null);
        }

        // Dismiss the notification that brought us here.
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(GlobalScreenshot.SCREENSHOT_NOTIFICATION_ID);

        Toast.makeText(context, R.string.screenshot_delete_confirmation, Toast.LENGTH_SHORT).show();
    }
}
