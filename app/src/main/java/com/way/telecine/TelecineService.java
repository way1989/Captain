package com.way.telecine;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.os.StatFs;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.format.DateUtils;
import android.util.Log;
import android.widget.Toast;

import com.way.captain.R;
import com.way.captain.fragment.SettingsFragment;
import com.way.captain.utils.AppUtils;

import java.util.Timer;
import java.util.TimerTask;

public final class TelecineService extends Service {
    public static final String ACTION_STOP_SCREENRECORD = "com.way.ACTION_STOP_SCREENRECORD";
    private static final String EXTRA_RESULT_CODE = "result-code";
    private static final String EXTRA_DATA = "data";
    private static final int NOTIFICATION_ID = 99118822;
    private static final String SHOW_TOUCHES = "show_touches";
    Boolean showCountdownProvider;
    Integer videoSizePercentageProvider;
    Boolean showTouchesProvider;
    ContentResolver contentResolver;
    private long startTime;
    private Timer mTimer;
    private Notification.Builder mBuilder;
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
            startTime = SystemClock.elapsedRealtime();
            mBuilder = createNotificationBuilder();
            mTimer = new Timer();
            mTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    updateNotification(TelecineService.this);
                }
            }, 100, 1000);

//            Context context = getApplicationContext();
//            String title = context.getString(R.string.notification_recording_title);
//            //String subtitle = context.getString(R.string.notification_recording_subtitle);
//            Notification.Builder builder  = new Notification.Builder(context) //
//                    .setContentTitle(title)/*.setContentText(subtitle)*/.setSmallIcon(R.drawable.ic_videocam_white_24dp)
//                    .setColor(context.getResources().getColor(R.color.primary_normal)).setAutoCancel(true)
//                    .setPriority(PRIORITY_MIN);
//            Intent stopIntent = new Intent("com.way.stop");
//            stopIntent.putExtra("id", NOTIFICATION_ID);
//            builder.addAction(R.drawable.ic_clear_white_24dp,context.getResources()
//                    .getString(R.string.share), PendingIntent.getBroadcast(context, 0, stopIntent, PendingIntent.FLAG_CANCEL_CURRENT));
//            Notification notification  = builder.build();
//            Log.d("way", "Moving service into the foreground with recording notification.");
//            startForeground(NOTIFICATION_ID, notification);
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

    private boolean hasAvailableSpace() {
        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        long bytesAvailable = stat.getBlockSizeLong() * stat.getBlockCountLong();
        long megAvailable = bytesAvailable / 1048576;
        return megAvailable >= 100;
    }

    private Notification.Builder createNotificationBuilder() {
        Notification.Builder builder = new Notification.Builder(this)
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_videocam_white_24dp)
                .setContentTitle(getString(R.string.notification_recording_title));
        Intent stopRecording = new Intent(ACTION_STOP_SCREENRECORD);
        //stopRecording.setClass(this, TelecineService.class);
        stopRecording.putExtra("id", NOTIFICATION_ID);
        builder.addAction(R.drawable.stop, getString(R.string.stop),
                PendingIntent.getBroadcast(this, 0, stopRecording, PendingIntent.FLAG_CANCEL_CURRENT));
        return builder;
    }

    public void updateNotification(Context context) {
        long timeElapsed = SystemClock.elapsedRealtime() - startTime;
        mBuilder.setContentText(getString(R.string.video_length,
                DateUtils.formatElapsedTime(timeElapsed / 1000)));
        startForeground(NOTIFICATION_ID, mBuilder.build());
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
        if (!hasAvailableSpace()) {
            Toast.makeText(this, R.string.not_enough_storage, Toast.LENGTH_LONG).show();
            return START_NOT_STICKY;
        }

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
