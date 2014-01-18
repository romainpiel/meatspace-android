package com.romainpiel.meatspace.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bugsense.trace.BugSenseHandler;
import com.google.analytics.tracking.android.EasyTracker;
import com.romainpiel.Constants;
import com.romainpiel.lib.api.ApiManager;
import com.romainpiel.lib.bus.BusManager;
import com.romainpiel.lib.bus.ChatEvent;
import com.romainpiel.lib.bus.MuteEvent;
import com.romainpiel.lib.bus.UIEvent;
import com.romainpiel.lib.helper.PreferencesHelper;
import com.romainpiel.lib.helper.PreviewHelper;
import com.romainpiel.lib.ui.fragment.CameraPreviewFragment;
import com.romainpiel.lib.ui.fragment.SettingsFragment;
import com.romainpiel.lib.utils.UIUtils;
import com.romainpiel.meatspace.BuildConfig;
import com.romainpiel.meatspace.R;
import com.romainpiel.meatspace.service.ChatService;
import com.romainpiel.model.Device;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Meatspace
 * User: romainpiel
 * Date: 01/11/2013
 * Time: 16:54
 */
public class MainActivity extends Activity implements PreviewHelper.OnCaptureListener {

    @InjectView(R.id.activity_main_input) EditText input;
    @InjectView(R.id.activity_main_send) ImageButton sendBtn;
    @InjectView(R.id.activity_main_progress_bar) ProgressBar progressBar;
    @InjectView(R.id.activity_main_char_count) TextView charCount;

