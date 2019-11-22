package com.uu1te721.etcommunications;

import android.graphics.Bitmap;
import android.util.Log;


import static com.uu1te721.etcommunications.utils.Constants.TAG;
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
        this.mText = "image";
    }

    Boolean hasPicture(int index) {
        Log.d(TAG, "bitmap of item at position " + index + " NOT null in hasPicture?:  " + String.valueOf(this.mMultimediaFile != null));
        return this.mMultimediaFile != null;
    }

    Bitmap getPicture() {
        return this.mMultimediaFile;
    }

    String getText() {
        return this.mText;
    }

    String getMessageDirection() {
        return this.messageDirection;
    }
}
