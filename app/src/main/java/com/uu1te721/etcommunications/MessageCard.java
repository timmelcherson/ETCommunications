package com.uu1te721.etcommunications;

import android.graphics.Bitmap;

 // TODO: This only handles pictures. Thus the variable "multimediafile" is not general for audio, etc.

 class MessageCard {
    private Bitmap multimediafile;
    private String mText;
    private String messageDirection;

    MessageCard(String text, String direction) {
        this.mText = text;
        this.messageDirection = direction;
    }

    MessageCard(Bitmap multimediafile, String direction){
        this.multimediafile = multimediafile;
        this.messageDirection = direction;
    }

    Boolean hasPicture(){
        if (multimediafile != null){
            return true;
        } else{
            return false;
        }
    }

    Bitmap getPicture(){
        return multimediafile;
    }

    String getText()
    {
        return mText;
    }

    String getMessageDirection()
    {
        return this.messageDirection;
    }
}
