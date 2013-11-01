package com.romainpiel.lib.ui.adapter;

import android.app.Fragment;

/**
 * Meatspace
 * User: romainpiel
 * Date: 01/11/2013
 * Time: 18:27
 */
public abstract class BaseFragment extends Fragment {

    public void runOnUiThread(Runnable runnable) {
        getActivity().runOnUiThread(runnable);
    }
}
