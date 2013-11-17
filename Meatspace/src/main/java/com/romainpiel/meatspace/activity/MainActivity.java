package com.romainpiel.meatspace.activity;

import android.app.AlertDialog;
import android.content.Intent;
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

        startService(new Intent(this, ChatService.class));
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

        TextView textView = (TextView) view.findViewById(R.id.dialog_about_message);
        textView.setText(Html.fromHtml(getString(R.string.dialog_about_content)));
        textView.setMovementMethod(LinkMovementMethod.getInstance());

        new AlertDialog.Builder(this)
                .setView(view)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }
}
