package com.uu1te721.etcommunications;

import android.util.Log;

import java.util.Arrays;

import static com.uu1te721.etcommunications.utils.Constants.TAG;

public class Transmitter {

    public static void transmit(CustomArduino arduino, byte[] txArray) {

        int index = 0;

        while (txArray.length - index > 125) {
            Log.d(TAG, "Sending chunk: " + (index+1));
            byte[] partialArray = Arrays.copyOfRange(txArray, index, index + 124);
            arduino.send(partialArray);
            index = index + 125;
        }

        Log.d(TAG, "Sending final range");
        byte[] finalArray = Arrays.copyOfRange(txArray, index, txArray.length-1);
        arduino.send(finalArray);
    }
}
