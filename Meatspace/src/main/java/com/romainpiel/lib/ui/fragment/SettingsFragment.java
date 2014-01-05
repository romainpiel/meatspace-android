package com.romainpiel.lib.ui.fragment;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.romainpiel.meatspace.R;

/**
 * MeatspaceProject
 * User: romainpiel
 * Date: 09/12/2013
 * Time: 19:26
 */
public class SettingsFragment extends PreferenceDialogFragment {

    public SettingsFragment() {
        super(R.xml.settings);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle b) {
        getDialog().setTitle(getString(R.string.settings_title));
        getDialog().getWindow().getAttributes().windowAnimations = R.style.Meatspace_Dialog_WindowAnimation;
        return super.onCreateView(inflater, container, b);
    }
}
