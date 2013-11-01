package com.romainpiel.model;

import java.util.List;

/**
 * Meatspace
 * User: romainpiel
 * Date: 01/11/2013
 * Time: 17:37
 */
public class ChatList {

    private Content chats;

    public List<Chat> get() {
        return chats != null? chats.get() : null;
    }

    private static class Content {
        private List<Chat> chats;

        public List<Chat> get() {
            return chats;
        }
    }
}
