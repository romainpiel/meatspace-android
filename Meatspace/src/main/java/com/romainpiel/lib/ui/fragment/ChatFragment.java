package com.romainpiel.lib.ui.fragment;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.romainpiel.lib.BusManager;
import com.romainpiel.lib.api.ApiManager;
import com.romainpiel.lib.gif.AnimatedGifEncoder;
import com.romainpiel.lib.ui.adapter.ChatAdapter;
import com.romainpiel.lib.ui.view.CameraPreview;
import com.romainpiel.lib.utils.BackgroundExecutor;
import com.romainpiel.meatspace.R;
import com.romainpiel.model.ChatList;
import com.squareup.otto.Subscribe;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.Views;

/**
 * Meatspace
 * User: romainpiel
 * Date: 01/11/2013
 * Time: 16:55
 */
public class ChatFragment extends Fragment implements Camera.PreviewCallback {

    public static final int CAPTURE_DURATION = 2000;
    private static final String API_GET_CHAT_REQ_ID = "ChatFragment.GET_CHAT";

    @InjectView(R.id.fragment_chat_list) ListView listView;
    @InjectView(R.id.fragment_chat_camera_preview) CameraPreview cameraPreview;

    private ChatAdapter adapter;
    private boolean initialized;
    private boolean capturing;
    private Handler uiHandler;
    private Runnable stopCaptureRunnable;
    private AnimatedGifEncoder gifEncoder;
    private OutputStream gifStream;

    public ChatFragment() {
        this.initialized = false;
        this.uiHandler = new Handler();
        this.stopCaptureRunnable = new Runnable() {
            @Override
            public void run() {
                capturing = false;

                gifEncoder.finish();

                // TODO use data from gifStream to send it
                // ...

                gifStream = null;
            }
        };
        this.gifEncoder = new AnimatedGifEncoder();
        this.gifEncoder.setDelay(150);
        this.gifEncoder.setRepeat(0);
        this.gifEncoder.setSize(128, 96);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, null);
        Views.inject(this, view);

        cameraPreview.setPreviewCallback(this);

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
        if (!capturing) {
            gifStream = new ByteArrayOutputStream();
            gifEncoder.start(gifStream);

            uiHandler.postDelayed(stopCaptureRunnable, CAPTURE_DURATION);

            capturing = true;
        }
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {

        if (capturing) {
            // TODO maybe do that in a separate thread
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            gifEncoder.addFrame(bitmap);
            bitmap.recycle();
        }
    }
}
