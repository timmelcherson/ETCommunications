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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static com.uu1te721.etcommunications.utils.Constants.ACTION_USB_PERMISSION;
import static com.uu1te721.etcommunications.utils.Constants.TAG;


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


    public void send(byte[] bytes) {

        if (serialPort != null) {
            int index = 0;
            int chunk = 1;
            Log.d(TAG, "SENDING array of size: " + bytes.length);
            while (bytes.length - index > 125) {
                Log.d(TAG, "Sending chunk: " + chunk);
                byte[] partialArray = Arrays.copyOfRange(bytes, index, index + 125);
                Log.d(TAG, Arrays.toString(partialArray));
                serialPort.write(partialArray);
                index = index + 125;
                chunk++;
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

    @Override
    public void onReceivedData(byte[] bytes) {

        if (bytes.length != 0) {
            Log.d(TAG, "RECEIVED: " + Arrays.toString(bytes));

            bytesReceived.addAll(toByteList(bytes));


            int length = bytesReceived.size();
            Log.d(TAG, "bytesReceived current size: " + length);
            Log.d(TAG, "bytesReceived current values: " + Arrays.toString(toByteArray(bytesReceived)));

            for (int i = 0; i < bytes.length; i++) {
                receiveCounter++;
                if (bytes[i] == '>') {
                    if (!isFlagDetected) {
                        isFlagDetected = true;
                        flagDetectCounter++;
                    } else {
                        flagDetectCounter++;
                    }
                    if (flagDetectCounter >= 3) {
                        Log.d(TAG, "TERMINATE CHARACTER IS HERE, TOTAL ARRAY RECEIVED: " + bytesReceived.toString());
                        Log.d(TAG, "IT HAS LENGTH: " + bytesReceived.size());
                        if (listener != null) {
                            Log.d(TAG, "Received in total (excluding flags): " + String.valueOf(receiveCounter - 1));
                            Log.d(TAG, Arrays.toString(toByteArray(bytesReceived)));
                            listener.onArduinoMessage(toByteArray(bytesReceived.subList(0, bytesReceived.size() - 3)));
                            Log.d(TAG, "sent to listener");
                            bytesReceived.clear();
                            receiveCounter = 0;
                        }
                    }

                } else {
                    isFlagDetected = false;
                    flagDetectCounter = 0;
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
