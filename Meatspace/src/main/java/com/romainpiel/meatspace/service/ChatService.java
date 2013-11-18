package com.romainpiel.meatspace.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.google.gson.Gson;
import com.koushikdutta.async.http.socketio.Acknowledge;
import com.koushikdutta.async.http.socketio.ConnectCallback;
import com.koushikdutta.async.http.socketio.EventCallback;
import com.koushikdutta.async.http.socketio.SocketIOClient;
import com.romainpiel.lib.api.ApiManager;
import com.romainpiel.lib.api.IOState;
import com.romainpiel.lib.bus.BusManager;
import com.romainpiel.lib.bus.ChatEvent;
import com.romainpiel.lib.utils.BackgroundExecutor;
import com.romainpiel.lib.utils.Debug;
import com.romainpiel.meatspace.R;
import com.romainpiel.meatspace.activity.MainActivity;
import com.romainpiel.model.Chat;
import com.romainpiel.model.ChatList;
import com.romainpiel.model.ChatRequest;
import com.squareup.otto.Produce;
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
    private static final int CHAT_NOTIF_ID = 1234;

    private ApiManager apiManager;
    private BusManager busManager;
    private SocketIOClient socketIOClient;
    private Handler handler;
    private ChatList chatList;
    private IOState ioState;

    @Override
    public void onCreate() {
        super.onCreate();

        apiManager = ApiManager.get();
        busManager = BusManager.get();
        handler = new Handler();

        ioState = IOState.DISCONNECTED;
        chatList = new ChatList();

        busManager.getChatBus().register(this);
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

        if (ioState.equals(IOState.DISCONNECTED)) {
            ioState = IOState.CONNECTING;

            // TODO avoid this and handle duplicates inside this class
            chatList.clear();

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
                                post(result);
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
            postError();
            return;
        }

        ioState = IOState.CONNECTED;

        socketIOClient = client;
        socketIOClient.addListener(ApiManager.EVENT_MESSAGE, this);

        PendingIntent pi =
                PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        builder.setContentIntent(pi)
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .setContentTitle(getString(R.string.service_chat_running))
                .setContentText(getString(R.string.service_chat_running_description));

        Notification note = builder.build();

        note.flags |= Notification.FLAG_NO_CLEAR;

        startForeground(CHAT_NOTIF_ID, note);

        post();
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

                            // TODO try to use strings from the very beginning...
                            chat = jsonParser.fromJson(jsonChat.toString(), Chat.class);

                            result.add(chat);
                        }

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                post(new ChatList(result));
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
        if (socketIOClient != null && socketIOClient.isConnected()) {
            Gson jsonParser = apiManager.getJsonParser();

            // 4 : json type (? not sure why)
            socketIOClient.emitRaw(4, jsonParser.toJson(chatRequest), null);
        } else {
            postError();
        }
    }

    public void postError() {
        ioState = IOState.ERROR;
        post();
    }

    public void post(ChatList items) {
        this.chatList.addAll(items.get());
        post();
    }

    public void post() {
        Debug.out(ioState);
        Debug.out(Debug.getCallingMethodInfo());
        this.busManager.getChatBus().post(new ChatEvent(ioState, chatList));
    }

    @Produce
    public ChatEvent produce() {
        return new ChatEvent(ioState, chatList);
    }
}
