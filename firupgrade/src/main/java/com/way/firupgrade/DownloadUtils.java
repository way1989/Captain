package com.way.firupgrade;

import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;


public class DownloadUtils {
    public static final String APK_SUFFIX = ".apk";
    public static final Integer DOWNLOAD_STATUS_RUNNING = 1;
    private static final long MAX_ALLOWED_DOWNLOAD_BYTES_BY_MOBILE = 3 * 1024 * 1024;//3M

    public static void DownloadApkWithProgress(Context context, final AppVersion appVersion) {
        if (context == null)
            return;
        if (checkDownloadRunning(context, appVersion))
            return;
        if (checkApkExist(context, appVersion)) {
            Intent installApkIntent = new Intent();
            installApkIntent.setAction(Intent.ACTION_VIEW);
            installApkIntent.setDataAndType(Uri.parse(Preferences.getDownloadPath(context)),
                    "application/vnd.android.package-archive");
            installApkIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            context.startActivity(installApkIntent);
        } else {
            String apkName = context.getPackageName() + System.currentTimeMillis() + APK_SUFFIX;
            // 系统下载程序
            final DownloadManager downloadManager = (DownloadManager) context
                    .getSystemService(Context.DOWNLOAD_SERVICE);

            boolean allowMobileDownload = false;
            Long recommendedMaxBytes = DownloadManager.getRecommendedMaxBytesOverMobile(context);
            // 可以在移动网络下下载
            if (recommendedMaxBytes == null || recommendedMaxBytes.longValue() > MAX_ALLOWED_DOWNLOAD_BYTES_BY_MOBILE) {
                allowMobileDownload = true;
            }

            Uri uri = Uri.parse(appVersion.getUpdateUrl());

            final Request request = new Request(uri);

            request.setNotificationVisibility(Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setNotificationVisibility(Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

            int NETWORK_TYPE = Request.NETWORK_WIFI;
            if (allowMobileDownload) {
                NETWORK_TYPE |= Request.NETWORK_MOBILE;
            }
            request.setAllowedNetworkTypes(NETWORK_TYPE);
            request.allowScanningByMediaScanner();
            // request.setShowRunningNotification(true);
            request.setNotificationVisibility(Request.VISIBILITY_VISIBLE);
            request.setVisibleInDownloadsUi(true);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, apkName);
            String appName = FIRUtils.getAppName(context);
            Log.i("liweiping", "appName = " + appName);
            request.setTitle(appName);
            request.setMimeType("application/vnd.android.package-archive");

            // id 保存起来跟之后的广播接收器作对比
            long id = downloadManager.enqueue(request);

            long oldId = Preferences.getDownloadId(context);
            if (oldId != -1) {
                downloadManager.remove(oldId);
            }

            Preferences.removeAll(context);
            Preferences.setDownloadId(context, id);
            Preferences.setVersionCode(context, appVersion.getVersionCode());
            Preferences.setVersionName(context, appVersion.getVersionName());
            Preferences.setDownloadStatus(context, DOWNLOAD_STATUS_RUNNING);
        }
    }

    private static boolean checkApkExist(Context context, AppVersion appVersion) {
        String versionName = Preferences.getVersionName(context);
        int versionCode = Preferences.getVersionCode(context);
        String downloadPath = Preferences.getDownloadPath(context);

        int appVersionCode = appVersion.getVersionCode();
        String appVersionName = appVersion.getVersionName();

        MyLog.i("way", "versionName = " + versionName + ", versionCode = " + versionCode + ", downloadPath = "
                + downloadPath + ", appVersionCode = " + appVersionCode + ", appVersionName = " + appVersionName);
        if (versionCode < 0 || appVersionCode < 0 || appVersionCode != versionCode
                || !TextUtils.equals(appVersionName, versionName) || TextUtils.isEmpty(downloadPath))
            return false;
        String path = Uri.parse(downloadPath).getPath();
        if (!TextUtils.isEmpty(path) && path.endsWith(APK_SUFFIX)) {
            File file = new File(path);
            if (file.exists()) {
                return true;
            }
        }

        return false;
    }

    private static boolean checkDownloadRunning(Context context, AppVersion appVersion) {
        String versionName = Preferences.getVersionName(context);
        int versionCode = Preferences.getVersionCode(context);
        int downloadStatus = Preferences.getDownloadStatus(context);
        long downloadId = Preferences.getDownloadId(context);

        int appVersionCode = appVersion.getVersionCode();
        String appVersionName = appVersion.getVersionName();
        MyLog.i("way", "versionName = " + versionName + ", versionCode = " + versionCode + ", downloadStatus = "
                + downloadStatus + ", appVersionCode = " + appVersionCode + ", appVersionName = " + appVersionName);
        if (versionCode < 0 || appVersionCode < 0 || appVersionCode != versionCode
                || !TextUtils.equals(appVersionName, versionName) || downloadId < 0)
            return false;
        final DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Query mDownloadQuery = new DownloadManager.Query();
        mDownloadQuery.setFilterById(downloadId);
        Cursor cursor = downloadManager.query(mDownloadQuery);
        if (cursor != null && cursor.moveToFirst()) {
            int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
            if (status == DownloadManager.STATUS_RUNNING || downloadStatus == DOWNLOAD_STATUS_RUNNING) {
                return true;
            }
        }

        return false;
    }
}
