package com.uu1te721.etcommunications;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;
import com.felhr.utils.ProtocolBuffer;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import me.aflak.arduino.Arduino;
import me.aflak.arduino.ArduinoListener;

public class MessengerActivity extends AppCompatActivity implements View.OnClickListener {

    private Button mSendBtn, mBeginBtn;
    private EditText mWriteMessageEt;
    private RecyclerView mMessageFeed;
    private MessengerRecyclerViewAdapter mMessengerAdapter;
    private LinearLayoutManager lm;

    private UsbDeviceConnection connection;
    private UsbDevice device;
    private UsbManager usbManager;
    private UsbSerialDevice serialPort;

    private List<String> mMessageList = new ArrayList<>();
    private List<MessageCard> mMessageCardList = new ArrayList<>();

    //    private MyHandler mHandler;
    private UsbService usbService;

    public static final String TAG = "TAG";
    private static final String ACTION_USB_PERMISSION = "com.uu1te721.etcommunications.USB_PERMISSION";
    private static final String ACTION_USB_NOT_SUPPORTED = "com.uu1te721.etcommunications.USB_NOT_SUPPORTED";


//    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            switch (intent.getAction()) {
//                case UsbService.ACTION_USB_PERMISSION_GRANTED: // USB PERMISSION GRANTED
//                    Toast.makeText(context, "USB Ready", Toast.LENGTH_SHORT).show();
//                    break;
//                case UsbService.ACTION_USB_PERMISSION_NOT_GRANTED: // USB PERMISSION NOT GRANTED
//                    Toast.makeText(context, "USB Permission not granted", Toast.LENGTH_SHORT).show();
//                    break;
//                case UsbService.ACTION_NO_USB: // NO USB CONNECTED
//                    Toast.makeText(context, "No USB connected", Toast.LENGTH_SHORT).show();
//                    break;
//                case UsbService.ACTION_USB_DISCONNECTED: // USB DISCONNECTED
//                    Toast.makeText(context, "USB disconnected", Toast.LENGTH_SHORT).show();
//                    break;
//                case UsbService.ACTION_USB_NOT_SUPPORTED: // USB NOT SUPPORTED
//                    Toast.makeText(context, "USB device not supported", Toast.LENGTH_SHORT).show();
//                    break;
//            }
//        }
//    };
//
//    private final ServiceConnection usbConnection = new ServiceConnection() {
//        @Override
//        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
//            usbService = ((UsbService.UsbBinder) arg1).getService();
//            usbService.setHandler(mHandler);
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName arg0) {
//            usbService = null;
//        }
//    };

    private Arduino mArduino;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messenger);

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
//        mWrittenMsg = findViewById(R.id.written_msg_tv);
        mMessageFeed = findViewById(R.id.message_feed_layout);

        MessageCard card1 = new MessageCard("Hello Friend", "received");
        MessageCard card2 = new MessageCard("Hello to youuuuuuuuuu", "sent");
        MessageCard card3 = new MessageCard("How u doin", "sent");
        MessageCard card4 = new MessageCard("amazin ty :)", "received");
        mMessageCardList.add(card1);
        mMessageCardList.add(card2);
        mMessageCardList.add(card3);
        mMessageCardList.add(card4);

//        mHandler = new MyHandler(this);

        mArduino = new Arduino(this);
        mArduino.addVendorId(10755);


//        usbManager = (UsbManager) getSystemService(MainActivity.USB_SERVICE);
//        setUiEnabled(false);
//
//        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
//        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
//        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
//        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
//        filter.addAction(ACTION_USB_NOT_SUPPORTED);
//        registerReceiver(usbReceiver, filter);

        mSendBtn.setOnClickListener(this);
        mBeginBtn.setOnClickListener(this);


        buildRecyclerView();
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.send_message_btn:
                sendMessage();
                break;

            case R.id.begin_btn:
