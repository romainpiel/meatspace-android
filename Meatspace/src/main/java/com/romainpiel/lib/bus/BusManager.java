package com.romainpiel.lib.bus;

/**
 * MeatspaceProject
 * User: romainpiel
 * Date: 03/11/2013
 * Time: 18:38
 */
public class BusManager {
    private static BusManager instance;

    private ChatBus chatBus;

    private BusManager() {
        chatBus = new ChatBus();
    }

    public static BusManager get() {
        if (instance == null) {
            instance = new BusManager();
        }
        return instance;
    }

    public ChatBus getChatBus() {
        return chatBus;
    }
}
