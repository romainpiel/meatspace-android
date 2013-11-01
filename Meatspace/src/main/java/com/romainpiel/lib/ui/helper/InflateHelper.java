package com.romainpiel.lib.ui.helper;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.romainpiel.lib.ui.listener.OnViewChangedListener;
import com.romainpiel.lib.ui.listener.OnViewChangedNotifier;

/**
 * BlaBlaCar
 * User: romainpiel
 * Date: 28/08/2013
 * Time: 15:19
 *
 * A simple view helper that does the annoying stuff for views
 */
public class InflateHelper {

    private Context context;
    private ViewGroup viewGroup;
    private int layoutId;
    private OnViewChangedNotifier onViewChangedNotifier;
    private boolean alreadyInflated;

    /**
     * Constructor
     *
     * @param context view's context
     * @param viewGroup view
     * @param onViewChangedListener a listener that will fire an event when the subviews are ready to be retrieved
     * @param layoutId the layout id to inflate
     */
    public InflateHelper(Context context, ViewGroup viewGroup, OnViewChangedListener onViewChangedListener, int layoutId) {
        this.context = context;
        this.viewGroup = viewGroup;
        this.layoutId = layoutId;

        onViewChangedNotifier = new OnViewChangedNotifier();
        alreadyInflated = false;

        OnViewChangedNotifier previousNotifier = OnViewChangedNotifier.replaceNotifier(onViewChangedNotifier);
        OnViewChangedNotifier.registerOnViewChangedListener(onViewChangedListener);
        OnViewChangedNotifier.replaceNotifier(previousNotifier);
    }

    /**
     * Call that method from your view's onFinishInflate()
     * Better to call it before the call to super.onFinishInflate()
     */
    public void onFinishInflate() {
        // check if already inflated to avoid inconvenient loops for the <merge/> layouts
        if (!alreadyInflated) {
            alreadyInflated = true;
            View.inflate(context, layoutId, viewGroup);
            onViewChangedNotifier.notifyViewChanged();
        }
    }
}
