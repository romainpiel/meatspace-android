package com.romainpiel.lib.ui.listener;

import java.util.LinkedList;
import java.util.List;

/**
 * Meatspace
 * User: romainpiel
 * Date: 28/08/2013
 * Time: 15:13
 */
public class OnViewChangedNotifier {

    private static OnViewChangedNotifier currentNotifier;

    private final List<OnViewChangedListener> listeners;

    public OnViewChangedNotifier() {
        this.listeners = new LinkedList<OnViewChangedListener>();
    }

    public static OnViewChangedNotifier replaceNotifier(OnViewChangedNotifier notifier) {
        OnViewChangedNotifier previousNotifier = currentNotifier;
        currentNotifier = notifier;
        return previousNotifier;
    }

    public static void registerOnViewChangedListener(OnViewChangedListener listener) {
        if (currentNotifier != null) {
            currentNotifier.listeners.add(listener);
        }
    }

    public void notifyViewChanged() {
        for (OnViewChangedListener listener : listeners) {
            listener.onViewChanged();
        }
    }

}
