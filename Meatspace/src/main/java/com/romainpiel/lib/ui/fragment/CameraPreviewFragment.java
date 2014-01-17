package com.romainpiel.lib.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.commonsware.cwac.camera.CameraFragment;
import com.commonsware.cwac.camera.CameraView;
import com.commonsware.cwac.camera.SimpleCameraHost;
import com.romainpiel.lib.ui.view.CameraPreview;

/**
 * meatspace-android
 * romainpiel
 * 16/01/2014
 */
public class CameraPreviewFragment extends CameraFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        CameraView cameraView = new CameraPreview(getActivity());
        cameraView.setHost(new CameraHost(getActivity()));
        cameraView.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                100,
                Gravity.CENTER_VERTICAL
        ));
        cameraView.setBackgroundColor(0x00ff00);

        setCameraView(cameraView);

        return cameraView;
    }

    private class CameraHost extends SimpleCameraHost {

        public CameraHost(Context context) {
            super(context);
        }
    }
}