    private ProgressDialog progressDialog;
    private AlertDialog errorDialog, aboutDialog;
    private CameraPreviewFragment frontCameraFragment, backCameraFragment;
    private Boolean isFrontCamera;
    private PreviewHelper previewHelper;
    private int maxCharCount;
    private Device device;
    private PreferencesHelper preferencesHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!BuildConfig.DEBUG) {
            BugSenseHandler.initAndStartSession(this, getString(R.string.key_bugsense));
        }

        setContentView(R.layout.activity_main);

        ButterKnife.inject(this);

        maxCharCount = getResources().getInteger(R.integer.input_max_char_count);

        preferencesHelper = new PreferencesHelper(this);
        previewHelper = new PreviewHelper(new Handler());
        previewHelper.setOnCaptureListener(this);
        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                invalidateMaxCharCount();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        invalidateMaxCharCount();

        device = new Device(this);

        setupCamera(false);
    }

    @Override
    public void onStart() {
        super.onStart();
        EasyTracker.getInstance(this).activityStart(this);
        ChatService.start(this);
        BusManager.get().getUiBus().post(UIEvent.FOREGROUD);
    }

    @Override
    public void onStop() {
        super.onStop();
        EasyTracker.getInstance(this).activityStop(this);
        BusManager.get().getUiBus().post(UIEvent.BACKGROUND);
    }

    @Override
    public void onPause() {
        super.onPause();

        // cancel capture
        previewHelper.cancelCapture();

        // unregister to bus
        BusManager.get().getChatBus().unregister(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        // enable ui
        setInputEnabled(true);

        // register to bus
        BusManager.get().getChatBus().register(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem item = menu.findItem(R.id.menu_main_switch_camera);
        if (item != null) {
            item.setVisible(Camera.getNumberOfCameras() > 1);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_main_switch_camera:
                setupCamera(true);
                break;
            case R.id.menu_main_disconnect:
                sendBroadcast(new Intent(Constants.FILTER_CHAT_CLOSE));
                break;
            case R.id.menu_main_unmute_all:
                BusManager.get().getChatBus().post(new MuteEvent(false, null));
                break;
            case R.id.menu_main_settings:
                showSettings();
                break;
            case R.id.menu_main_about:
                showAboutDialog();
                break;
        }


        return super.onMenuItemSelected(featureId, item);
    }

    @Subscribe
    public void onMessage(ChatEvent event) {

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

    /**
     * show about dialog
     */
    public void showAboutDialog() {
        if (aboutDialog == null) {
            ViewGroup view = (ViewGroup) View.inflate(this, R.layout.dialog_about, null);

            TextView versionTv = (TextView) view.findViewById(R.id.dialog_about_version);
            versionTv.setText(String.format(getString(R.string.dialog_about_version),
                    BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE));

            TextView messageTv = (TextView) view.findViewById(R.id.dialog_about_message);
            messageTv.setText(Html.fromHtml(getString(R.string.dialog_about_content)));
            messageTv.setMovementMethod(LinkMovementMethod.getInstance());

            aboutDialog = new AlertDialog.Builder(this)
                    .setView(view)
                    .setPositiveButton(android.R.string.ok, null)
                    .create();
            aboutDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {
                    aboutDialog = null;
                }
            });
            aboutDialog.getWindow().getAttributes().windowAnimations = R.style.Meatspace_Dialog_WindowAnimation;
            aboutDialog.show();
        }
    }

    /**
     * show settings dialog
     */
    public void showSettings() {
        FragmentManager fm = getFragmentManager();
        if (fm.findFragmentByTag(Constants.FRAGMENT_TAG_SETTINGS) == null) {
            new SettingsFragment().show(fm, Constants.FRAGMENT_TAG_SETTINGS);
        }
    }

    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage(getString(R.string.chat_loading));
            progressDialog.setCancelable(true);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {
                    progressDialog = null;
                }
            });
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
                errorDialog = new AlertDialog.Builder(this)
                        .setPositiveButton(R.string.chat_error_connect, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ChatService.start(MainActivity.this);
                            }
                        })
                        .setNeutralButton(R.string.chat_error_settings, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                FragmentManager fm = getFragmentManager();
                                new SettingsFragment().show(fm, null);
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
                errorDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        errorDialog = null;
                    }
                });
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
        stopService(new Intent(this, ChatService.class));
        finish();
    }


    @OnClick(R.id.activity_main_send)
    public void send() {
        previewHelper.capture();
    }

    @Override
    public void onCaptureStarted() {
        progressBar.setProgress(0);
        setInputEnabled(false);
    }

    @Override
    public void onCaptureProgress(float progress) {
        progressBar.setProgress((int) (progress * 100));
    }

    @Override
    public void onCaptureComplete(byte[] gifData) {

        ApiManager.get().emit(
                this,
                UIUtils.getText(input),
                gifData,
                device.getId()
        );

        input.setText(null);
        setInputEnabled(true);
    }

    @Override
    public void onCaptureFailed() {
        setInputEnabled(true);
        Toast.makeText(this, R.string.chat_error_couldnotpostmessage, Toast.LENGTH_SHORT).show();
    }

    private void setInputEnabled(boolean enabled) {
        input.setEnabled(enabled);
        sendBtn.setEnabled(enabled);
        progressBar.setVisibility(enabled ? View.GONE : View.VISIBLE);
    }

    public void setupCamera(boolean toggle) {
        // cancel capture
        previewHelper.cancelCapture();
        setInputEnabled(true);

        // check from prefs if null
        if (isFrontCamera == null) {
            isFrontCamera = preferencesHelper.isFrontCamera(true);
        }

        // else toggle if that's we want
        else if (toggle) {
            isFrontCamera = !isFrontCamera;
            preferencesHelper.saveIsFrontCamera(isFrontCamera);
        }

        // take previous fragment
        CameraPreviewFragment f = isFrontCamera? frontCameraFragment : backCameraFragment;

        // or create one if first time on that orientation
        if (f == null) {
            f = CameraPreviewFragment.newInstance(isFrontCamera);
        }

        // save that in a variable
        if (isFrontCamera) {
            frontCameraFragment = f;
        } else {
            backCameraFragment = f;
        }

        // and show!
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.activity_main_camera_preview, f)
                .commit();
    }

    private void invalidateMaxCharCount() {
        int maxCharLeft = maxCharCount - input.getText().length();
        charCount.setText(String.valueOf(maxCharLeft));
        sendBtn.setEnabled(maxCharLeft >= 0);
    }
}
