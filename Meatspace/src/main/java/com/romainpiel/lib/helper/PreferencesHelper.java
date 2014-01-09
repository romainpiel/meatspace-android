package com.romainpiel.lib.helper;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.romainpiel.meatspace.BuildConfig;
import com.romainpiel.meatspace.R;

/**
 * Meatspace
 * User: romainpiel
 * Date: 12/09/2013
 * Time: 11:45
 *
 * Helper class for storing and retrieving data from the SharedPreferences
 */
public class PreferencesHelper {

    private static final String KEY_PREFS_CAMERA_ID = "camera_id";
    private static final String APP_SHARED_PREFS = PreferencesHelper.class.getSimpleName();

    private SharedPreferences sharedPrefs;
    private SharedPreferences.Editor prefsEditor;

    public PreferencesHelper(Context context) {
        this.sharedPrefs = context.getSharedPreferences(APP_SHARED_PREFS, Activity.MODE_PRIVATE);
        this.prefsEditor = sharedPrefs.edit();
    }

    public int getCameraId(int defaultValue) {
        return sharedPrefs.getInt(KEY_PREFS_CAMERA_ID, defaultValue);
    }

    public void saveCameraId(int cameraId) {
        prefsEditor.putInt(KEY_PREFS_CAMERA_ID, cameraId);
        prefsEditor.apply();
    }

    public static int getAutoKillTimeoutBg(Context context) {
        return Integer.valueOf(getDefaultString(
                context,
                context.getString(R.string.settings_auto_kill_timeout_bg_key),
                context.getResources().getString(R.string.settings_auto_kill_timeout_bg_default)));
    }

    public static boolean areNotificationsEnabled(Context context) {
        return getDefaultBoolean(
                context,
                context.getString(R.string.settings_enable_notifications_key),
                context.getResources().getBoolean(R.bool.settings_enable_notifications_default));
    }

    public static String getBaseUrl(Context context) {
        return getDefaultString(
                context,
                context.getString(R.string.settings_url_key),
                context.getResources().getString(BuildConfig.MEATSPACE_BASE_URL));
    }

    public static String getDefaultString(Context context, String key, String defValue) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(key, defValue);
    }

    public static boolean getDefaultBoolean(Context context, String key, boolean defValue) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(key, defValue);
    }
}
