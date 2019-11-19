package com.uu1te721.etcommunications;

public class MessageCard {
    private String mText;
    private String messageDirection;

    public MessageCard(String text, String direction) {
        this.mText = text;
        this.messageDirection = direction;
    }

    public void setText(String str) {
        this.mText = str;
    }

    public String getText() {
        return mText;
    }

    public String getMessageDirection() {
        return this.messageDirection;
    }
}
