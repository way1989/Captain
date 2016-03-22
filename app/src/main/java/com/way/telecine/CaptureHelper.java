package com.way.telecine;

import android.app.Activity;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.util.Log;

import static android.content.Context.MEDIA_PROJECTION_SERVICE;

public final class CaptureHelper {
    private static final int CREATE_SCREEN_CAPTURE = 4242;

    private CaptureHelper() {
        throw new AssertionError("No instances.");
    }

    static void fireScreenCaptureIntent(Activity activity) {
        MediaProjectionManager manager = (MediaProjectionManager) activity.getSystemService(MEDIA_PROJECTION_SERVICE);
        Intent intent = manager.createScreenCaptureIntent();
        activity.startActivityForResult(intent, CREATE_SCREEN_CAPTURE);
    }

    static boolean handleActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        if (requestCode != CREATE_SCREEN_CAPTURE) {
            return false;
        }

        if (resultCode == Activity.RESULT_OK) {
            Log.d("way", "Acquired permission to screen capture. Starting service.");
            activity.startService(TelecineService.newIntent(activity, resultCode, data));
        } else {
            Log.d("way", "Failed to acquire permission to screen capture.");
            return false;
        }
        return true;
    }
}
