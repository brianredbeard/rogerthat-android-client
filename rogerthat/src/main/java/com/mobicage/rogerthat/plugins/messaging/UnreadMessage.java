package com.mobicage.rogerthat.plugins.messaging;

public class UnreadMessage {
    public String key;
    public String message;
    public String friendName;
    public String friendEmail;

    UnreadMessage(String key, String message, String friendName, String friendEmail) {
        this.key = key;
        this.message = message;
        this.friendName = friendName;
        this.friendEmail = friendEmail;
    }

}
