package com.romainpiel.lib;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

/**
 * MeatspaceProject
 * User: romainpiel
 * Date: 03/11/2013
 * Time: 18:38
 */
public class BusManager {
    private static BusManager instance;

    private Bus chatBus;

    private BusManager() {
        chatBus = new Bus(ThreadEnforcer.MAIN);
    }

    public static BusManager get() {
        if (instance == null) {
            instance = new BusManager();
        }
        return instance;
    }

    public Bus getChatBus() {
        return chatBus;
    }
}
