package com.romainpiel.model;

import com.romainpiel.lib.utils.BoundedTreeSet;

import java.util.Collection;
import java.util.Comparator;

/**
 * Meatspace
 * User: romainpiel
 * Date: 01/11/2013
 * Time: 17:37
 */
public class ChatList {

    private Content chats;

    public ChatList() {
        this(null);
    }

    public ChatList(Collection<Chat> chats) {
        this.chats = new Content();

        if (chats != null) {
            this.chats.addAll(chats);
        }
    }

    public Collection<Chat> get() {
        return chats != null? chats.get() : null;
    }

    public void addAll(Collection<Chat> items) {
        if (chats != null && chats.get() != null) {
            chats.get().addAll(items);
        }
    }

    public void clear() {
        if (chats != null && chats.get() != null) {
            chats.get().clear();
        }
    }

    public void setMaxSize(int maxSize) {
        if (chats != null && chats.get() != null) {
            chats.get().setMaxSize(maxSize);
        }
    }

    private static class Content {
        private BoundedTreeSet<Chat> chats;

        public Content() {
            chats = new BoundedTreeSet<Chat>(new Comparator<Chat>() {
                @Override
                public int compare(Chat lhs, Chat rhs) {
                    return Chat.compare(lhs, rhs);
                }
            });
        }

        public BoundedTreeSet<Chat> get() {
            return chats;
        }

        public void addAll(Collection<Chat> items) {
            chats.addAll(items);
        }
    }
}
