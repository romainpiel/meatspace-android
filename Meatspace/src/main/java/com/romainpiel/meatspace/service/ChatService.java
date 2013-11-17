package com.romainpiel.meatspace.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

import com.google.gson.Gson;
import com.koushikdutta.async.http.socketio.Acknowledge;
import com.koushikdutta.async.http.socketio.ConnectCallback;
import com.koushikdutta.async.http.socketio.EventCallback;
import com.koushikdutta.async.http.socketio.SocketIOClient;
import com.romainpiel.lib.bus.BusManager;
import com.romainpiel.lib.api.ApiManager;
import com.romainpiel.lib.utils.BackgroundExecutor;
import com.romainpiel.lib.utils.Debug;
import com.romainpiel.model.Chat;
import com.romainpiel.model.ChatList;
import com.romainpiel.model.ChatRequest;
import com.squareup.otto.Subscribe;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Meatspace
 * User: romainpiel
 * Date: 03/11/2013
 * Time: 17:47
 */
public class ChatService extends Service implements ConnectCallback, EventCallback {

    private static final String API_GET_CHAT_REQ_ID = "ChatService.GET_CHAT";

    private ApiManager apiManager;
    private BusManager busManager;
    private SocketIOClient socketIOClient;
    private Handler handler;
    private boolean initialized;

    @Override
    public void onCreate() {
        super.onCreate();
        apiManager = ApiManager.get();
        busManager = BusManager.get();
        busManager.getChatBus().register(this);
        handler = new Handler();
    }

    @Override
    public void onDestroy() {
        if (socketIOClient != null) {
            apiManager.disconnect(socketIOClient);
        }
        busManager.getChatBus().unregister(this);
        BackgroundExecutor.cancelAll(API_GET_CHAT_REQ_ID, true);
        super.onDestroy();
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {

        if (!initialized) {
            initialized = true;
            fetchChat();
            apiManager.connect(this, this);
        }

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void fetchChat() {
        BackgroundExecutor.execute(
                new Runnable() {
                    @Override
                    public void run() {
                        final ChatList result = ApiManager.get().meatspace(ChatService.this).getChats();
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                busManager.getChatBus().post(result);
                            }
                        });
                    }
                },
                API_GET_CHAT_REQ_ID,
                null
        );
    }

    @Override
    public void onConnectCompleted(Exception ex, SocketIOClient client) {

        if (ex != null) {
            return;
        }

        socketIOClient = client;
        socketIOClient.addListener(ApiManager.EVENT_MESSAGE, this);

    }

    @Override
    public void onEvent(String event, final JSONArray argument, Acknowledge acknowledge) {
        if (event.equals(ApiManager.EVENT_MESSAGE)) {

            final Gson jsonParser = apiManager.getJsonParser();

            BackgroundExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    try {

                        JSONObject object;
                        JSONObject jsonChat;
                        Chat chat;
                        final List<Chat> result = new ArrayList<Chat>();

                        for (int i = 0; i < argument.length(); i++) {
                            object = argument.getJSONObject(i);
                            jsonChat = (JSONObject) object.get("chat");

                            // TODO try to use strings form the very beginning...
                            chat = jsonParser.fromJson(jsonChat.toString(), Chat.class);

                            result.add(chat);
                        }

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                busManager.getChatBus().post(new ChatList(result));
                            }
                        });

                    } catch (JSONException e) {
                        Debug.out(e);
                    }
                }
            });
        }
    }

    @Subscribe
    public void onEvent(ChatRequest chatRequest) {
        if (socketIOClient != null) {
            Gson jsonParser = apiManager.getJsonParser();

            // 4 : json type (? not sure why)
            socketIOClient.emitRaw(4, jsonParser.toJson(chatRequest), null);
        }
    }
}
