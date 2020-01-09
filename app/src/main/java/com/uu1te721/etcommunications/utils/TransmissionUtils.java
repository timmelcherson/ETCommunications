package com.uu1te721.etcommunications.utils;

import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static java.nio.ByteBuffer.allocate;

public class TransmissionUtils {

    public static byte[] addTransmissionFlagToByteArray(byte flag, byte[] arr) {

        if ((char) flag == 'i') {
            ByteBuffer combined;
            // its an media file. Create byte buffer of format: {FLAG, size, data, '>'}
            byte[] byteLenArr =  ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(arr.length).array();
            combined = allocate(1 + 4 + arr.length + 4);
            combined.put(flag);
            combined.put(byteLenArr);
            combined.put(arr);
            combined.put((byte) '>');
            combined.put((byte) '>');
            combined.put((byte) '>');
            combined.put((byte) '\r');
            return combined.array();
        }

        else {
            ByteBuffer combined;
            combined = allocate(2 + arr.length + 4);
            combined.put(flag);
            combined.put((byte) arr.length);
            combined.put(arr);
            combined.put((byte) '>');
            combined.put((byte) '>');
            combined.put((byte) '>');
            combined.put((byte) '\r');
            return combined.array();
        }
    }
}
