package com.romainpiel.lib.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.romainpiel.meatspace.R;

/**
 * MeatspaceProject
 * User: romainpiel
 * Date: 14/12/2013
 * Time: 10:50
 */
public class PreferenceHelper {

    public static boolean isRunInBgEnabled(Context context) {
        return getDefaultBoolean(
                context,
                context.getString(R.string.settings_run_in_bg_key),
                context.getResources().getBoolean(R.bool.settings_run_in_bg_default));
    }

    public static boolean getDefaultBoolean(Context context, String key, boolean defValue) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(key, defValue);
    }
}
