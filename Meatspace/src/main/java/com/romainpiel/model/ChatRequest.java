package com.romainpiel.model;

/**
 * MeatspaceProject
 * User: romainpiel
 * Date: 10/11/2013
 * Time: 18:25
 */
public class ChatRequest {

    private String apiKey;
    private String message;
    private String picture;
    private String fingerprint;

    public ChatRequest(String apiKey, String message, String picture, String fingerprint) {
        this.apiKey = apiKey;
        this.message = message;
        this.picture = picture;
        this.fingerprint = fingerprint;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getFingerprint() {
        return fingerprint;
    }

    public void setFingerprint(String fingerprint) {
        this.fingerprint = fingerprint;
    }
}
