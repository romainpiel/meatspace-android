package com.romainpiel.meatspace.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bugsense.trace.BugSenseHandler;
import com.google.analytics.tracking.android.EasyTracker;
import com.romainpiel.Constants;
import com.romainpiel.lib.bus.BusManager;
import com.romainpiel.lib.bus.ChatEvent;
import com.romainpiel.lib.bus.MuteEvent;
import com.romainpiel.lib.bus.UIEvent;
import com.romainpiel.lib.ui.fragment.ChatFragment;
import com.romainpiel.lib.ui.fragment.SettingsFragment;
import com.romainpiel.meatspace.BuildConfig;
import com.romainpiel.meatspace.R;
import com.romainpiel.meatspace.service.ChatService;
import com.squareup.otto.Subscribe;

/**
 * Meatspace
 * User: romainpiel
 * Date: 01/11/2013
 * Time: 16:54
 */
public class MainActivity extends Activity {

    private ProgressDialog progressDialog;
    private AlertDialog errorDialog, aboutDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!BuildConfig.DEBUG) {
            BugSenseHandler.initAndStartSession(this, getString(R.string.key_bugsense));
        }
        setContentView(R.layout.activity_main);
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

        // unregister to bus
        BusManager.get().getChatBus().unregister(this);
    }

    @Override
    public void onResume() {
        super.onResume();

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
                ChatFragment fragment = (ChatFragment) getFragmentManager().findFragmentById(R.id.main_fragment);
                if (fragment != null) {
                    fragment.switchCamera();
                }
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
}
