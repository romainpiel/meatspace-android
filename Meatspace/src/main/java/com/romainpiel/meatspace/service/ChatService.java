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
import com.koushikdutta.async.http.socketio.DisconnectCallback;
import com.koushikdutta.async.http.socketio.ErrorCallback;
import com.koushikdutta.async.http.socketio.EventCallback;
import com.koushikdutta.async.http.socketio.SocketIOClient;
import com.romainpiel.Constants;
import com.romainpiel.lib.api.ApiManager;
import com.romainpiel.lib.api.IOState;
import com.romainpiel.lib.bus.BusManager;
import com.romainpiel.lib.bus.ChatEvent;
import com.romainpiel.lib.bus.MuteEvent;
import com.romainpiel.lib.bus.UIEvent;
import com.romainpiel.lib.helper.PreferencesHelper;
import com.romainpiel.lib.utils.BackgroundExecutor;
import com.romainpiel.lib.utils.CacheManager;
import com.romainpiel.lib.utils.Debug;
import com.romainpiel.meatspace.R;
import com.romainpiel.meatspace.activity.MainActivity;
import com.romainpiel.model.Chat;
import com.romainpiel.model.ChatList;
import com.romainpiel.model.ChatRequest;
import com.romainpiel.model.Device;
import com.romainpiel.model.SocketChatEvent;
import com.squareup.otto.Produce;
import com.squareup.otto.Subscribe;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * Meatspace
 * User: romainpiel
 * Date: 03/11/2013
 * Time: 17:47
 */
public class ChatService extends Service implements ConnectCallback, EventCallback, ErrorCallback, DisconnectCallback {

    private static final String API_GET_CHAT_REQ_ID = "ChatService.GET_CHAT";
    private static final String API_CACHE_MUTED_REQ_ID = "ChatService.CACHE_MUTED";
    private static final String API_GET_CACHED_MUTED_REQ_ID = "ChatService.GET_CACHED_MUTED";

    private ApiManager apiManager;
    private BusManager busManager;
    private BroadcastReceiver closeChatReceiver;
    private SocketIOClient socketIOClient;
    private Handler handler;
    private ChatList chatList;
    private HashSet<String> mutedUsers;
    private IOState ioState;
    private boolean appInBackground;
    private int missedMessageCount;
    private Runnable autoKillTimeoutBgRunnable;
    private CacheManager cacheManager;

    public static void start(Context context) {
        context.startService(new Intent(context, ChatService.class));
    }

    public static void stop(Context context) {
        context.stopService(new Intent(context, ChatService.class));
    }

