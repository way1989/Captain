package com.way.captain.utils;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import com.way.captain.App;
import com.way.captain.activity.PermissionCheckActivity;

import java.util.ArrayList;
import java.util.Hashtable;

/**
 * Created by way on 16/2/28.
 */
public class OsUtil {
    private static boolean sIsAtLeastM;
    private static Hashtable<String, Integer> sPermissions = new Hashtable<String, Integer>();
    private static String[] sRequiredPermissions = new String[]{
            // Required to read existing SMS threads
            //Manifest.permission.READ_SMS,
            // Required for knowing the phone number, number of SIMs, etc.
//            Manifest.permission.READ_PHONE_STATE,
            // This is not strictly required, but simplifies the contact picker scenarios
//            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    static {
        sIsAtLeastM = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M;
    }

    /**
     * @return True if the version of Android that we're running on is at least M
     * (API level 23).
     */
    public static boolean isAtLeastM() {
        return sIsAtLeastM;
    }

    /**
     * Does the app have all the specified permissions
     */
    public static boolean hasPermissions(final String[] permissions) {
        for (final String permission : permissions) {
            if (!hasPermission(permission)) {
                return false;
            }
        }
        return true;
    }

    @TargetApi(Build.VERSION_CODES.M)
    public static boolean hasPermission(final String permission) {
        if (OsUtil.isAtLeastM()) {
            // It is safe to cache the PERMISSION_GRANTED result as the process gets killed if the
            // user revokes the permission setting. However, PERMISSION_DENIED should not be
            // cached as the process does not get killed if the user enables the permission setting.
            if (!sPermissions.containsKey(permission)
                    || sPermissions.get(permission) == PackageManager.PERMISSION_DENIED) {
                final Context context = App.getContext();
                final int permissionState = context.checkSelfPermission(permission);
                sPermissions.put(permission, permissionState);
            }
            return sPermissions.get(permission) == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }

    public static String[] getMissingPermissions(final String[] permissions) {
        final ArrayList<String> missingList = new ArrayList<String>();
        for (final String permission : permissions) {
            if (!hasPermission(permission)) {
                missingList.add(permission);
            }
        }

        final String[] missingArray = new String[missingList.size()];
        missingList.toArray(missingArray);
        return missingArray;
    }

    /**
     * Does the app have the minimum set of permissions required to operate.
     */
    public static boolean hasRequiredPermissions() {
        return hasPermissions(sRequiredPermissions);
    }

    public static String[] getMissingRequiredPermissions() {
        return getMissingPermissions(sRequiredPermissions);
    }

    public static boolean redirectToPermissionCheckIfNeeded(final Activity activity) {
        if (!OsUtil.hasRequiredPermissions()) {
            final Intent intent = new Intent(activity, PermissionCheckActivity.class);
            activity.startActivity(intent);
        } else {
            // No redirect performed
            return false;
        }

        // Redirect performed
        activity.finish();
        return true;
    }
}
