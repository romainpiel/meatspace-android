package com.romainpiel.lib.api;

import com.romainpiel.model.ChatList;

import retrofit.http.GET;

/**
 * Meatspace
 * User: romainpiel
 * Date: 01/11/2013
 * Time: 17:05
 */
public interface MeatspaceRestClient {

    @GET("/get/chats")
    public ChatList getChats();
}
