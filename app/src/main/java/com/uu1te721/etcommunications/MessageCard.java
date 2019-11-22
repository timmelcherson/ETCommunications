package com.uu1te721.etcommunications;

import android.graphics.Bitmap;

// TODO: This only handles pictures. Thus the variable "mMultimediaFile" is not general for audio, etc.

class MessageCard {
    private Bitmap mMultimediaFile;
    private String mText;
    private String messageDirection;

    // Constructor for text messages
    MessageCard(String text, String direction) {
        this.mText = text;
        this.messageDirection = direction;
    }

    // Constructor for multimedia messages
    MessageCard(Bitmap mMultimediaFile, String direction) {
        this.mMultimediaFile = mMultimediaFile;
        this.messageDirection = direction;
    }

    Boolean hasPicture() {
        return this.mMultimediaFile != null;
    }

    Bitmap getPicture() {
        return mMultimediaFile;
    }

    String getText() {
        return mText;
    }

    String getMessageDirection() {
        return this.messageDirection;
    }
}
