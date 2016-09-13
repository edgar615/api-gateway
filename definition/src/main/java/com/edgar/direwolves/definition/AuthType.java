package com.edgar.direwolves.definition;

public enum AuthType {

    /**
     * JWT
     */
    JWT("jwt"),

    /**
     * appKey
     */
    APP_KEY("appKey");

    private final String value;

    AuthType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}