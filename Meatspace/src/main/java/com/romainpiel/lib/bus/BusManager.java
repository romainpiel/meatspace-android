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
    private UIBus uiBus;

    private BusManager() {
        chatBus = new ChatBus();
        uiBus = new UIBus();
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

    public UIBus getUiBus() {
        return uiBus;
    }
}
