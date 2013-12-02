package com.romainpiel.lib.bus;

/**
 * MeatspaceProject
 * User: romainpiel
 * Date: 02/12/2013
 * Time: 20:27
 */
public class MuteEvent {

    private boolean isMuted;
    private String fingerprint;

    public MuteEvent(boolean isMuted, String fingerprint) {
        this.isMuted = isMuted;
        this.fingerprint = fingerprint;
    }

    public boolean isMuted() {
        return isMuted;
    }

    public void setMuted(boolean isMuted) {
        this.isMuted = isMuted;
    }

    public String getFingerprint() {
        return fingerprint;
    }

    public void setFingerprint(String fingerprint) {
        this.fingerprint = fingerprint;
    }
}
