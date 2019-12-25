package com.uu1te721.etcommunications.uicomponents;

import android.graphics.Bitmap;
import android.util.Log;

import static com.uu1te721.etcommunications.utils.Constants.TAG;
// TODO: This only handles pictures. Thus the variable "mMultimediaFile" is not general for audio, etc.

public class MessageCard {
    private String photopath;
    private Bitmap mMultimediaFile;
    private String mText;
    private String messageDirection;

    // Constructor for text messages
    public MessageCard(String text, String direction) {
        this.mText = text;
        this.messageDirection = direction;
    }

    public MessageCard(Bitmap mMultimediaFile, String direction) {
        this.mMultimediaFile = mMultimediaFile;
        this.messageDirection = direction;
        this.mText = "";
    }

    // Constructor for multimedia messages
    public MessageCard(Bitmap mMultimediaFile, String photopath, String direction) {
        this.mMultimediaFile = mMultimediaFile;
        this.messageDirection = direction;
        this.photopath = photopath;
        this.mText = "";
    }

    public Boolean hasPicture(int index) {
        return this.mMultimediaFile != null;
    }


    public String getPhotoPath(){
        return this.photopath;
    }


    public Bitmap getPicture() {
        return this.mMultimediaFile;
    }

    public String getText() {
        return this.mText;
    }

    public String getMessageDirection() {
        return this.messageDirection;
    }
}
