package com.romainpiel.meatspace.activity;

import android.app.AlertDialog;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bugsense.trace.BugSenseHandler;
import com.romainpiel.lib.utils.Debug;
import com.romainpiel.meatspace.BuildConfig;
import com.romainpiel.meatspace.R;
import com.romainpiel.meatspace.service.ChatService;

/**
 * Meatspace
 * User: romainpiel
 * Date: 01/11/2013
 * Time: 16:54
 */
public class MainActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!BuildConfig.IS_DEBUG) {
            BugSenseHandler.initAndStartSession(this, getString(R.string.key_bugsense));
        }
        setContentView(R.layout.activity_main);

        ChatService.start(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_main_about:
                showAboutDialog();
                break;
        }


        return super.onMenuItemSelected(featureId, item);
    }

    private void showAboutDialog() {
        ViewGroup view = (ViewGroup) View.inflate(this, R.layout.dialog_about, null);

        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            TextView versionTv = (TextView) view.findViewById(R.id.dialog_about_version);
            versionTv.setText(String.format(getString(R.string.dialog_about_version),
                    pInfo.versionName, pInfo.versionCode));
        } catch (PackageManager.NameNotFoundException e) {
            Debug.out(e);
        }

        TextView messageTv = (TextView) view.findViewById(R.id.dialog_about_message);
        messageTv.setText(Html.fromHtml(getString(R.string.dialog_about_content)));
        messageTv.setMovementMethod(LinkMovementMethod.getInstance());

        new AlertDialog.Builder(this)
                .setView(view)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }
}
