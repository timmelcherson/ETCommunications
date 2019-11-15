package com.uu1te721.etcommunications;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;

public class SendMessageActivity extends AppCompatActivity implements View.OnClickListener {

    private Button mSendBtn, mBeginBtn;
    private EditText mWriteMessageEt;
    private TextView mWrittenMsg;

    private UsbDeviceConnection connection;
    private UsbDevice device;
    private UsbManager usbManager;
    private UsbSerialDevice serialPort;

    public static final String TAG = "TAG";
    private static final String ACTION_USB_PERMISSION = "com.uu1te721.etcommunications.USB_PERMISSION";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_message);

        // Set up toolbar as action bar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(null);

        // Add back button to toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mSendBtn = findViewById(R.id.send_message_btn);
        mBeginBtn = findViewById(R.id.begin_btn);
        mWriteMessageEt = findViewById(R.id.write_message_et);

        mSendBtn.setOnClickListener(this);
        mBeginBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.send_message_btn:
                sendMessage();
                break;

            case R.id.begin_btn:
                findUsbDevices();
                break;
        }
    }

    private void sendMessage() {
        String msg = mWriteMessageEt.getText().toString();
        Toast.makeText(this, "hej in bytes: " + "hej".getBytes(), Toast.LENGTH_SHORT).show();

        serialPort.write("hej".getBytes());
    }

    public void findUsbDevices() {

        Toast.makeText(this, "FindUsbDevices", Toast.LENGTH_SHORT).show();

        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();

        while (deviceIterator.hasNext()) {

            device = deviceIterator.next();

            PendingIntent permissionIntent = PendingIntent.getBroadcast(this, 0, new
                    Intent(ACTION_USB_PERMISSION), 0);

            usbManager.requestPermission(device, permissionIntent);

            IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
            registerReceiver(usbReceiver, filter);

            String model = device.getDeviceName();

            int deviceID = device.getDeviceId();
            int vendorId = device.getVendorId();
            int productId = device.getProductId();
            int deviceClass = device.getDeviceClass();
            int deviceSubclass = device.getDeviceSubclass();
        }
//        UsbManager usbManager = new UsbManager();
//        HashMap<String, UsbDevice> usbDevices = usbManager.getDeviceList();
//        if (!usbDevices.isEmpty()) {
//            boolean keep = true;
//            for (Map.Entry<String, UsbDevice> entry : usbDevices.entrySet()) {
//                device = entry.getValue();
//                int deviceVID = device.getVendorId();
//                if (deviceVID == 0x2341)//Arduino Vendor ID
//                {
//                    PendingIntent pi = PendingIntent.getBroadcast(this, 0,
//                            new Intent(ACTION_USB_PERMISSION), 0);
//                    usbManager.requestPermission(device, pi);
//                    keep = false;
//                } else {
//                    connection = null;
//                    device = null;
//                }
//
//                if (!keep)
//                    break;
//            }
//        }
    }

    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            Toast.makeText(context, "usbReceiver", Toast.LENGTH_SHORT).show();
            if (intent.getAction().equals(ACTION_USB_PERMISSION)) {

                synchronized (this) {
                    device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {

                            boolean granted = intent.getExtras().getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED);

                            if (granted) {
                                connection = usbManager.openDevice(device);
                                serialPort = UsbSerialDevice.createUsbSerialDevice(device, connection);

                                if (serialPort != null) {
                                    if (serialPort.open()) {
//                                        setUiEnabled(true); //Enable Buttons in UI
                                        serialPort.setBaudRate(9600);
                                        serialPort.setDataBits(UsbSerialInterface.DATA_BITS_8);
                                        serialPort.setStopBits(UsbSerialInterface.STOP_BITS_1);
                                        serialPort.setParity(UsbSerialInterface.PARITY_NONE);
                                        serialPort.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
                                        serialPort.read(mCallback);
                                        tvAppend(mWrittenMsg, "Serial Connection Opened");
                                        Toast.makeText(context, "Serial Connection Opened!\n", Toast.LENGTH_SHORT).show();

                                    } else {
                                        Log.d("SERIAL", "PORT NOT OPEN");
                                    }
                                }
                            }
                        }
                    } else {
                        Log.d(TAG, "permission denied for device " + device);
                    }
                }
//                    } else {
//                        Log.d("SERIAL", "PORT IS NULL");
//                    }
//                } else {
//                    Log.d("SERIAL", "PERM NOT GRANTED");
//                }
            }
//            else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {
//                onClickStart(startButton);
//            } else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
//                onClickStop(stopButton);
//            }
        }
    };

    UsbSerialInterface.UsbReadCallback mCallback = new UsbSerialInterface.UsbReadCallback() {

        //Defining a Callback which triggers whenever data is read.
        @Override
        public void onReceivedData(byte[] arg0) {
            String data = null;
            try {
                data = new String(arg0, "UTF-8");
                data.concat("/n");
                tvAppend(mWrittenMsg, data);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    };

    private void tvAppend(TextView tv, CharSequence text) {
        runOnUiThread(() -> tv.append(text));
    }

    @Override
    public boolean onSupportNavigateUp() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
