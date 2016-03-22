package com.way.screenshot;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.way.captain.R;
import com.way.captain.fragment.SettingsFragment;
import com.way.captain.service.ChatHeadService;

import java.nio.ByteBuffer;

public class TakeScreenshotService extends Service {
    private static final String TAG = "TakeScreenshotService";
    private static final String DISPLAY_NAME = "Screenshot";
    private static final String EXTRA_RESULT_CODE = "result-code";
    private static final String EXTRA_DATA = "data";
    private static GlobalScreenshot mScreenshot;
    final Object mScreenshotLock = new Object();
    final Runnable mScreenshotTimeout = new Runnable() {
        @Override
        public void run() {
            synchronized (mScreenshotLock) {
                Toast.makeText(TakeScreenshotService.this, R.string.screenshot_failed_title, Toast.LENGTH_SHORT).show();
                stopSelf();
            }
        }
    };
    private boolean mIsRunning;
    private Bitmap mScreenShotBitmap;
    private int mScreenWidth;
    private int mScreenHeight;
    private MediaProjection mProjection;
    private MediaProjectionManager mProjectionManager;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private ImageReader mImageReader;
    Runnable getScreenshotBitmapTask = new Runnable() {

        @Override
        public void run() {
            getImage();
        }
    };
    private ImageReader.OnImageAvailableListener mListener = new ImageReader.OnImageAvailableListener() {

        @Override
        public void onImageAvailable(ImageReader reader) {
            mHandler.removeCallbacks(mScreenshotTimeout);
            mImageReader.setOnImageAvailableListener(null, null);// 移除监听
            mHandler.removeCallbacks(getScreenshotBitmapTask);
            mHandler.post(getScreenshotBitmapTask);
        }
    };

    public static Intent newIntent(Context context, int resultCode, Intent data) {
        Intent intent = new Intent(context, TakeScreenshotService.class);
        intent.putExtra(EXTRA_RESULT_CODE, resultCode);
        intent.putExtra(EXTRA_DATA, data);
        return intent;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // return new Messenger(mHandler).getBinder();
        throw new AssertionError("Not supported.");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, TAG + " onCreate...");
        // Dismiss the notification that brought us here.
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(GlobalScreenshot.SCREENSHOT_NOTIFICATION_ID);
        mScreenshot = new GlobalScreenshot(TakeScreenshotService.this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, TAG + " onDestroy...");
        if (mScreenShotBitmap != null && !mScreenShotBitmap.isRecycled())
            mScreenShotBitmap.recycle();
        mScreenShotBitmap = null;
        if (mImageReader != null)
            mImageReader.close();
        if (mProjection != null)
            mProjection.stop();
        PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(SettingsFragment.HIDE_FLOATVIEW_KEY, false).apply();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mIsRunning) {
            Log.d(TAG, "Already running! Ignoring...");
            Toast.makeText(this, R.string.screenshot_saving_title, Toast.LENGTH_SHORT).show();
            return START_NOT_STICKY;
        }
        Log.d(TAG, "Starting up!");
        mIsRunning = true;

        final int resultCode = intent.getIntExtra(EXTRA_RESULT_CODE, 0);
        final Intent data = intent.getParcelableExtra(EXTRA_DATA);
        if (resultCode == 0 || data == null) {
            throw new IllegalStateException("Result code or data missing.");
        }
        mHandler.postDelayed(new Runnable() {

            @Override
            public void run() {
                startCapture(resultCode, data);
            }
        }, 200L);
        return START_NOT_STICKY;
    }

    private void startCapture(int resultCode, Intent data) {
        mProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        mProjection = mProjectionManager.getMediaProjection(resultCode, data);
        WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getRealMetrics(displayMetrics);
        mScreenWidth = displayMetrics.widthPixels;
        mScreenHeight = displayMetrics.heightPixels;
        mImageReader = ImageReader.newInstance(mScreenWidth, mScreenHeight, PixelFormat.RGBA_8888, 1);// 只获取一张图片
        mProjection.createVirtualDisplay(DISPLAY_NAME, mScreenWidth, mScreenHeight, displayMetrics.densityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_PRESENTATION, mImageReader.getSurface(), null, null);
        mImageReader.setOnImageAvailableListener(mListener, null);
        mHandler.postDelayed(mScreenshotTimeout, 5000L);// 设置截屏超时时间
    }

    private Bitmap addBorder(Bitmap bitmap, int width) {
        Bitmap newBitmap = Bitmap.createBitmap(width + bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
        Canvas canvas = new Canvas(newBitmap);
        canvas.drawColor(Color.TRANSPARENT);
        canvas.drawBitmap(bitmap, 0.0F, 0.0F, null);
        return newBitmap;
    }

    private void getImage() {
        Image image = mImageReader.acquireLatestImage();
        if (image == null) {
            throw new NullPointerException("image is null...");
        }
        int imageHeight = image.getHeight();
        Image.Plane[] planes = image.getPlanes();
        ByteBuffer byteBuffer = planes[0].getBuffer();
        int pixelStride = planes[0].getPixelStride();
        int rowStride = planes[0].getRowStride() - pixelStride * mScreenWidth;
        mScreenShotBitmap = Bitmap.createBitmap(mScreenWidth + rowStride / pixelStride, imageHeight,
                Bitmap.Config.ARGB_8888);
        mScreenShotBitmap.copyPixelsFromBuffer(byteBuffer);
        if (rowStride != 0) {
            mScreenShotBitmap = addBorder(mScreenShotBitmap, -(rowStride / pixelStride));
        }
        image.close();
        if (mImageReader != null)
            mImageReader.close();
        if (mProjection != null)
            mProjection.stop();
        takeScreenshot();
    }

    private void takeScreenshot() {
        mScreenshot.takeScreenshot(mScreenShotBitmap, new Runnable() {
            @Override
            public void run() {
                stopSelf();
            }
        }, false);
    }

}
