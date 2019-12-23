package com.uu1te721.etcommunications.utils;

import java.nio.ByteBuffer;

public class TransmissionUtils {

    public static byte[] addTransmissionFlagToByteArray(byte flag, byte[] arr) {

        ByteBuffer combined = ByteBuffer.allocate(1 + arr.length + 3);
        combined.put(flag);
        combined.put(arr);
        combined.put((byte) '>');
        combined.put((byte) '>');
        combined.put((byte) '>');
        return combined.array();
    }
}
