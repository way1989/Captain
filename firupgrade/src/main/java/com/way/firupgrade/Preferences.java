package com.way.firupgrade;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * @author way 2014/11/3
 */
public class Preferences {
    public static final String PREFERENCES_NAME = "com_upgrade_manager";
    public static final String KEY_DOWNLOAD_ID = "downloadId";
    protected static final String KEY_VERSION_CODE = "version_code";
    protected static final String KEY_VERSION_NAME = "version_name";
    protected static final String KEY_DOWNLOAD_PATH = "download_path";
    protected static final String KEY_DOWNLOAD_STATUS = "download_status";

    public static void setDownloadId(Context context, long downloadId) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_APPEND);
        SharedPreferences.Editor editor = pref.edit();
        editor.putLong(KEY_DOWNLOAD_ID, downloadId);
        editor.commit();
    }

    public static long getDownloadId(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_APPEND);
        return pref.getLong(KEY_DOWNLOAD_ID, -1);
    }

    public static void setDownloadPath(Context context, String downloadPath) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_APPEND);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(KEY_DOWNLOAD_PATH, downloadPath);
        editor.commit();
    }

    public static String getDownloadPath(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_APPEND);
        return pref.getString(KEY_DOWNLOAD_PATH, "");
    }

    public static void removeAll(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_APPEND);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.commit();
    }

    public static void setDownloadStatus(Context context, int downloadId) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_APPEND);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(KEY_DOWNLOAD_STATUS, downloadId);
        editor.commit();
    }

    public static int getDownloadStatus(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_APPEND);
        return pref.getInt(KEY_DOWNLOAD_STATUS, -1);
    }

    public static void setVersionCode(Context context, int versionCode) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_APPEND);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(KEY_VERSION_CODE, versionCode);
        editor.commit();
    }

    public static int getVersionCode(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_APPEND);
        return pref.getInt(KEY_VERSION_CODE, -1);
    }

    public static void setVersionName(Context context, String versionName) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_APPEND);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(KEY_VERSION_NAME, versionName);
        editor.commit();
    }

    public static String getVersionName(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_APPEND);
        return pref.getString(KEY_VERSION_NAME, "");
    }
}
