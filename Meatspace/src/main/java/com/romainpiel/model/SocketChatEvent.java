package com.romainpiel.model;

import java.util.ArrayList;
import java.util.List;

/**
 * MeatspaceProject
 * User: romainpiel
 * Date: 07/12/2013
 * Time: 15:53
 *
 * model for a socket io event
 */
public class SocketChatEvent {

    private String name;
    private List<ChatWrapper> args;

    public String getName() {
        return name;
    }

    public List<Chat> getChats() {
        if (args == null) return null;
        List<Chat> result = new ArrayList<Chat>();
        for (ChatWrapper chatWrapper : args) {
            result.add(chatWrapper.chat);
        }
        return result;
    }

    private class ChatWrapper {
        private Chat chat;
    }
}
