package com.romainpiel.lib.ui.helper;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

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
}
