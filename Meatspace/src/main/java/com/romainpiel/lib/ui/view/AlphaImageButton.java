package com.romainpiel.lib.ui.view;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageButton;

/**
 * MeatspaceProject
 * User: romainpiel
 * Date: 17/11/2013
 * Time: 17:04
 */
public class AlphaImageButton extends ImageButton {

    public AlphaImageButton(Context context) {
        super(context);
    }

    public AlphaImageButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AlphaImageButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        ObjectAnimator.ofFloat(this, "alpha", enabled ? 1f : 0.5f).start();
    }
}
