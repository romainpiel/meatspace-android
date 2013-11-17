package com.romainpiel.lib.bus;

import com.romainpiel.model.ChatList;
import com.romainpiel.model.ChatRequest;
import com.squareup.otto.Bus;
import com.squareup.otto.Produce;
import com.squareup.otto.ThreadEnforcer;

/**
 * BlaBlaCar
 * User: romainpiel
 * Date: 29/08/2013
 * Time: 09:04
 *
 * Wrapper around a Bus associated to a Provider
 * Registering to a SessionBus instance will automatically fire an event
 */
public class ChatBus {

    private Bus bus;
    private ChatList chatList;

    public ChatBus() {
        // events will be fired on the main thread
        this.bus = new Bus(ThreadEnforcer.MAIN);
        this.chatList = new ChatList();

        bus.register(new Producer());
    }

    public void register(Object object) {
        bus.register(object);
    }

    public void unregister(Object object) {
        bus.unregister(object);
    }

    public void post(ChatList items) {
        this.chatList.addAll(items.get());
        this.chatList.setFromNetwork(true);
        bus.post(chatList);
    }

    public void post(ChatRequest chatRequest) {
        bus.post(chatRequest);
    }

    // IMPORTANT: the producer must target only one bus, ie. not be registered to multiple buses
    private class Producer {

        @Produce
        public ChatList produce() {
            return chatList;
        }
    }
}
