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

    private String mContext = "";

    private void init(Context context, int baudRate) {
        Log.d(TAG, "Initialize arduino from context: " + context);

        if (context instanceof MainActivity) {
            Log.d(TAG, "isntance of main");
            mContext = "Main";
        }
        if (context instanceof MessengerActivity) {
            Log.d(TAG, "instance of messenger");
            mContext = "Messenger";
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

    public void resetArduinoState() {
        Log.d(TAG, "resetArduinoState called in customarduino, setting flag to false");
        isStartFlagDetected = false;
        isEndFlagDetected = false;
        flagDetectCounter = 0;
        previousSize = 0;
        MESSENGER_STATE = messengerState.no_state;
        bytesReceived.clear();
    }

    public void send(byte[] bytes) {

        if (serialPort != null) {
            int index = 0;
            int framesize = 127 - 11;

            Log.d(TAG, "SENDING array of size: " + bytes.length);
            while (bytes.length - index > framesize) {

                byte[] partialArray = Arrays.copyOfRange(bytes, index, index + framesize - 1);

                ByteBuffer partialArrWithCarriageReturn = allocate(partialArray.length + 1);
                partialArrWithCarriageReturn.put(partialArray);
                partialArrWithCarriageReturn.put((byte) '\r');
                serialPort.write(partialArrWithCarriageReturn.array());
                index = index + framesize;

                try {
                    Thread.sleep(100);
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

    boolean isStartFlagDetected = false;
    boolean isEndFlagDetected = false;
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
    int previousSize = 0;


    messengerState MESSENGER_STATE = messengerState.no_state;

    @Override
    public void onReceivedData(byte[] bytes) {


        if (bytes.length > 0) {
            Log.d(TAG, "Context of: " + mContext + ", onReceived data: " + Arrays.toString(bytes));
            //Log.d(TAG, "Some frame received. Length is: " + String.valueOf(bytes.length));
            bytesReceived.addAll(toByteList(bytes));

            if (!isStartFlagDetected) {
                // LOOK FOR STATE CHANGES.
                if ((char) bytes[0] == 't') {
                    // a text is found. If no state is initiated. Start text state.
                    MESSENGER_STATE = messengerState.text_state;
                    isStartFlagDetected = true;
                    Log.d(TAG, "Buffering a new text message");
                } else if ((char) bytes[0] == 'i') {
                    MESSENGER_STATE = messengerState.text_state;
                    isStartFlagDetected = true;
                    Log.d(TAG, "Buffering a new image message");
                } else if ((char) bytes[0] == 'D') {
                    MESSENGER_STATE = messengerState.ranging_state;
                    isStartFlagDetected = true;
                }
            }

            if (MESSENGER_STATE == messengerState.image_state || MESSENGER_STATE == messengerState.text_state) {
                for (int i = previousSize; i < bytesReceived.size(); i++) {
                    if (bytesReceived.get(i) == '>') {
                        isEndFlagDetected = true;
                        flagDetectCounter++;
                        Log.d(TAG, "Detected flag: " + flagDetectCounter + " times");

                        if (flagDetectCounter >= 2) {
                            Log.d(TAG, "Sending to listener: " + bytesReceived.subList(0, bytesReceived.size() - 3));
                            listener.onArduinoMessage(toByteArray(bytesReceived.subList(0, bytesReceived.size() - 3)));
                            resetArduinoState();
                        }
                    } else {
                        Log.d(TAG, "False alarm, keep working");
                        isEndFlagDetected = false;
                        flagDetectCounter = 0;
                    }
                }
                previousSize = bytes.length;
            } else if (MESSENGER_STATE == messengerState.ranging_state) {
                if (bytesReceived.size() >= 11) {
                    listener.onArduinoMessage(toByteArray(bytesReceived.subList(0, 7)));
                    Log.d(TAG, "sent to listener: " + bytesReceived.subList(0, 7));
                    Log.d(TAG, "bytesReceived before sublist: " + bytesReceived);
                    bytesReceived = bytesReceived.subList(9, bytesReceived.size() - 1);
                    Log.d(TAG, "bytesReceived after sublist: " + bytesReceived);
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
