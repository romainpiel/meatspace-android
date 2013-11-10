package com.romainpiel.model;

import android.content.Context;
import android.content.SharedPreferences;

import com.romainpiel.Constants;

import java.util.UUID;

/**
 * MeatspaceProject
 * User: romainpiel
 * Date: 10/11/2013
 * Time: 18:15
 */
public class Device {

    private Context context;
    private String id;

    public Device(Context context) {
        this.context = context;
    }

    public synchronized String getId() {
        if (id == null) {
            SharedPreferences sharedPrefs = context.getSharedPreferences(
                    Constants.PREF_KEY_DEVICE_ID, Context.MODE_PRIVATE);
            id = sharedPrefs.getString(Constants.PREF_KEY_DEVICE_ID, null);
            if (id == null) {
                id = UUID.randomUUID().toString();
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putString(Constants.PREF_KEY_DEVICE_ID, id);
                editor.apply();
            }
        }
        return id;
    }
}
