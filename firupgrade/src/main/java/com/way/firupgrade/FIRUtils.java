package com.way.firupgrade;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import im.fir.sdk.FIR;
import im.fir.sdk.VersionCheckCallback;

public class FIRUtils {
    private static Map<String, LocaleHandler> mLocaleHandlers;

    public final static void checkForUpdate(final Activity context, final boolean isShowToast) {
        if (context == null)
            return;
        String api_token = context.getResources().getString(R.string.api_token);
        if (TextUtils.isEmpty(api_token))
            throw new NullPointerException("api_token must not null");
        if (mLocaleHandlers == null)
            createHandlers();

        final ProgressDialog dialog = new ProgressDialog(context);
        dialog.setIndeterminate(true);
        dialog.setCancelable(true);
        dialog.setTitle(getProgressDialogTitle());
        dialog.setMessage(getProgressDialogMessage());

        FIR.checkForUpdateInFIR(api_token, new VersionCheckCallback() {

            @Override
            public void onSuccess(String versionJson) {
                Log.i("fir", "check from fir.im success! " + "\n" + versionJson);
                final AppVersion appVersion = getAppVersion(versionJson);
                if (appVersion == null) {
                    Toast.makeText(context, getToastMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }
                int appVersionCode = getVersionCode(context);
                String appVersionName = getVersionName(context);
                Log.i("fir", "check from fir.im success! appVersionCode = "  + appVersionCode + ", appVersionName = " + appVersionName);
                if (appVersionCode != appVersion.getVersionCode() && !TextUtils.equals(appVersionName, appVersion.getVersionName())) {
                    new AlertDialog.Builder(context).setTitle(getDialogTitle()).setMessage(appVersion.getChangeLog())
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    // when download complete, broadcast will be sent to
                                    // receiver
                                    DownloadUtils.DownloadApkWithProgress(context, appVersion);
                                }
                            }).setNegativeButton(android.R.string.cancel, null).create().show();
                } else {
                    if (isShowToast)
                        Toast.makeText(context, getToastMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onStart() {
                Log.i("fir", "check from fir.im onStart! ");
                if (isShowToast && dialog != null && !dialog.isShowing())
                    dialog.show();
            }

            @Override
            public void onFinish() {
                Log.i("fir", "check from fir.im onFinish! ");

                if (dialog != null && dialog.isShowing()) {
                    dialog.cancel();
                }
            }

            @Override
            public void onFail(Exception exception) {
                Log.i("fir", "check from fir.im onFail! exception = " + exception);

                if (isShowToast)
                    Toast.makeText(context, getToastNetErrorMessage(), Toast.LENGTH_SHORT).show();
            }

        });
    }

    private static AppVersion getAppVersion(String versionJson) {
        if (TextUtils.isEmpty(versionJson))
            return null;
        try {
            JSONObject jsonObject = new JSONObject(versionJson);
            String versionName = jsonObject.getString("versionShort");
            int versionCode = jsonObject.getInt("version");
            String changeLog = jsonObject.getString("changelog");
            String updateUrl = jsonObject.getString("install_url");
            long fileSize = jsonObject.getJSONObject("binary").getInt("fsize");
            long updateTime = jsonObject.getLong("updated_at");
            AppVersion appVersion = new AppVersion(versionCode, versionName, changeLog, updateUrl, fileSize, updateTime);
            Log.i("fir", "check from fir.im getAppVersion!  appVersion = " + appVersion);
            return appVersion;
        } catch (JSONException e) {
            Log.i("fir", "check from fir.im getAppVersion!  e = " + e);

            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取版本号
     *
     * @param context
     * @return
     */
    public static int getVersionCode(Context context) {
        int versionCode = -1;
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            versionCode = info.versionCode;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionCode;
    }

    /**
     * 获取版本信息
     *
     * @param context
     * @return
     */
    public static String getVersionName(Context context) {
        String versionName = null;
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            versionName = info.versionName;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionName;
    }

    /**
     * 获取版本信息
     *
     * @param context
     * @return
     */
    public static String getAppName(Context context) {
        String appName = null;
        try {
            PackageManager packageManager = context.getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), 0);
            appName = packageManager.getApplicationLabel(applicationInfo).toString();
            Log.i("liweiping", "appName = " + appName);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return appName;
    }

    private static void createHandlers() {
        mLocaleHandlers = new HashMap<String, LocaleHandler>();
        mLocaleHandlers.put(LocaleChinese.defaultLocale, new LocaleChinese());
        mLocaleHandlers.put(LocaleChinaTW.defaultLocale, new LocaleChinaTW());
        mLocaleHandlers.put(LocaleEnglish.defaultLocale, new LocaleEnglish());
        mLocaleHandlers.put(Locale.CHINA.toString(), new LocaleChina());
        mLocaleHandlers.put(Locale.US.toString(), new LocaleUS());
    }

    private static LocaleHandler lookupHandlerBy(String handlerName) {
        LocaleHandler handler = mLocaleHandlers.get(handlerName);
        if (handler == null)
            return mLocaleHandlers.get(Locale.ENGLISH.getLanguage());
        return mLocaleHandlers.get(handlerName);
    }

    private static String getLocaleLanguage() {
        return Locale.getDefault().toString();
    }

    public static String getDialogTitle() {
        LocaleHandler handler = lookupHandlerBy(getLocaleLanguage());
        return handler.getDialogTitle();
    }

    public static String getProgressDialogTitle() {
        LocaleHandler handler = lookupHandlerBy(getLocaleLanguage());
        return handler.getProgressDialogTitle();
    }

    public static String getProgressDialogMessage() {
        LocaleHandler handler = lookupHandlerBy(getLocaleLanguage());
        return handler.getProgressDialogMessage();
    }

    public static String getToastMessage() {
        LocaleHandler handler = lookupHandlerBy(getLocaleLanguage());
        return handler.getToastMessage();
    }

    public static String getToastNetErrorMessage() {
        LocaleHandler handler = lookupHandlerBy(getLocaleLanguage());
        return handler.getToastNetErrorMessage();
    }
}
