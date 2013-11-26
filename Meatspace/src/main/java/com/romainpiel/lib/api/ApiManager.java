package com.romainpiel.lib.api;

import android.content.Context;
import android.os.Handler;

import com.google.gson.Gson;
import com.koushikdutta.async.http.socketio.ConnectCallback;
import com.koushikdutta.async.http.socketio.SocketIOClient;
import com.romainpiel.lib.bus.BusManager;
import com.romainpiel.lib.gif.GIFUtils;
import com.romainpiel.meatspace.BuildConfig;
import com.romainpiel.model.ChatRequest;

/**
 * Meatspace
 * User: romainpiel
 * Date: 01/11/2013
 * Time: 17:05
 */
public class ApiManager {

    public static final String EVENT_MESSAGE = "message";

    private static ApiManager instance;

    private Gson jsonParser;

    private ApiManager() {}

    public static ApiManager get() {
        if (instance == null) {
            instance = new ApiManager();
        }
        return instance;
    }

    public Gson getJsonParser() {
        if (jsonParser == null) {
            jsonParser = new Gson();
        }
        return jsonParser;
    }

    public void connect(Context context, ConnectCallback callback) {
        SocketIOClient.connect(context.getString(BuildConfig.MEATSPACE_BASE_URL), callback, new Handler());
    }

    public void disconnect(SocketIOClient client) {
        client.disconnect();
    }

    public void emit(Context context, String text, byte[] picture, String fingerprint) {
        ChatRequest chatRequest = new ChatRequest(
                context.getString(BuildConfig.MEATSPACE_KEY),
                text,
                GIFUtils.mediaFromGIFbytes(picture),
                fingerprint
        );
        BusManager.get().getChatBus().post(chatRequest);
    }
}
