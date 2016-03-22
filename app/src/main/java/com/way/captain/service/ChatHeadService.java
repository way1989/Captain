package com.way.captain.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.way.captain.R;
import com.way.captain.activity.MainActivity;
import com.way.captain.floatview.FloatingView;
import com.way.captain.floatview.FloatingViewListener;
import com.way.captain.floatview.FloatingViewManager;
import com.way.captain.fragment.SettingsFragment;
import com.way.screenshot.TakeScreenshotActivity;
import com.way.telecine.TelecineShortcutLaunchActivity;


/**
 * ChatHead Service
 */
public class ChatHeadService extends Service implements FloatingViewListener, View.OnClickListener,
        View.OnLongClickListener, SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = "ChatHeadService";

    /**
     * 通知ID
     */
    private static final int NOTIFICATION_ID = 9083150;


    /**
     * FloatingViewManager
     */
    //private FloatingViewManager mFloatingViewManager;
    /**
     * Vibrator
     */
    private Vibrator mVibrator;
    private WindowManager mWindowManager;
    private FloatingView mFloatingView;
    private ImageView mIconView;

    private boolean mIsRunning;
    private SharedPreferences mPreferences;

    @Override
    public void onCreate() {
        super.onCreate();
        mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mIsRunning) {
            return START_STICKY;
        }
        mIsRunning = true;

        final DisplayMetrics metrics = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getMetrics(metrics);
        //final LayoutInflater inflater = LayoutInflater.from(this);
        //final ImageView iconView = (ImageView) inflater.inflate(R.layout.widget_chathead, null, false);
        mIconView = new ImageView(this);
        mIconView.setId(R.id.fab);
        mIconView.setImageResource(R.drawable.theme_captain);
        mIconView.setOnClickListener(this);
        mIconView.setOnLongClickListener(this);


//        mFloatingViewManager = new FloatingViewManager(this, this);
//        mFloatingViewManager.setFixedTrashIconImage(R.drawable.ic_trash_fixed);
//        mFloatingViewManager.setActionTrashIconImage(R.drawable.ic_trash_action);
//        final FloatingViewManager.Options options = new FloatingViewManager.Options();
//        options.shape = FloatingViewManager.SHAPE_CIRCLE;
//        options.overMargin = (int) (16 * metrics.density);
//        mFloatingViewManager.addViewToWindow(iconView, options);

        mFloatingView = new FloatingView(this);
        mFloatingView.setShape(FloatingViewManager.SHAPE_CIRCLE);
        mFloatingView.setOverMargin((int) (16 * metrics.density));
        mFloatingView.setInitCoords(metrics.widthPixels, metrics.heightPixels / 2);

        mFloatingView.addView(mIconView);
        mWindowManager.addView(mFloatingView, mFloatingView.getWindowLayoutParams());

        // 常駐起動
        //startForeground(NOTIFICATION_ID, createNotification());

        return START_REDELIVER_INTENT;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDestroy() {
        destroy();
        super.onDestroy();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onFinishFloatingView() {
        stopSelf();
    }

    /**
     * View
     */
    private void destroy() {
        /*if (mFloatingViewManager != null) {
            mFloatingViewManager.removeAllViewToWindow();
            mFloatingViewManager = null;
        }*/
        mPreferences.unregisterOnSharedPreferenceChangeListener(this);
        if (mFloatingView != null) {
            mIsRunning = false;
            mWindowManager.removeViewImmediate(mFloatingView);
            mFloatingView = null;
        }

    }

    /**
     * 通知を表示します。
     */
    private Notification createNotification() {
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setWhen(System.currentTimeMillis());
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentTitle(getString(R.string.chathead_content_title));
        builder.setContentText(getString(R.string.chathead_content_text));
        builder.setOngoing(true);
        builder.setPriority(NotificationCompat.PRIORITY_MIN);
        builder.setCategory(NotificationCompat.CATEGORY_SERVICE);

        // PendingIntent作成
        final Intent notifyIntent = new Intent(this, MainActivity.class);
        PendingIntent notifyPendingIntent = PendingIntent.getActivity(this, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(notifyPendingIntent);

        return builder.build();
    }


    @Override
    public void onClick(final View v) {
        mVibrator.vibrate(30);

        v.animate().alpha(0).withEndAction(new Runnable() {
            @Override
            public void run() {
//                mFloatingView.setDraggable(false);
                v.setVisibility(View.GONE);
                PreferenceManager.getDefaultSharedPreferences(ChatHeadService.this).edit()
                        .putBoolean(SettingsFragment.HIDE_FLOATVIEW_KEY, true).apply();
            }
        });
        Intent i = new Intent(ChatHeadService.this, TelecineShortcutLaunchActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

    @Override
    public boolean onLongClick(final View v) {
        mVibrator.vibrate(40);
        v.animate().alpha(0f).withEndAction(new Runnable() {
            @Override
            public void run() {
//                mFloatingView.setDraggable(false);
                v.setVisibility(View.GONE);
                PreferenceManager.getDefaultSharedPreferences(ChatHeadService.this).edit()
                        .putBoolean(SettingsFragment.HIDE_FLOATVIEW_KEY, true).apply();
                Intent i = new Intent(ChatHeadService.this, TakeScreenshotActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            }
        });

        return true;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.i(TAG, "onSharedPreferenceChanged... key = " + key);
        if (key.equals(SettingsFragment.HIDE_FLOATVIEW_KEY)) {
            if (!sharedPreferences.getBoolean(SettingsFragment.HIDE_FLOATVIEW_KEY, false)) {
                mIconView.setVisibility(View.VISIBLE);
                mIconView.animate().alpha(1);
//                mFloatingView.setDraggable(true);
            }
        }
    }
}
