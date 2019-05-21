package com.thelastcodebenders.follower.client.telegram;

public enum Channels {
    ADMIN("-1001228646767"), COMMUNITY("@sosyaltrend");

    private final String value;

    Channels(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
