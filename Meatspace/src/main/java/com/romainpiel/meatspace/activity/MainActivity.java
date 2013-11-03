package com.romainpiel.meatspace.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

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
        setContentView(R.layout.activity_main);

        startService(new Intent(this, ChatService.class));
    }
}
