package com.uu1te721.etcommunications;

import android.graphics.Bitmap;

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


    String getText()
    {
        return mText;
    }

    String getMessageDirection()
    {
        return this.messageDirection;
    }
}
