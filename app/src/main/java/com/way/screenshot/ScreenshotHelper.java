package com.way.screenshot;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.util.Log;

final class ScreenshotHelper {
    private static final int CREATE_SCREENSHOT = 100;

    private ScreenshotHelper() {
        throw new AssertionError("No instances.");
    }

    static void fireScreenCaptureIntent(Activity activity) {
        MediaProjectionManager manager = (MediaProjectionManager) activity
                .getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        Intent intent = manager.createScreenCaptureIntent();
        activity.startActivityForResult(intent, CREATE_SCREENSHOT);
        Log.i("TakeScreenshotService", "fireScreenCaptureIntent...");
    }

    static boolean handleActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        Log.i("TakeScreenshotService", "handleActivityResult... requestCode = " + requestCode);
        if (requestCode != CREATE_SCREENSHOT) {
            return false;
        }
        if (resultCode == Activity.RESULT_OK) {
            Log.d("TakeScreenshotService", "Acquired permission to screen capture. Starting service.");
            activity.startService(TakeScreenshotService.newIntent(activity, resultCode, data));
        } else {
            Log.d("TakeScreenshotService", "Failed to acquire permission to screen capture.");
            return false;
        }
        return true;
    }
}
