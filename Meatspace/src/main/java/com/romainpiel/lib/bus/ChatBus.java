package com.romainpiel.lib.bus;

import com.romainpiel.model.ChatRequest;
import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

/**
 * BlaBlaCar
 * User: romainpiel
 * Date: 29/08/2013
 * Time: 09:04
 *
 * Wrapper around a Bus associated to a Provider
 * Registering to a ChatBus instance will automatically fire an event
 */
public class ChatBus {

    private Bus bus;

    public ChatBus() {
        // events will be fired on the main thread
        this.bus = new Bus(ThreadEnforcer.MAIN);
    }

    public void register(Object object) {
        bus.register(object);
    }

    public void unregister(Object object) {
        bus.unregister(object);
    }

    public void post(ChatEvent event) {
        bus.post(event);
    }

    public void post(ChatRequest chatRequest) {
        bus.post(chatRequest);
    }

    public void post(MuteEvent muteEvent) {
        bus.post(muteEvent);
    }
}
