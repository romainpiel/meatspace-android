package com.romainpiel.lib.ui.fragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import com.romainpiel.lib.api.ApiManager;
import com.romainpiel.lib.bus.BusManager;
import com.romainpiel.lib.bus.ChatEvent;
import com.romainpiel.lib.helper.PreviewHelper;
import com.romainpiel.lib.ui.adapter.ChatAdapter;
import com.romainpiel.lib.ui.view.CameraPreview;
import com.romainpiel.lib.utils.UIUtils;
import com.romainpiel.meatspace.R;
import com.romainpiel.meatspace.service.ChatService;
import com.romainpiel.model.ChatList;
import com.romainpiel.model.Device;
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

    @InjectView(R.id.fragment_chat_list) ListView listView;
    @InjectView(R.id.fragment_chat_camera_preview) CameraPreview cameraPreview;
    @InjectView(R.id.fragment_chat_input) EditText input;
    @InjectView(R.id.fragment_chat_send) ImageButton sendBtn;

    private ProgressDialog progressDialog;
    private AlertDialog errorDialog;
    private ChatAdapter adapter;
    private PreviewHelper previewHelper;
    private Handler uiHandler;
    private Device device;

    public ChatFragment() {
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

        if (device == null) {
            device = new Device(getActivity());
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
        cameraPreview.stopPreview();
        BusManager.get().getChatBus().unregister(this);
        previewHelper.cancelCapture();

        // clear adapter, it will be refilled by the bus producer
        adapter.setItems(null);
    }

    @Override
    public void onResume() {
        super.onResume();
        cameraPreview.startPreview();
        BusManager.get().getChatBus().register(this);
    }

    @Subscribe
    public void onMessage(ChatEvent event) {
        notifyDatasetChanged(event.getChatList());

        switch (event.getIoState()) {
            case IDLE:
                showProgressDialog();
                break;
            case CONNECTING:
                showProgressDialog();
                break;
            case CONNECTED:
                dismissProgressDialog();
                break;
            case DISCONNECTED:
                cancelProgressDialog(
                        getString(R.string.chat_error_app_closed),
                        getString(R.string.chat_error_app_closed_message));
                break;
            case ERROR:
                cancelProgressDialog(
                        getString(R.string.chat_error_unreachable_title),
                        getString(R.string.chat_error_unreachable_message));
                break;
        }
    }

    public void notifyDatasetChanged(final ChatList chatList) {
        notifyDatasetChanged(new Runnable() {
            @Override
            public void run() {
                adapter.setItems(chatList.get());
                UIUtils.scrollToBottom(listView, adapter);
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
            }

        });
    }

    @OnClick(R.id.fragment_chat_send)
    public void send() {
        previewHelper.capture();
    }

    @Override
    public void onCaptureStarted() {
        input.setEnabled(false);
        sendBtn.setEnabled(false);
    }

    @Override
    public void onCaptureComplete(byte[] gifData) {

        ApiManager.get().emit(
                getActivity(),
                UIUtils.getText(input),
                gifData,
                device.getId()
        );

        input.setText(null);

        input.setEnabled(true);
        sendBtn.setEnabled(true);
    }

    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage(getString(R.string.chat_loading));
            progressDialog.setCancelable(true);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    cancelProgressDialog(null, null);
                }
            });
        }
        progressDialog.show();
    }

    private void cancelProgressDialog(String title, String message) {
        dismissProgressDialog();
        if (TextUtils.isEmpty(message)) {
            forceFinish();
        } else {
            if (errorDialog == null) {
                errorDialog = new AlertDialog.Builder(getActivity())
                        .setPositiveButton(R.string.chat_error_connect, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ChatService.start(getActivity());
                            }
                        })
                        .setNegativeButton(R.string.chat_error_leave, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                forceFinish();
                            }
                        })
                        .setCancelable(false)
                        .create();
            }

            errorDialog.setTitle(title);
            errorDialog.setMessage(message);
            errorDialog.show();
        }
    }

    private void dismissProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    private void forceFinish() {
        getActivity().stopService(new Intent(getActivity(), ChatService.class));
        getActivity().finish();
    }
}
