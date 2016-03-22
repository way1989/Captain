package com.way.telecine;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;

import com.way.captain.R;
import com.way.captain.fragment.SettingsFragment;
import com.way.captain.service.ChatHeadService;
import com.way.captain.utils.AppUtils;

import static android.app.Notification.PRIORITY_MIN;

public final class TelecineService extends Service {
    private static final String EXTRA_RESULT_CODE = "result-code";
    private static final String EXTRA_DATA = "data";
    private static final int NOTIFICATION_ID = 99118822;
    private static final String SHOW_TOUCHES = "show_touches";
    Boolean showCountdownProvider;
    Integer videoSizePercentageProvider;
    Boolean showTouchesProvider;
    ContentResolver contentResolver;
    private final RecordingSession.Listener listener = new RecordingSession.Listener() {
        private int showTouch = 0;

        @TargetApi(Build.VERSION_CODES.M)
        @Override
        public void onStart() {
            if (showTouchesProvider) {
                showTouch = Settings.System.getInt(contentResolver, SHOW_TOUCHES, 0);
                if (!AppUtils.isMarshmallow())
                    Settings.System.putInt(contentResolver, SHOW_TOUCHES, 1);
            }


            Context context = getApplicationContext();
            String title = context.getString(R.string.notification_recording_title);
            //String subtitle = context.getString(R.string.notification_recording_subtitle);
            Notification notification = new Notification.Builder(context) //
                    .setContentTitle(title)/*.setContentText(subtitle)*/.setSmallIcon(R.drawable.ic_videocam_white_24dp)
                    .setColor(context.getResources().getColor(R.color.primary_normal)).setAutoCancel(true)
                    .setPriority(PRIORITY_MIN).build();

            Log.d("way", "Moving service into the foreground with recording notification.");
            startForeground(NOTIFICATION_ID, notification);
        }

        @TargetApi(Build.VERSION_CODES.M)
        @Override
        public void onStop() {
            if (showTouchesProvider) {
                if (!AppUtils.isMarshmallow())
                    Settings.System.putInt(contentResolver, SHOW_TOUCHES, showTouch);
            }

            stopForeground(true /* remove notification */);
        }

        @Override
        public void onEnd() {
            Log.d("way", "Shutting down.");
            stopSelf();
        }
    };
    private SharedPreferences mSharedPreferences;
    private boolean running;
    private RecordingSession recordingSession;

    public static Intent newIntent(Context context, int resultCode, Intent data) {
        Intent intent = new Intent(context, TelecineService.class);
        intent.putExtra(EXTRA_RESULT_CODE, resultCode);
        intent.putExtra(EXTRA_DATA, data);
        return intent;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        contentResolver = getContentResolver();
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (running) {
            Log.d("way", "Already running! Ignoring...");
            return START_NOT_STICKY;
        }
        Log.d("way", "Starting up!");
        running = true;

        int resultCode = intent.getIntExtra(EXTRA_RESULT_CODE, 0);
        Intent data = intent.getParcelableExtra(EXTRA_DATA);
        if (resultCode == 0 || data == null) {
            throw new IllegalStateException("Result code or data missing.");
        }
        showTouchesProvider = mSharedPreferences.getBoolean(SettingsFragment.SHOW_TOUCHES_KEY, true);
        showCountdownProvider = mSharedPreferences.getBoolean(SettingsFragment.SHOW_COUNTDOWN_KEY, false);
        videoSizePercentageProvider = Integer.valueOf(mSharedPreferences.getString(SettingsFragment.VIDEO_SIZE_KEY, "100"));
        recordingSession = new RecordingSession(this, listener, resultCode, data, showCountdownProvider,
                videoSizePercentageProvider);
        recordingSession.showOverlay();

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        recordingSession.destroy();
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(SettingsFragment.HIDE_FLOATVIEW_KEY, false).apply();
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new AssertionError("Not supported.");
    }
}
