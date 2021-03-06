package com.uu1te721.etcommunications.arduino;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.util.Log;

import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;
import com.uu1te721.etcommunications.activities.MainActivity;
import com.uu1te721.etcommunications.activities.MessengerActivity;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static com.uu1te721.etcommunications.utils.Constants.ACTION_USB_PERMISSION;
import static com.uu1te721.etcommunications.utils.Constants.TAG;
import static java.nio.ByteBuffer.allocate;


public class CustomArduino implements UsbSerialInterface.UsbReadCallback {
    private Context context;
    private CustomArduinoListener listener;

    private UsbDeviceConnection connection;
    private UsbSerialDevice serialPort;
    private UsbReceiver usbReceiver;
    private UsbManager usbManager;
    private UsbDevice lastArduinoAttached;

    private int baudRate;
    private boolean isOpened;
    private List<Integer> vendorIds;
    private List<Byte> bytesReceived;
    private byte delimiter;

    private static final int DEFAULT_BAUD_RATE = 9600;
    private static final byte DEFAULT_DELIMITER = '\n';


    public CustomArduino(Context context, int baudRate) {
        init(context, baudRate);
    }

    public CustomArduino(Context context) {
        init(context, DEFAULT_BAUD_RATE);
    }

    private void init(Context context, int baudRate) {
        Log.d(TAG, "Initialize arduino from context: " + context);

        if (context instanceof MainActivity) {
            Log.d(TAG, "isntance of main");
        }
        if (context instanceof MessengerActivity){
            Log.d(TAG, "instance of messenger");
        }
        this.context = context;
        this.usbReceiver = new UsbReceiver();
        this.usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        this.baudRate = baudRate;
        this.isOpened = false;
        this.vendorIds = new ArrayList<>();
        this.vendorIds.add(9025);
        this.bytesReceived = new ArrayList<>();
        this.delimiter = DEFAULT_DELIMITER;
    }

    public void setArduinoListener(CustomArduinoListener listener) {
        this.listener = listener;

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        intentFilter.addAction(ACTION_USB_PERMISSION);
        context.registerReceiver(usbReceiver, intentFilter);

        lastArduinoAttached = getAttachedArduino();
        if (lastArduinoAttached != null && listener != null) {
            listener.onArduinoAttached(lastArduinoAttached);
        }
    }

    public void unsetArduinoListener() {
        this.listener = null;
    }

    public void open(UsbDevice device) {
        PendingIntent permissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        context.registerReceiver(usbReceiver, filter);
        usbManager.requestPermission(device, permissionIntent);
    }

    public void reopen() {
        open(lastArduinoAttached);
    }

    public void close() {
        if (serialPort != null) {
            serialPort.close();
        }
        if (connection != null) {
            connection.close();
        }

        isOpened = false;
        context.unregisterReceiver(usbReceiver);
    }

    public void stateSwitched() {
        Log.d(TAG, "stateSwitched called in customarduino, setting flag to false");
        isFlagDetected = false;
        bytesReceived.clear();
    }