    @Override
    public void onCreate() {
        super.onCreate();

        cacheManager = new CacheManager(this, false);
        apiManager = ApiManager.get();
        busManager = BusManager.get();
        handler = new Handler();

        ioState = IOState.IDLE;
        chatList = new ChatList();
        mutedUsers = new HashSet<String>();

        BackgroundExecutor.execute(new Runnable() {
            @Override
            public void run() {
                final HashSet<String> cachedMutedUsers = (HashSet<String>) cacheManager.readFile(Constants.CACHE_MUTED_USERS, null);

                if (cachedMutedUsers != null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            mutedUsers = cachedMutedUsers;
                            if (chatList.get() != null && !chatList.get().isEmpty()) {
                                syncChatList(chatList);
                                post();
                            }
                        }
                    });
                }
            }
        }, API_GET_CACHED_MUTED_REQ_ID, null);

        busManager.getChatBus().register(this);
        busManager.getUiBus().register(this);

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
        busManager.getUiBus().unregister(this);
        unregisterReceiver(closeChatReceiver);
        BackgroundExecutor.cancelAll(API_GET_CHAT_REQ_ID, true);
        BackgroundExecutor.cancelAll(API_GET_CACHED_MUTED_REQ_ID, true);
        BackgroundExecutor.cancelAll(API_CACHE_MUTED_REQ_ID, true);
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

    /**
     * Socket connection callback
     *
     * @param ex     potential exception if error
     * @param client socket client
     */
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
                socketIOClient.setErrorCallback(ChatService.this);
                socketIOClient.setDisconnectCallback(ChatService.this);
                socketIOClient.addListener(ChatService.this);

                showForeground();

                post();
            }
        });
    }

    /**
     * place service in foreground or update its notification
     */
    private void showForeground() {

        Intent openIntent = new Intent(this, MainActivity.class);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pi =
                PendingIntent.getActivity(this, 0, openIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        String description;
        if (appInBackground && missedMessageCount > 0) {
            description = getResources().getQuantityString(R.plurals.service_chat_running_description_missed_messages_, missedMessageCount, missedMessageCount);
            if (PreferencesHelper.areNotificationsEnabled(this)) {
                String lastMessage = chatList.get().last().getValue().getMessage();
                builder.setTicker(lastMessage);
            }
        } else {
            description = getString(R.string.service_chat_running_description);
        }

        RemoteViews notificationView = new RemoteViews(this.getPackageName(), R.layout.notification_template);
        notificationView.setTextViewText(R.id.notification_template_title, getString(R.string.service_chat_running));
        notificationView.setTextViewText(R.id.notification_template_text2, description);
        notificationView.setOnClickPendingIntent(R.id.notification_template_cancel,
                PendingIntent.getBroadcast(this, 0, new Intent(Constants.FILTER_CHAT_CLOSE), 0));

        Notification notification = builder.setContentIntent(pi)
                .setSmallIcon(R.drawable.ic_stat_meatspace)
                .setContent(notificationView)
                .build();

        notification.flags |= Notification.FLAG_NO_CLEAR;

        startForeground(Constants.NOTIFICICATION_ID_CHAT, notification);
    }

    /**
     * Socket event callback
     *
     * @param dataString  raw data of the message
     * @param acknowledge socket channel details
     */
    @Override
    public void onEvent(final String dataString, Acknowledge acknowledge) {

        final Gson jsonParser = apiManager.getJsonParser();

        BackgroundExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {

                    final SocketChatEvent event = jsonParser.fromJson(dataString, SocketChatEvent.class);
                    final String name = event.getName();
                    final List<Chat> chats = event.getChats();

                    if (!ApiManager.EVENT_MESSAGE.equals(name) || chats == null)
                        return;

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            ChatList newChats = new ChatList(chats);
                            syncChatList(newChats);
                            saveAndPost(newChats);

                            int newMissedMessageCount = 0;
                            for (Chat chat : chats) {
                                // don't count if it's from me
                                if (!chat.getValue().isFromMe()) {
                                    newMissedMessageCount++;
                                }
                            }
                            if (appInBackground && newMissedMessageCount > 0) {
                                missedMessageCount += newMissedMessageCount;
                                showForeground();
                            }
                        }
                    });

                } catch (Exception e) {
                    Debug.out(e);
                }
            }
        });
    }

    /**
     * a new chat request to post was posted
     *
     * @param chatRequest chat request to post
     */
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

    /**
     * a user mute was requested
     *
     * @param muteEvent associated mute event
     */
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

        BackgroundExecutor.execute(new Runnable() {
            @Override
            public void run() {
                cacheManager.writeFile(Constants.CACHE_MUTED_USERS, mutedUsers);
            }
        }, API_CACHE_MUTED_REQ_ID, null);

        syncChatList(chatList);
        post();
    }

    @Subscribe
    public void onEvent(UIEvent uiEvent) {
        missedMessageCount = 0;
        appInBackground = uiEvent == UIEvent.BACKGROUND;
        showForeground();

        if (uiEvent == UIEvent.BACKGROUND) {
            int autokillTimeout = PreferencesHelper.getAutoKillTimeoutBg(this);
            if (autokillTimeout >= 0) {
                autoKillTimeoutBgRunnable = new Runnable() {
                    @Override
                    public void run() {
                        if (BusManager.get().getUiBus().getLastEvent() == UIEvent.BACKGROUND) {
                            sendBroadcast(new Intent(Constants.FILTER_CHAT_CLOSE));
                        }
                    }
                };
                handler.postDelayed(autoKillTimeoutBgRunnable, autokillTimeout * 60 * 1000);
            }
        } else if (autoKillTimeoutBgRunnable != null) {
            handler.removeCallbacks(autoKillTimeoutBgRunnable);
            autoKillTimeoutBgRunnable = null;
        }
    }

    /**
     * synchronize the chat list:
     * - with the muted users list
     * - init isFromMe variable
     *
     * @param chatList chat list to sync
     */
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

    /**
     * set current ioState to ERROR then post an event on the event bus
     */
    public void postError() {
        ioState = IOState.ERROR;
        post();
    }

    /**
     * add items to current chat list and post an event to the event bus
     *
     * @param items items to add
     */
    public void saveAndPost(ChatList items) {
        this.chatList.addAll(items.get());
        post();
    }

    /**
     * post an event to the event bus
     */
    public void post() {
        this.busManager.getChatBus().post(new ChatEvent(false, ioState, chatList));
    }

    @Produce
    public ChatEvent produce() {
        return new ChatEvent(true, ioState, chatList);
    }

    @Override
    public void onError(String error) {
        postError();
    }

    @Override
    public void onDisconnect(Exception e) {
        sendBroadcast(new Intent(Constants.FILTER_CHAT_CLOSE));
    }
}
