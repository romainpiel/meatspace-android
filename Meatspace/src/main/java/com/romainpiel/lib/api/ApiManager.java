package com.romainpiel.lib.api;

import com.romainpiel.lib.api.meatspace.MeatspaceClient;
import com.romainpiel.meatspace.BuildConfig;

import retrofit.RestAdapter;

/**
 * Meatspace
 * User: romainpiel
 * Date: 01/11/2013
 * Time: 17:05
 */
public class ApiManager {

    private static ApiManager instance;

    private MeatspaceClient meatspaceClient;

    private ApiManager() {}

    public static ApiManager get() {
        if (instance == null) {
            instance = new ApiManager();
        }
        return instance;
    }

    public MeatspaceClient meatspace() {
        if (meatspaceClient == null) {

            RestAdapter restAdapter = new RestAdapter.Builder()
                    .setLogLevel(RestAdapter.LogLevel.BASIC)
                    .setServer(BuildConfig.MEATSPACE_BASE_URL)
                    .build();

            meatspaceClient = restAdapter.create(MeatspaceClient.class);
        }
        return meatspaceClient;
    }
}
