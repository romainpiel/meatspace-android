package com.romainpiel.lib.api;

import android.content.Context;
import android.os.Handler;

import com.bugsense.trace.BugSenseHandler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.koushikdutta.async.http.socketio.ConnectCallback;
import com.koushikdutta.async.http.socketio.SocketIOClient;
import com.romainpiel.lib.bus.BusManager;
import com.romainpiel.lib.helper.PreferencesHelper;
import com.romainpiel.lib.utils.ApiKeyGenerator;
import com.romainpiel.meatspace.BuildConfig;
import com.romainpiel.model.ChatRequest;
import com.romainpiel.model.GifMedia;

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

    private ApiManager() {
    }

    public static ApiManager get() {
        if (instance == null) {
            instance = new ApiManager();
        }
        return instance;
    }

    public Gson getJsonParser() {
        if (jsonParser == null) {
            jsonParser = new GsonBuilder()
                    .registerTypeAdapter(GifMedia.class, GifMedia.getDeserializer())
                    .create();
        }
        return jsonParser;
    }

    public void connect(Context context, ConnectCallback callback) {
        SocketIOClient.connect(
                PreferencesHelper.getBaseUrl(context), callback, new Handler());
    }

    public void disconnect(SocketIOClient client) {
        client.disconnect();
    }

    public void emit(Context context, String text, byte[] picture, String fingerprint) {
        String media = GifMedia.mediaFromGIFbytes(picture);

        if (media == null) {
            // malformed GIF
            if (!BuildConfig.DEBUG) {
                BugSenseHandler.sendEvent("null gif, bytes==null "+ (picture == null));
            }
            return;
        }

        ChatRequest chatRequest = new ChatRequest(
                ApiKeyGenerator.getApiKey(),
                text,
                media,
                fingerprint
        );
        BusManager.get().getChatBus().post(chatRequest);
    }
}
