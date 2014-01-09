package com.romainpiel.lib.ui.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.romainpiel.Constants;
import com.romainpiel.meatspace.BuildConfig;
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

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ListPreference urlPreference = (ListPreference) findPreference(getString(R.string.settings_url_key));
        if (urlPreference.getValue() == null) {
            urlPreference.setValue(getString(BuildConfig.MEATSPACE_BASE_URL));
        }
        urlPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                getActivity().sendBroadcast(new Intent(Constants.FILTER_CHAT_CLOSE));
                return true;
            }
        });
    }
}
