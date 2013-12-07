package com.romainpiel.meatspace;

import android.app.Application;

import com.google.analytics.tracking.android.GoogleAnalytics;

/**
 * MeatspaceProject
 * User: romainpiel
 * Date: 02/12/2013
 * Time: 19:42
 */
public class MeatspaceApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        GoogleAnalytics.getInstance(this).setDryRun(BuildConfig.DEBUG);
    }
}
