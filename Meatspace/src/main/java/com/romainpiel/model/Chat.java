package com.romainpiel.model;

/**
 * Meatspace
 * User: romainpiel
 * Date: 01/11/2013
 * Time: 17:33
 */
public class Chat implements Comparable<Chat> {

    private String key;
    private Value value;

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

    @Override
    public int compareTo(Chat another) {
        return compare(this, another);
    }

    public static int compare(Chat lhs, Chat rhs) {
        long l = lhs.getValue().getCreated();
        long r = rhs.getValue().getCreated();
        return l < r ? -1 : (l == r ? 0 : 1);
    }

    public static class Value {
        private String fingerprint;
        private String message;
        private String media;
        private int ttl;
        private long created;
        private boolean isMuted;
        private boolean isFromMe;

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

        public boolean isMuted() {
            return isMuted;
        }

        public void setMuted(boolean isMuted) {
            this.isMuted = isMuted;
        }

        public boolean isFromMe() {
            return isFromMe;
        }

        public void setFromMe(boolean isFromMe) {
            this.isFromMe = isFromMe;
        }
    }
}
