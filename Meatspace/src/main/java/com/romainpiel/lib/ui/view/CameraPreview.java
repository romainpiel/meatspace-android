package com.romainpiel.lib.ui.view;

import android.content.Context;
import android.view.View;

import com.commonsware.cwac.camera.CameraView;
import com.romainpiel.lib.utils.Debug;

/**
 * meatspace-android
 * romainpiel
 * 16/01/2014
 */
public class CameraPreview extends CameraView {

    public CameraPreview(Context context) {
        super(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        Debug.out("onMeasure "+width + " " + height);

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        super.onLayout(changed, l, t, r, b);

        if (changed && getChildCount() > 0) {

            View child = getChildAt(0);
            assert child != null;

            final int width = r - l;
            final int height = b - t;
            Debug.out("onLayout "+width + " " + height);
        }

    }
}
