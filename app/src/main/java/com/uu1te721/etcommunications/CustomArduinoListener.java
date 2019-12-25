package com.uu1te721.etcommunications;

import android.hardware.usb.UsbDevice;

public interface CustomArduinoListener {
    void onArduinoAttached(UsbDevice device);
    void onArduinoDetached();
    void onArduinoMessage(byte[] bytes);
    void onArduinoOpened();
    void onUsbPermissionDenied();
}
