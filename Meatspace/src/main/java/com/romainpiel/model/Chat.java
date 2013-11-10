package com.romainpiel.model;

import android.util.Base64;

import com.romainpiel.lib.utils.Debug;

import java.io.UnsupportedEncodingException;

/**
 * Meatspace
 * User: romainpiel
 * Date: 01/11/2013
 * Time: 17:33
 */
public class Chat {

    private String key;
    private Value value;

    public Chat(String fingerprint, String message, byte[] gifBytes) {
        // TODO fill remaining fields
        value = new Value(fingerprint, message, gifBytes, 0, 0);
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Value getValue() {
        return value;
    }

    public void setValue(Value value) {
        this.value = value;
    }

    public static class Value {
        private String fingerprint;
        private String message;
        private String media;
        private int ttl;
        private long created;

        private static final String GIF_PREFIX = "data:image/gif;base64,";

        public Value(String fingerprint, String message, byte[] gifBytes, int ttl, long created) {
            this.fingerprint = fingerprint;
            this.message = message;
            mediaFromGIFbytes(gifBytes);
            this.ttl = ttl;
            this.created = created;
        }

        public String getFingerprint() {
            return fingerprint;
        }

        public void setFingerprint(String fingerprint) {
            this.fingerprint = fingerprint;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getMedia() {
            return media;
        }

        public void setMedia(String media) {
            this.media = media;
        }

        public int getTtl() {
            return ttl;
        }

        public void setTtl(int ttl) {
            this.ttl = ttl;
        }

        public long getCreated() {
            return created;
        }

        public void setCreated(long created) {
            this.created = created;
        }

        public void mediaFromGIFbytes(byte[] bytes) {
            try {
                media = GIF_PREFIX + new String(Base64.encode(bytes, Base64.DEFAULT), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                Debug.out(e);
            }
        }

        public byte[] mediaToGIFbytes() {
            String gifData = media.replace(GIF_PREFIX, "");
            return Base64.decode(gifData, Base64.DEFAULT);
        }
    }
}
