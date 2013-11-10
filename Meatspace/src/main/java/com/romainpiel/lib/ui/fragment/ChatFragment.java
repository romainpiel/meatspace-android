package com.romainpiel.lib.ui.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import com.romainpiel.lib.BusManager;
import com.romainpiel.lib.api.ApiManager;
import com.romainpiel.lib.helper.PreviewHelper;
import com.romainpiel.lib.ui.adapter.ChatAdapter;
import com.romainpiel.lib.ui.view.CameraPreview;
import com.romainpiel.lib.utils.BackgroundExecutor;
import com.romainpiel.lib.utils.Debug;
import com.romainpiel.meatspace.R;
import com.romainpiel.model.Chat;
import com.romainpiel.model.ChatList;
import com.squareup.otto.Subscribe;

import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.Views;

/**
 * Meatspace
 * User: romainpiel
 * Date: 01/11/2013
 * Time: 16:55
 */
public class ChatFragment extends Fragment implements PreviewHelper.OnCaptureListener, CameraPreview.PreviewReadyCallback {

    private static final String API_GET_CHAT_REQ_ID = "ChatFragment.GET_CHAT";

    @InjectView(R.id.fragment_chat_list) ListView listView;
    @InjectView(R.id.fragment_chat_camera_preview) CameraPreview cameraPreview;
    @InjectView(R.id.fragment_chat_send) Button sendBtn;

    private ChatAdapter adapter;
    private PreviewHelper previewHelper;
    private boolean initialized;
    private Handler uiHandler;

    public ChatFragment() {
        this.initialized = false;
        this.uiHandler = new Handler();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, null);
        Views.inject(this, view);

        previewHelper = new PreviewHelper(uiHandler);
        previewHelper.setOnCaptureListener(this);
        cameraPreview.setPreviewCallback(previewHelper);
        cameraPreview.setOnPreviewReady(this);

        return view;
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        if (adapter == null) {
            adapter = new ChatAdapter(getActivity());
        }

        if (!initialized) {
            fetchChat();
        }

        listView.setAdapter(adapter);
    }

    @Override
    public void onPreviewReady() {
        previewHelper.setAngle(cameraPreview.getAngle());
    }

    @Override
    public void onPause() {
        super.onPause();
        BackgroundExecutor.cancelAll(API_GET_CHAT_REQ_ID, true);
        BusManager.get().getChatBus().unregister(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        BusManager.get().getChatBus().register(this);
    }

    @Subscribe
    public void onMessage(ChatList chatList) {
        notifyDatasetChanged(chatList, false);
    }

    public void fetchChat() {
        // TODO do that in the ChatService
        BackgroundExecutor.execute(
                new Runnable() {
                    @Override
                    public void run() {
                        final ChatList chatList = ApiManager.get().meatspace().getChats();
                        if (chatList != null) {
                            notifyDatasetChanged(chatList, true);
                            initialized = true;
                        }
                    }
                },
                API_GET_CHAT_REQ_ID,
                null
        );
    }

    public void notifyDatasetChanged(final ChatList chatList, final boolean clearBefore) {
        notifyDatasetChanged(new Runnable() {
            @Override
            public void run() {
                if (clearBefore) {
                    adapter.setItems(chatList.get());
                } else {
                    adapter.appendItems(chatList.get());
                }
            }
        });
    }

    public void notifyDatasetChanged(final Runnable runBefore) {
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                if (runBefore != null) {
                    runBefore.run();
                }
                adapter.notifyDataSetChanged();
                initialized = true;
            }

        });
    }

    @OnClick(R.id.fragment_chat_send)
    public void send() {
        previewHelper.capture();
    }

    @Override
    public void onCaptureStarted() {
        sendBtn.setEnabled(false);
    }

    @Override
    public void onCaptureComplete(byte[] gifData) {

        Chat chat = new Chat("", "message", gifData);
        Debug.out(chat);

        sendBtn.setEnabled(true);
    }
}
