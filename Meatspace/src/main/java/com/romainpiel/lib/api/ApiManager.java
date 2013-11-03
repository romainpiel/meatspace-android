package com.romainpiel.lib.api;

import android.os.Handler;

import com.google.gson.Gson;
import com.koushikdutta.async.http.socketio.ConnectCallback;
import com.koushikdutta.async.http.socketio.SocketIOClient;
import com.romainpiel.meatspace.BuildConfig;

import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;

/**
 * Meatspace
 * User: romainpiel
 * Date: 01/11/2013
 * Time: 17:05
 */
public class ApiManager {

    public static final String EVENT_MESSAGE = "message";

    private static ApiManager instance;

    private MeatspaceRestClient meatspaceRestClient;
    private Gson jsonParser;

    private ApiManager() {}

    public static ApiManager get() {
        if (instance == null) {
            instance = new ApiManager();
        }
        return instance;
    }

    public MeatspaceRestClient meatspace() {
        if (meatspaceRestClient == null) {

            jsonParser = getJsonParser();

            RestAdapter restAdapter = new RestAdapter.Builder()
                    .setLogLevel(RestAdapter.LogLevel.BASIC)
                    .setConverter(new GsonConverter(jsonParser))
                    .setServer(BuildConfig.MEATSPACE_BASE_URL)
                    .build();

            meatspaceRestClient = restAdapter.create(MeatspaceRestClient.class);
        }
        return meatspaceRestClient;
    }

    public Gson getJsonParser() {
        if (jsonParser == null) {
            jsonParser = new Gson();
        }
        return jsonParser;
    }

    public void connect(ConnectCallback callback) {
        SocketIOClient.connect(BuildConfig.MEATSPACE_BASE_URL, callback, new Handler());
    }

    public void disconnect(SocketIOClient client) {
        client.disconnect();
    }
}
