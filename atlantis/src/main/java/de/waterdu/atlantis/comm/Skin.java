package de.waterdu.atlantis.comm;

public class Skin {

    private String skin;
    private String signature;

    public Skin() {
        this("", "");
    }

    public Skin(String skin, String signature) {
        this.skin = skin;
        this.signature = signature;
    }

    public boolean hasSkin() {
        return !this.skin.isEmpty();
    }

    public boolean hasSignature() {
        return !this.signature.isEmpty();
    }

    public String getSkin() {
        return this.skin;
    }

    public void setSkin(String skin) {
        this.skin = skin;
    }

    public String getSignature() {
        return this.signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }
}
