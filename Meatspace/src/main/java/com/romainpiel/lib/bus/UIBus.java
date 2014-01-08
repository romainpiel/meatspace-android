package com.romainpiel.lib.bus;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

/**
 * MeatspaceProject
 * User: romainpiel
 * Date: 20/12/2013
 * Time: 19:29
 */
public class UIBus {

    private Bus bus;
    private UIEvent lastEvent;

    public UIBus() {
        // events will be fired on the main thread
        this.bus = new Bus(ThreadEnforcer.MAIN);
    }

    public void post(UIEvent uiEvent) {
        bus.post(uiEvent);
        lastEvent = uiEvent;
    }

    public void register(Object object) {
        bus.register(object);
    }

    public void unregister(Object object) {
        bus.unregister(object);
    }

    public UIEvent getLastEvent() {
        return lastEvent;
    }
}
