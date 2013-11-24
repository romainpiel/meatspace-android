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

    private IOState ioState;
    private ChatList chatList;

    public ChatEvent(IOState ioState, ChatList chatList) {
        this.ioState = ioState;
        this.chatList = chatList;
    }

    public IOState getIoState() {
        return ioState;
    }

    public void setIoState(IOState ioState) {
        this.ioState = ioState;
    }

    public ChatList getChatList() {
        return chatList;
    }

    public void setChatList(ChatList chatList) {
        this.chatList = chatList;
    }
}
