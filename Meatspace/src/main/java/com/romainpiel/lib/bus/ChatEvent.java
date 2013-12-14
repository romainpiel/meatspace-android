package com.romainpiel.lib.bus;

import com.romainpiel.lib.api.IOState;
import com.romainpiel.model.ChatList;

/**
 * MeatspaceProject
 * User: romainpiel
 * Date: 18/11/2013
 * Time: 08:36
 */
public class ChatEvent {

    private boolean fromProducer;
    private IOState ioState;
    private ChatList chatList;

    public ChatEvent(boolean fromProducer, IOState ioState, ChatList chatList) {
        this.fromProducer = fromProducer;
        this.ioState = ioState;
        this.chatList = chatList;
    }

    public boolean isFromProducer() {
        return fromProducer;
    }

    public IOState getIoState() {
        return ioState;
    }

    public ChatList getChatList() {
        return chatList;
    }
}
