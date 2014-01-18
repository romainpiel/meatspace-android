package com.romainpiel.meatspace;

import android.app.Application;

import com.google.analytics.tracking.android.GoogleAnalytics;
import com.romainpiel.lib.helper.PreferencesHelper;

/**
 * MeatspaceProject
 * User: romainpiel
 * Date: 02/12/2013
 * Time: 19:42
 */
public class MeatspaceApplication extends Application {

    private PreferencesHelper preferencesHelper;

    @Override
    public void onCreate() {
        super.onCreate();

        preferencesHelper = new PreferencesHelper(this);

        // disable google analytics if in debug mode
        GoogleAnalytics.getInstance(this).setDryRun(BuildConfig.DEBUG);

        checkUpdate();
    }

    private void checkUpdate() {
        int lastVersion = preferencesHelper.getLastAppVersion(0);
        if (lastVersion < BuildConfig.VERSION_CODE) {

            // some base url were changed between 17 and 18
            if (lastVersion < 18) {
                PreferencesHelper.cleanDefault(this, R.string.settings_url_key);
            }

            preferencesHelper.saveLastAppVersion(BuildConfig.VERSION_CODE);
        }
    }
}