    public void send(byte[] bytes) {

        if (serialPort != null) {
            int index = 0;
            int chunk = 1;
            int framesize = 127 - 9;

            Log.d(TAG, "SENDING array of size: " + bytes.length);
            while (bytes.length - index > framesize) {
                Log.d(TAG, "Sending chunk nr: " + chunk);


                byte[] partialArray = Arrays.copyOfRange(bytes, index, index + framesize - 1);

                ByteBuffer partialArrWithCarriageReturn = allocate(partialArray.length + 1);
                partialArrWithCarriageReturn.put(partialArray);
                partialArrWithCarriageReturn.put((byte) '\r');
                Log.d(TAG, "length is: " + partialArrWithCarriageReturn.array().length);
                Log.d(TAG, Arrays.toString(partialArrWithCarriageReturn.array()));
                serialPort.write(partialArrWithCarriageReturn.array());
                index = index + framesize;
                chunk++;
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Log.d(TAG, "Error sleeping: " + String.valueOf(e));
                }
            }
            Log.d(TAG, "Sending final range");
            Log.d(TAG, "--------------------");
            byte[] finalArray = Arrays.copyOfRange(bytes, index, bytes.length);
            Log.d(TAG, Arrays.toString(finalArray));

            Log.d(TAG, "last Element in array is:" + finalArray[finalArray.length - 1]);
            serialPort.write(finalArray);
        }
    }

    public void setDelimiter(byte delimiter) {
        this.delimiter = delimiter;
    }

    public void setBaudRate(int baudRate) {
        this.baudRate = baudRate;
    }

    public void addVendorId(int id) {
        vendorIds.add(id);
    }

    private class UsbReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            UsbDevice device;
            if (intent.getAction() != null) {
                switch (intent.getAction()) {
                    case UsbManager.ACTION_USB_DEVICE_ATTACHED:
                        device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                        if (hasId(device.getVendorId())) {
                            lastArduinoAttached = device;
                            if (listener != null) {
                                listener.onArduinoAttached(device);
                            }
                        }
                        break;
                    case UsbManager.ACTION_USB_DEVICE_DETACHED:
                        device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                        if (hasId(device.getVendorId())) {
                            if (listener != null) {
                                listener.onArduinoDetached();
                            }
                        }
                        break;
                    case ACTION_USB_PERMISSION:
                        if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                            device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                            if (hasId(device.getVendorId())) {
                                connection = usbManager.openDevice(device);
                                serialPort = UsbSerialDevice.createUsbSerialDevice(device, connection);
                                if (serialPort != null) {
                                    if (serialPort.open()) {
                                        serialPort.setBaudRate(baudRate);
                                        serialPort.setDataBits(UsbSerialInterface.DATA_BITS_8);
                                        serialPort.setStopBits(UsbSerialInterface.STOP_BITS_1);
                                        serialPort.setParity(UsbSerialInterface.PARITY_NONE);
                                        serialPort.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
                                        serialPort.read(CustomArduino.this);

                                        isOpened = true;

                                        if (listener != null) {
                                            listener.onArduinoOpened();
                                        }
                                    }
                                }
                            }
                        } else if (listener != null) {
                            listener.onUsbPermissionDenied();
                        }
                        break;
                }
            }
        }
    }

    private UsbDevice getAttachedArduino() {
        HashMap<String, UsbDevice> map = usbManager.getDeviceList();
        for (UsbDevice device : map.values()) {
            if (hasId(device.getVendorId())) {
                return device;
            }
        }
        return null;
    }

    private List<Integer> indexOf(byte[] bytes, byte b) {
        List<Integer> idx = new ArrayList<>();
        for (int i = 0; i < bytes.length; i++) {
            if (bytes[i] == b) {
                idx.add(i);
            }
        }
        return idx;
    }

    private List<Byte> toByteList(byte[] bytes) {
        List<Byte> list = new ArrayList<>();
        for (byte b : bytes) {
            list.add(b);
        }
        return list;
    }

    private byte[] toByteArray(List<Byte> bytes) {
        byte[] array = new byte[bytes.size()];
        for (int i = 0; i < bytes.size(); i++) {
            array[i] = bytes.get(i);
        }
        return array;
    }

    int receiveCounter = 0;

    boolean isFlagDetected = false;
    int flagDetectCounter = 0;

    int counter = 1;

    boolean bufferingInProgress = false;

    public enum messengerState {
        no_state,
        ranging_state,
        text_state,
        image_state
    }


    int lentext = 0;
    int lenImage = 0;
    byte[] lenImageArr = new byte[4];
    byte[] distanceArr = new byte[4];
    int receivedSize = 0;


    messengerState MESSENGER_STATE = messengerState.no_state;

    @Override
    public void onReceivedData(byte[] bytes) {

        if (bytes.length > 0) {
            Log.d(TAG, "Something received. Length is " + bytes.length);
            //Log.d(TAG, "Some frame received. Length is: " + String.valueOf(bytes.length));
            bytesReceived.addAll(toByteList(bytes));
            receivedSize += bytes.length;

            if (!isFlagDetected) {
                // LOOK FOR STATE CHANGES.
                if ((char) bytes[0] == 't') {
                    // a text is found. If no state is initiated. Start text state.
                    MESSENGER_STATE = messengerState.text_state;
                    Log.d(TAG, "Buffering a new text message");
                } else if ((char) bytes[0] == 'i') {
                    MESSENGER_STATE = messengerState.text_state;
                    Log.d(TAG, "Buffering a new image message");
                } else if ((char) bytes[0] == 'D') {
                    MESSENGER_STATE = messengerState.ranging_state;
                }
                isFlagDetected = true;
            } else {
                switch (MESSENGER_STATE) {
                    case image_state:
                        if (counter > 0 && counter < 4) {
                            lenImageArr[counter - 1] = bytes[counter];
                        } else if (counter >= 4) {
                            lenImage = ByteBuffer.wrap(lenImageArr).order(ByteOrder.LITTLE_ENDIAN).getInt();
                            Log.d(TAG, "The length of image: " + lenImage);
                            bufferingInProgress = true;
                        }
                        break;

                    case text_state:
                        if (counter == 1) {
                            lentext = (int) bytes[1];
                            Log.d(TAG, "The length of the text is: " + lentext);
                            bufferingInProgress = true;
                        }
                        break;

                    case ranging_state:

                        if (receivedSize >= 9) {
                            listener.onArduinoMessage(toByteArray(bytesReceived.subList(0, 8)));
                            bytesReceived = bytesReceived.subList(9, bytesReceived.size()-1);
                            receivedSize = receivedSize - 9;
                        }

                        if (counter == 1 && ((char) bytes[counter] == 'S')) {
                            // distance can be found at index 3 to 6.
                            bytesReceived.addAll(toByteList(bytes));

                            if (counter >= 9)
                                bufferingInProgress = true;
                        }
                        break;
                }
                counter++;
            }
        } else if (MESSENGER_STATE == messengerState.text_state && bufferingInProgress) {
            for (int i = 0; i < bytes.length; i++) {
                if ((char) bytes[i] == '>') {
                    // the end of the array is found.
                    bufferingInProgress = false;
                }
            }
        }
        if (bufferingInProgress) {
            if (MESSENGER_STATE == messengerState.ranging_state) {
                for (int i = 0; i < bytes.length; i++) {

                    if (counter > 2 && counter < 7) {
                        distanceArr[4] = bytes[counter];
                    } else if (counter == 6) {
                        Log.d(TAG, "Distance found: " + distanceArr.toString());
                        listener.onArduinoMessage(toByteArray(bytesReceived.subList(0, bytesReceived.size() - 1)));
                        MESSENGER_STATE = messengerState.no_state;
                        counter = 1;
                        lentext = 0;
                        lenImage = 0;
                    }

                    counter++;
                }
            } else if (MESSENGER_STATE == messengerState.text_state) {

                if (lentext <= 2) {
                    // The entire text is buffered.
                    Log.d(TAG, "The entire text is buffered");
                    listener.onArduinoMessage(toByteArray(bytesReceived.subList(0, bytesReceived.size() - 1)));
                    MESSENGER_STATE = messengerState.no_state;

                    counter = 1;
                    lentext = 0;
                    lenImage = 0;
                } else {
                    lentext -= bytes.length;
                    bytesReceived.addAll(toByteList(bytes));
                    counter += bytes.length;

                }
            } else if (MESSENGER_STATE == messengerState.image_state) {
                if (lenImage <= 5) {
                    // The entire text is buffered.
                    Log.d(TAG, "The entire image is buffered");
                    Log.d(TAG, "Image size is: " + counter);
                    listener.onArduinoMessage(toByteArray(bytesReceived.subList(0, bytesReceived.size() - 1)));
                    MESSENGER_STATE = messengerState.no_state;

                    counter = 1;
                    lentext = 0;
                    lenImage = 0;

                } else {
                    lentext -= bytes.length;
                    bytesReceived.addAll(toByteList(bytes));
                    counter += bytes.length;

                }
            }
        }
    }


    public boolean isOpened() {
        return isOpened;
    }

    private boolean hasId(int id) {
        Log.i(getClass().getSimpleName(), "Vendor id : " + id);
        for (int vendorId : vendorIds) {
            if (vendorId == id) {
                return true;
            }
        }
        return false;
    }
}
