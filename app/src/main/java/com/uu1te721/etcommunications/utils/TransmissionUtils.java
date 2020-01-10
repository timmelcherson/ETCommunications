package com.uu1te721.etcommunications.utils;

import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static java.nio.ByteBuffer.allocate;

public class TransmissionUtils {

    public static byte[] addTransmissionFlagToByteArray(byte flag, byte[] arr) {

        ByteBuffer combined = ByteBuffer.allocate(1 + arr.length + 3);
        combined.put(flag);
        combined.put(arr);
        combined.put((byte) '>');
        combined.put((byte) '>');
        combined.put((byte) '>');
        return combined.array();

        /*if ((char) flag == 'i') {
//            ByteBuffer combined;
            // its an media file. Create byte buffer of format: {FLAG, size, data, '>'}
//            byte[] byteLenArr =  ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(arr.length).array();
            ByteBuffer combined = ByteBuffer.allocate(1 + arr.length + 3);
            combined.put(flag);
            combined.put(arr);
            combined.put((byte) '>');
            combined.put((byte) '>');
            combined.put((byte) '>');
            return combined.array();
        }

        else {
            ByteBuffer combined;
            combined = ByteBuffer.allocate(1 + arr.length + 3);
            combined.put(flag);
//            combined.put((byte) arr.length);
            combined.put(arr);
            combined.put((byte) '>');
            combined.put((byte) '>');
            combined.put((byte) '>');
            return combined.array();
        }*/
    }
}
