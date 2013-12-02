package com.romainpiel.meatspace.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import com.google.gson.Gson;
import com.koushikdutta.async.http.socketio.Acknowledge;
import com.koushikdutta.async.http.socketio.ConnectCallback;
import com.koushikdutta.async.http.socketio.EventCallback;
import com.koushikdutta.async.http.socketio.SocketIOClient;
import com.romainpiel.Constants;
import com.romainpiel.lib.api.ApiManager;
import com.romainpiel.lib.api.IOState;
import com.romainpiel.lib.bus.BusManager;
import com.romainpiel.lib.bus.ChatEvent;
import com.romainpiel.lib.bus.MuteEvent;
import com.romainpiel.lib.utils.BackgroundExecutor;
import com.romainpiel.lib.utils.Debug;
import com.romainpiel.meatspace.R;
import com.romainpiel.meatspace.activity.MainActivity;
import com.romainpiel.model.Chat;
import com.romainpiel.model.ChatList;
import com.romainpiel.model.ChatRequest;
import com.romainpiel.model.Device;
import com.squareup.otto.Produce;
import com.squareup.otto.Subscribe;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    private BroadcastReceiver closeChatReceiver;
    private SocketIOClient socketIOClient;
    private Handler handler;
    private ChatList chatList;
    private Set<String> mutedUsers;
    private IOState ioState;

    public static void start(Context context) {
        context.startService(new Intent(context, ChatService.class));
    }

    @Override
    public void onCreate() {
        super.onCreate();

        apiManager = ApiManager.get();
        busManager = BusManager.get();
        handler = new Handler();

        ioState = IOState.IDLE;
        chatList = new ChatList();
        mutedUsers = new HashSet<String>();

        busManager.getChatBus().register(this);

        closeChatReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                ioState = IOState.DISCONNECTED;
                post();
                context.stopService(new Intent(context, ChatService.class));
            }
        };
        registerReceiver(closeChatReceiver, new IntentFilter(Constants.FILTER_CHAT_CLOSE));
    }

    @Override
    public void onDestroy() {
        if (socketIOClient != null) {
            apiManager.disconnect(socketIOClient);
        }
        busManager.getChatBus().unregister(this);
        unregisterReceiver(closeChatReceiver);
        BackgroundExecutor.cancelAll(API_GET_CHAT_REQ_ID, true);
        super.onDestroy();
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {

        if (!ioState.equals(IOState.CONNECTING) && !ioState.equals(IOState.CONNECTED)) {
            ioState = IOState.CONNECTING;

            // TODO avoid this and handle duplicates inside this class
            chatList.clear();

            apiManager.connect(this, this);
        }

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onConnectCompleted(final Exception ex, final SocketIOClient client) {

        handler.post(new Runnable() {
            @Override
            public void run() {
                if (ex != null) {
                    postError();
                    return;
                }

                ioState = IOState.CONNECTED;

                socketIOClient = client;
                socketIOClient.addListener(ApiManager.EVENT_MESSAGE, ChatService.this);

                showForeground();

                post();
            }
        });
    }

    private void showForeground() {

        Intent openIntent = new Intent(this, MainActivity.class);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pi =
                PendingIntent.getActivity(this, 0, openIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        RemoteViews notificationView = new RemoteViews(this.getPackageName(), R.layout.notification_template);
        notificationView.setTextViewText(R.id.notification_template_title, getString(R.string.service_chat_running));
        notificationView.setTextViewText(R.id.notification_template_text2, getString(R.string.service_chat_running_description));
        notificationView.setOnClickPendingIntent(R.id.notification_template_cancel,
                PendingIntent.getBroadcast(this, 0, new Intent(Constants.FILTER_CHAT_CLOSE), 0));

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        Notification notification = builder.setContentIntent(pi)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContent(notificationView)
                .build();

        notification.flags |= Notification.FLAG_NO_CLEAR;

        startForeground(Constants.NOTIFICICATION_ID_CHAT, notification);
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
                                ChatList newChats = new ChatList(result);
                                syncChatList(newChats);
                                post(newChats);
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

    @Subscribe
    public void onEvent(MuteEvent muteEvent) {
        if (muteEvent.isMuted()) {
            if (muteEvent.getFingerprint() != null) {
                mutedUsers.add(muteEvent.getFingerprint());
            }
        } else if (muteEvent.getFingerprint() != null) {
            mutedUsers.remove(muteEvent.getFingerprint());
        } else if (muteEvent.getFingerprint() == null) {
            mutedUsers.clear();
        }
        syncChatList(chatList);
        post();
    }

    private void syncChatList(ChatList chatList) {

        String myFingerprint = new Device(this).getId();

        Collection<Chat> list = chatList.get();
        Chat.Value chatValue;
        String fingerprint;
        for (Chat chat : list) {
            chatValue = chat.getValue();
            fingerprint = chatValue.getFingerprint();
            chatValue.setMuted(mutedUsers.contains(fingerprint));
            chatValue.setFromMe(fingerprint.equals(myFingerprint));
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
        this.busManager.getChatBus().post(new ChatEvent(ioState, chatList));
    }

    @Produce
    public ChatEvent produce() {
        return new ChatEvent(ioState, chatList);
    }
}