//                findUsbDevices();
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        setupArduino();
    }

    private boolean isMessageReceived = false;

    private void setupArduino() {
        mArduino.setArduinoListener(new ArduinoListener() {
            @Override
            public void onArduinoAttached(UsbDevice device) {
                mArduino.open(device);
            }

            @Override
            public void onArduinoDetached() {
                // arduino detached from phone
            }

            @Override
            public void onArduinoMessage(byte[] bytes) {
                String data = null;

                try {
                    data = new String(bytes, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                Log.d(TAG, "bytes length: " + bytes.length);
                Log.d(TAG, "isMessageReceived: " + isMessageReceived);

                if (!isMessageReceived) {
                    Log.d(TAG, "onArduinoMessage: " + Arrays.toString(bytes));
                    isMessageReceived = true;
                    receiveMessage(data);
                    Log.d(TAG, "isMessageReceived: " + isMessageReceived);
                }
            }

            @Override
            public void onArduinoOpened() {
                // you can start the communication
                String str = "Hello Arduino !";
                mArduino.send(str.getBytes());
            }

            @Override
            public void onUsbPermissionDenied() {
                // Permission denied, display popup then
                mArduino.reopen();
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mArduino.unsetArduinoListener();
        mArduino.close();
    }

    private void sendMessage() {
        String msg = "";

//        serialPort.write(msg.getBytes());

        if (!mWriteMessageEt.getText().toString().equals("")) {
            msg = mWriteMessageEt.getText().toString();
            if (usbService != null) { // if UsbService was correctly binded, Send data
                usbService.write(msg.getBytes());
            }
        }
        MessageCard msgCard = new MessageCard(msg, "sent");
        mMessageCardList.add(msgCard);
        mMessengerAdapter.notifyDataSetChanged();
        lm.smoothScrollToPosition(mMessageFeed, null, mMessageCardList.size() - 1);
//        tvAppend(mWrittenMsg, msg);
    }

    private void buildRecyclerView() {
        lm = new LinearLayoutManager(this);
//        lm.setReverseLayout(true);
        mMessageFeed.setLayoutManager(lm);
        mMessengerAdapter = new MessengerRecyclerViewAdapter(this, mMessageCardList);
        mMessageFeed.setAdapter(mMessengerAdapter);
    }

    /*public void findUsbDevices() {

        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();

        if (!deviceList.isEmpty()) {

            boolean keep = true;

            for (Map.Entry<String, UsbDevice> item : deviceList.entrySet()) {

                device = item.getValue();

                if (device.getVendorId() == 9025 || device.getVendorId() == 10755) {
                    Toast.makeText(this, "Arduino connected", Toast.LENGTH_SHORT).show();
                    PendingIntent permissionIntent = PendingIntent.getBroadcast(this, 0, new
                            Intent(ACTION_USB_PERMISSION), 0);
                    usbManager.requestPermission(device, permissionIntent);
                    keep = false;
                } else {
                    connection = null;
                    device = null;
                }

                if (!keep) {
                    break;
                }
            }
        }
    }*/

    /*private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            Toast.makeText(context, "usbReceiver", Toast.LENGTH_SHORT).show();
            if (intent.getAction().equals(ACTION_USB_PERMISSION)) {

                device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    if (device != null) {

                        boolean granted = intent.getExtras().getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED);

                        if (granted) {
                            connection = usbManager.openDevice(device);
                            serialPort = UsbSerialDevice.createUsbSerialDevice(device, connection);

                            if (serialPort != null) {
                                if (serialPort.open()) {
                                    Log.d(TAG, "onReceive: setting Ui TRUE");
                                    setUiEnabled(true); //Enable Buttons in UI
                                    serialPort.setBaudRate(9600);
                                    serialPort.setDataBits(UsbSerialInterface.DATA_BITS_8);
                                    serialPort.setParity(UsbSerialInterface.PARITY_ODD);
                                    serialPort.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);

                                    try {
                                        serialPort.read(mCallback);
                                        Log.d(TAG, "Successfully read serialport");
                                    } catch (Exception e) {
                                        Log.e(TAG, "Error in reading serialport: " + e);
                                    }

//                                    tvAppend(mWrittenMsg, "Serial Connection Opened");
                                    Toast.makeText(context, "Serial Connection Opened!\n", Toast.LENGTH_SHORT).show();

                                } else {
                                    Log.d("SERIAL", "PORT NOT OPEN");
                                }
                            } else {
                                Log.d("SERIAL", "PORT IS NULL");
                            }
                        } else {
                            Log.d(TAG, "permission denied for device " + device);
                        }
                    }
                } else {
                    Log.d("SERIAL", "PERM NOT GRANTED");
                }

            } else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {
                findUsbDevices();
            }
//            else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
//                onClickStop(stopButton);
//            }
            else if (intent.getAction().equals(ACTION_USB_NOT_SUPPORTED)) {
                Intent newIntent = new Intent(ACTION_USB_NOT_SUPPORTED);
                context.sendBroadcast(newIntent);
            }
        }
    };*/

    private void setUiEnabled(boolean isEnabled) {
        mBeginBtn.setEnabled(!isEnabled);
        mSendBtn.setEnabled(isEnabled);
    }

//    public MessageCard mCurrentMsgCard;
//    private int mCurrentMsgCardPos;
//    private boolean mNewMessageAdded = false;
//    public String mIncomingMessage;
//    ProtocolBuffer buffer = new ProtocolBuffer(ProtocolBuffer.TEXT);
//    ProtocolBuffer buffer2 = new ProtocolBuffer(ProtocolBuffer.BINARY);

    /*UsbSerialInterface.UsbReadCallback mCallback = new UsbSerialInterface.UsbReadCallback() {

        //Defining a Callback which triggers whenever data is read.
        @Override
        public void onReceivedData(byte[] arg0) {

//            final MessageCard msgCard = new MessageCard("", "received");
//            mMessageCardList.add(msgCard);
//
//            if (!mNewMessageAdded) {
//                mCurrentMsgCardPos = mMessageCardList.indexOf(msgCard);
//                mNewMessageAdded = true;
//            }

//            Log.d(TAG, "arg0" + Arrays.toString(arg0));
//            if (!mNewMessageAdded) {
//                mCurrentMsgCard = new MessageCard("", "received");
//                mNewMessageAdded = true;
//            }
            byte[] buffer = new byte[128];
            boolean isOpen = serialPort.syncOpen();
            while (isOpen){
                int read = serialPort.syncRead(buffer, 0);
            }
            serialPort.syncClose();

//            if (arg0 != null) {
//                buffer.setDelimiter("\r\n");
//                buffer.appendData(arg0);
//            }
//            Log.d(TAG, "BUFFER TEXT: " + buffer.nextTextCommand());
//            Log.d(TAG, "BUFFER BINARY: " + Arrays.toString(buffer2.nextBinaryCommand()));
//            while (buffer.hasMoreCommands()) {
//                Log.d(TAG, "nextTextCommand: " + buffer.nextTextCommand());
//                mIncomingMessage = mIncomingMessage + buffer.nextTextCommand();
//            }
            messageReceived();


//            try {
//                data = new String(arg0, "UTF-8");
////                receiveMessage(data);
//                mCurrentMsgCard.setText(Arrays.toString(arg0));
//                messageReceived();
//
//            } catch (UnsupportedEncodingException e) {
//                e.printStackTrace();
//            }
        }
    };*/

    /*private void messageReceived() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MessageCard msg = new MessageCard(mIncomingMessage, "received");
                mMessageCardList.add(msg);
                mMessengerAdapter.notifyDataSetChanged();
            }
        });
        lm.smoothScrollToPosition(mMessageFeed, null, mMessageCardList.size() - 1);
    }*/

    private void receiveMessage(String msg) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MessageCard msgCard = new MessageCard(msg, "received");
                mMessageCardList.add(msgCard);
                mMessengerAdapter.notifyDataSetChanged();
                isMessageReceived = false;
            }
        });

        lm.smoothScrollToPosition(mMessageFeed, null, mMessageCardList.size() - 1);
    }

    /*private void tvAppend(TextView tv, CharSequence text) {
        final TextView ftv = tv;
        final CharSequence ftext = text;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ftv.append(ftext);
            }
        });
    }*/

    /*@Override
    public void onResume() {
        super.onResume();
        setFilters();  // Start listening notifications from UsbService
        startService(UsbService.class, usbConnection, null); // Start UsbService(if it was not started before) and Bind it
    }

    @Override
    protected void onPause() {
//        Toast.makeText(this, "onPause", Toast.LENGTH_SHORT).show();
        super.onPause();
        unregisterReceiver(mUsbReceiver);
        unbindService(usbConnection);
//        unregisterReceiver(usbReceiver);
    }

    @Override
    protected void onStop() {
        Toast.makeText(this, "onStop", Toast.LENGTH_SHORT).show();
        unregisterReceiver(mUsbReceiver);
        super.onStop();
    }

    private void setFilters() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbService.ACTION_USB_PERMISSION_GRANTED);
        filter.addAction(UsbService.ACTION_NO_USB);
        filter.addAction(UsbService.ACTION_USB_DISCONNECTED);
        filter.addAction(UsbService.ACTION_USB_NOT_SUPPORTED);
        filter.addAction(UsbService.ACTION_USB_PERMISSION_NOT_GRANTED);
        registerReceiver(mUsbReceiver, filter);
    }*/

    /*private void startService(Class<?> service, ServiceConnection serviceConnection, Bundle extras) {
        if (!UsbService.SERVICE_CONNECTED) {
            Intent startService = new Intent(this, service);
            if (extras != null && !extras.isEmpty()) {
                Set<String> keys = extras.keySet();
                for (String key : keys) {
                    String extra = extras.getString(key);
                    startService.putExtra(key, extra);
                }
            }
            startService(startService);
        }
        Intent bindingIntent = new Intent(this, service);
        bindService(bindingIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }*/

    /*
     * This handler will be passed to UsbService. Data received from serial port is displayed through this handler
     */
    /*private static class MyHandler extends Handler {
        private final WeakReference<MessengerActivity> mActivity;

        public MyHandler(MessengerActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UsbService.MESSAGE_FROM_SERIAL_PORT:
                    String data = (String) msg.obj;
                    Toast.makeText(mActivity.get(), "MSG: " + data, Toast.LENGTH_SHORT).show();
                    mActivity.get().receiveMessage(data);
                    break;
                case UsbService.CTS_CHANGE:
                    Toast.makeText(mActivity.get(), "CTS_CHANGE",Toast.LENGTH_LONG).show();
                    break;
                case UsbService.DSR_CHANGE:
                    Toast.makeText(mActivity.get(), "DSR_CHANGE",Toast.LENGTH_LONG).show();
                    break;
            }
        }
    }*/


    @Override
    public boolean onSupportNavigateUp() {
        super.onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
