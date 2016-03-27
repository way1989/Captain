package com.way.telecine;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.way.captain.R;
import com.way.captain.fragment.SettingsFragment;

import java.util.Locale;

import static android.graphics.PixelFormat.TRANSLUCENT;
import static android.os.Build.VERSION_CODES.LOLLIPOP_MR1;
import static android.text.TextUtils.getLayoutDirectionFromLocale;
import static android.view.WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR;
import static android.view.WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
import static android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
import static android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
import static android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
import static android.view.WindowManager.LayoutParams.TYPE_TOAST;

final class OverlayView extends FrameLayout {
    private static final String TAG = "OverlayView";
    private static final int COUNTDOWN_DELAY = 1000;
    private static final int NON_COUNTDOWN_DELAY = 500;
    private static final int DURATION_ENTER_EXIT = 300;
    private final Listener listener;
    private final boolean showCountDown;
    private View mStartContainer;
    private View mCancelButton;
    private TextView mQualityTextView;
    private View mStartButton;
    private View mStopContainer;
    private View mStopButton;
    private TextView mCountDownTextView;
    private TextView mRecordingTimeTextView;
    private long mRecordingStartTime;
    OnClickListener clickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.record_overlay_cancel:
                    onCancelClicked();
                    break;
                case R.id.record_overlay_start:
                    onStartClicked();
                    break;
                case R.id.record_change_quality:
                    int videoSizePercentageProvider = Integer.valueOf(PreferenceManager.getDefaultSharedPreferences(getContext()).getString(SettingsFragment.VIDEO_SIZE_KEY, "100"));
                    Log.i(TAG, "onClick... videoSizePercentageProvider = " + videoSizePercentageProvider);
                    switch (videoSizePercentageProvider) {
                        case 100:
                            PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putString(SettingsFragment.VIDEO_SIZE_KEY, "50").commit();
                            mQualityTextView.setText(R.string.float_record_normal_quality);
                            break;
                        case 75:
                            PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putString(SettingsFragment.VIDEO_SIZE_KEY, "100").commit();
                            mQualityTextView.setText(R.string.float_record_super_hd_quality);
                            break;
                        case 50:
                            PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putString(SettingsFragment.VIDEO_SIZE_KEY, "75").commit();
                            mQualityTextView.setText(R.string.float_record_hd_quality);
                            break;
                        default:
                            PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putString(SettingsFragment.VIDEO_SIZE_KEY, "100").commit();
                            mQualityTextView.setText(R.string.float_record_super_hd_quality);
                            break;
                    }
                    break;
                default:
                    break;
            }
        }
    };

    //private int animationWidth;

    private OverlayView(Context context, Listener listener, boolean showCountDown) {
        super(context);
        this.listener = listener;
        this.showCountDown = showCountDown;

        inflate(context, R.layout.float_screen_record_control, this);
        initViews();
//        animationWidth = getResources().getDimensionPixelOffset(R.dimen.overlay_width);
//        if (getLayoutDirectionFromLocale(Locale.getDefault()) == LAYOUT_DIRECTION_RTL) {
//            animationWidth = -animationWidth; // Account for animating in from
//            // the other side of screen.
//        }
        CheatSheet.setup(mCancelButton);
        CheatSheet.setup(mStartButton);
    }

    static OverlayView create(Context context, Listener listener, boolean showCountDown) {
        return new OverlayView(context, listener, showCountDown);
    }

    static WindowManager.LayoutParams createLayoutParams(Context context) {
        Resources res = context.getResources();
        int width = res.getDimensionPixelSize(R.dimen.overlay_width);
        int height = res.getDimensionPixelSize(R.dimen.overlay_height);
        // TODO Remove explicit "M" comparison when M is released.
        if (Build.VERSION.SDK_INT > LOLLIPOP_MR1 || "M".equals(Build.VERSION.RELEASE)) {
            height = res.getDimensionPixelSize(R.dimen.overlay_height_m);
        }
        width = WindowManager.LayoutParams.WRAP_CONTENT;
        height = WindowManager.LayoutParams.WRAP_CONTENT;
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(width, height, TYPE_TOAST,
                FLAG_NOT_FOCUSABLE | FLAG_NOT_TOUCH_MODAL | FLAG_LAYOUT_NO_LIMITS | FLAG_LAYOUT_INSET_DECOR
                        | FLAG_LAYOUT_IN_SCREEN,
                TRANSLUCENT);
        params.y = res.getDimensionPixelSize(R.dimen.overlay_height) * 2;
        params.gravity = Gravity.TOP | Gravity.CENTER;

        return params;
    }

    @SuppressLint("RtlHardcoded") // Gravity.END is not honored by WindowManager
    // for added views.
    private static int gravityEndLocaleHack() {
        int direction = getLayoutDirectionFromLocale(Locale.getDefault());
        return direction == LAYOUT_DIRECTION_RTL ? Gravity.LEFT : Gravity.RIGHT;
    }

    private static String millisecondToTimeString(long milliSeconds,
                                                  boolean displayCentiSeconds) {
        long seconds = milliSeconds / 1000; // round down to compute seconds
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long remainderMinutes = minutes - (hours * 60);
        long remainderSeconds = seconds - (minutes * 60);

        StringBuilder timeStringBuilder = new StringBuilder();

        // Hours
        if (hours > 0) {
            if (hours < 10) {
                timeStringBuilder.append('0');
            }
            timeStringBuilder.append(hours);

            timeStringBuilder.append(':');
        } else {
            //timeStringBuilder.append('0');
            //timeStringBuilder.append('0');
            //timeStringBuilder.append(':');
        }

        // Minutes
        if (remainderMinutes < 10) {
            timeStringBuilder.append('0');
        }
        timeStringBuilder.append(remainderMinutes);
        timeStringBuilder.append(':');

        // Seconds
        if (remainderSeconds < 10) {
            timeStringBuilder.append('0');
        }
        timeStringBuilder.append(remainderSeconds);

        // Centi seconds
        if (displayCentiSeconds) {
            timeStringBuilder.append('.');
            long remainderCentiSeconds = (milliSeconds - seconds * 1000) / 10;
            if (remainderCentiSeconds < 10) {
                timeStringBuilder.append('0');
            }
            timeStringBuilder.append(remainderCentiSeconds);
        }

        return timeStringBuilder.toString();
    }

    private void initViews() {
        mStartContainer = findViewById(R.id.record_overlay_buttons);
        mCancelButton = findViewById(R.id.record_overlay_cancel);
        mQualityTextView = (TextView) findViewById(R.id.show_record_quality);
        mStartButton = findViewById(R.id.record_overlay_start);
        mStopContainer = findViewById(R.id.recorder_layout);
        mStopButton = findViewById(R.id.record_overlay_stop);
        mCountDownTextView = (TextView) findViewById(R.id.record_overlay_recording);
        mRecordingTimeTextView = (TextView) findViewById(R.id.recording_time);
        mCancelButton.setOnClickListener(clickListener);
        mStartButton.setOnClickListener(clickListener);
        findViewById(R.id.record_change_quality).setOnClickListener(clickListener);
        int videoSizePercentageProvider = Integer.valueOf(PreferenceManager.getDefaultSharedPreferences(getContext()).getString(SettingsFragment.VIDEO_SIZE_KEY, "100"));
        Log.i(TAG, "initView videoSizePercentageProvider = " + videoSizePercentageProvider);
        switch (videoSizePercentageProvider) {
            case 100:
                mQualityTextView.setText(R.string.float_record_super_hd_quality);
                break;
            case 75:
                mQualityTextView.setText(R.string.float_record_hd_quality);
                break;
            case 50:
                mQualityTextView.setText(R.string.float_record_normal_quality);
                break;
            default:
                mQualityTextView.setText(R.string.float_record_super_hd_quality);
                break;
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        setAlpha(0);
        animate().alpha(1).setDuration(DURATION_ENTER_EXIT).setInterpolator(new DecelerateInterpolator());
        //setTranslationX(animationWidth);
        //animate().translationX(0).setDuration(DURATION_ENTER_EXIT).setInterpolator(new DecelerateInterpolator());

    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    void onCancelClicked() {
        animate().alpha(0).setDuration(DURATION_ENTER_EXIT)
                .setInterpolator(new AccelerateInterpolator()).withEndAction(new Runnable() {
            @Override
            public void run() {
                listener.onCancel();
            }
        });
//		animate().translationX(animationWidth).setDuration(DURATION_ENTER_EXIT)
//				.setInterpolator(new AccelerateInterpolator()).withEndAction(new Runnable() {
//					@Override
//					public void run() {
//						listener.onCancel();
//					}
//				});
    }

    void onStartClicked() {
        if (!showCountDown) {
            mStartContainer.animate().alpha(0).withEndAction(new Runnable() {
                @Override
                public void run() {
                    startRecording();
                }
            });
            return;
        }
        mStartContainer.animate().alpha(0);
        mCountDownTextView.setVisibility(VISIBLE);
        mCountDownTextView.animate().alpha(1).withEndAction(new Runnable() {
            @Override
            public void run() {
                showCountDown();
            }
        });
    }

    private void startRecording() {
        mCountDownTextView.setVisibility(INVISIBLE);
        mStopContainer.setVisibility(View.VISIBLE);
        mStopContainer.animate().alpha(1);
        mStopButton.setVisibility(VISIBLE);
        mStopButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onStop();
            }
        });
        listener.onStart();
        mRecordingStartTime = SystemClock.uptimeMillis();
        updateRecordingTime();
    }

    private void showCountDown() {
        String[] countdown = getResources().getStringArray(R.array.countdown);
        countdown(countdown, 0); // array resource must not be empty
    }

    private void countdownComplete() {
        mCountDownTextView.animate().alpha(0).setDuration(COUNTDOWN_DELAY).withEndAction(new Runnable() {
            @Override
            public void run() {
                startRecording();
            }
        });
    }

    private void countdown(final String[] countdownArr, final int index) {
        mCountDownTextView.setText(countdownArr[index]);
        postDelayed(new Runnable() {
            @Override
            public void run() {
                if (index < countdownArr.length - 1) {
                    countdown(countdownArr, index + 1);
                } else {
                    countdownComplete();
                }
            }
        }, COUNTDOWN_DELAY);
    }

    private void updateRecordingTime() {

        long now = SystemClock.uptimeMillis();
        long delta = now - mRecordingStartTime;

        String text = millisecondToTimeString(delta, false);
        long targetNextUpdateDelay = 1000;

        mRecordingTimeTextView.setText(text);

        long actualNextUpdateDelay = targetNextUpdateDelay
                - (delta % targetNextUpdateDelay);
        postDelayed(new Runnable() {
            @Override
            public void run() {
                updateRecordingTime();
            }
        }, actualNextUpdateDelay);
    }

    interface Listener {
        /**
         * Called when cancel is clicked. This view is unusable once this
         * callback is invoked.
         */
        void onCancel();

        /**
         * Called when start is clicked and it is appropriate to start
         * recording. This view will hide itself completely before invoking this
         * callback.
         */
        void onStart();

        /**
         * Called when stop is clicked. This view is unusable once this callback
         * is invoked.
         */
        void onStop();
    }
}
