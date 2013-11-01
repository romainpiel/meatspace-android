package com.romainpiel.lib.api.meatspace;

import com.romainpiel.model.ChatList;

import retrofit.http.GET;

/**
 * Meatspace
 * User: romainpiel
 * Date: 01/11/2013
 * Time: 17:05
 */
public interface MeatspaceClient {

    @GET("/get/chats")
    public ChatList getChats();
}
