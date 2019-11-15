package com.uu1te721.etcommunications;

import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.bluetooth.BluetoothClass;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.bluetooth.BluetoothClass.Device;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.linroid.filtermenu.library.FilterMenu;
import com.linroid.filtermenu.library.FilterMenuLayout;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private FilterMenuLayout mLayout;

    public static final String TAG = "TAG";
    private static final String ACTION_USB_PERMISSION = "com.uu1te721.etcommunications.USB_PERMISSION";

    private TextView tv3, tv4, tv5, tv6, tv7;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLayout = (FilterMenuLayout) findViewById(R.id.filter_menu);
        tv3 = findViewById(R.id.device_id_tv);
        tv4 = findViewById(R.id.vendor_tv);
        tv5 = findViewById(R.id.product_tv);
        tv6 = findViewById(R.id.class_tv);
        tv7 = findViewById(R.id.sublcass_tv);


        buildFilterMenu();
//        findUsbDevices();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mLayout.setVisibility(View.VISIBLE);
    }


    private void buildFilterMenu() {
        final FilterMenu menu = new FilterMenu.Builder(this)
                .inflate(R.menu.filter_menu_items)//inflate  menu resource
                .attach(mLayout)
                .withListener(new FilterMenu.OnMenuChangeListener() {
                    @Override
                    public void onMenuItemClick(View view, int position) {
                        switch (position) {
                            case 0:
                                Toast.makeText(MainActivity.this, "CLICK 1", Toast.LENGTH_SHORT).show();
                                break;

                            case 1:
                                Intent intent = new Intent(MainActivity.this, SendMessageActivity.class);
                                startActivity(intent);
                                break;
                        }
                    }

                    @Override
                    public void onMenuCollapse() {
                    }

                    @Override
                    public void onMenuExpand() {
                    }
                })
                .build();
    }


    public void findUsbDevices(View view) {

        Log.d(TAG, "Starting findUsbDevices");
        UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();

        Log.d(TAG, "DeviceList: " + deviceList);
        TextView tv1 = findViewById(R.id.device_list);
        tv1.setText(deviceList.toString());
        while (deviceIterator.hasNext()) {

            UsbDevice device = deviceIterator.next();

            Log.d(TAG, "Actionstring: " + ACTION_USB_PERMISSION);
            TextView tv2 = findViewById(R.id.action_string);
            tv2.setText(ACTION_USB_PERMISSION);

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


            tv3.setText(String.valueOf(deviceID));
            tv4.setText(String.valueOf(vendorId));
            tv5.setText(String.valueOf(productId));
            tv6.setText(String.valueOf(deviceClass));
            tv7.setText(String.valueOf(deviceSubclass));
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
                    UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {

                            boolean granted = intent.getExtras().getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED);

                            if (granted) {

                            }
                        }
                    } else {
                        Log.d(TAG, "permission denied for device " + device);
                    }
                }
//                boolean granted =
//                        intent.getExtras().getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED);
//                if (granted) {
//                    connection = usbManager.openDevice(device);
//                    serialPort = UsbSerialDevice.createUsbSerialDevice(device, connection);
//                    if (serialPort != null) {
//                        if (serialPort.open()) { //Set Serial Connection Parameters.
//                            setUiEnabled(true); //Enable Buttons in UI
//                            serialPort.setBaudRate(9600);
//                            serialPort.setDataBits(UsbSerialInterface.DATA_BITS_8);
//                            serialPort.setStopBits(UsbSerialInterface.STOP_BITS_1);
//                            serialPort.setParity(UsbSerialInterface.PARITY_NONE);
//                            serialPort.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
//                            serialPort.read(mCallback); //
//                            tvAppend(textView,"Serial Connection Opened!\n");
//
//                        } else {
//                            Log.d("SERIAL", "PORT NOT OPEN");
//                        }
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

}
