package com.uu1te721.etcommunications;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
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

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public static final String TAG = "TAG";
    private static final String ACTION_USB_PERMISSION = "com.uu1te721.etcommunications.USB_PERMISSION";
    private static final String ACTION_USB_NOT_SUPPORTED = "com.uu1te721.etcommunications.USB_NOT_SUPPORTED";

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

        usbManager = (UsbManager) getSystemService(MainActivity.USB_SERVICE);
        setUiEnabled(false);

        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        filter.addAction(ACTION_USB_NOT_SUPPORTED);
        registerReceiver(usbReceiver, filter);

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
                findUsbDevices();
                break;
        }
    }

    private void sendMessage() {
        String msg = mWriteMessageEt.getText().toString();
        MessageCard msgCard = new MessageCard(msg, "sent");
        mMessageCardList.add(msgCard);
        serialPort.write(msg.getBytes());
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

    public void findUsbDevices() {

        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();

        if (!deviceList.isEmpty()) {

            boolean keep = true;

            for (Map.Entry<String, UsbDevice> item : deviceList.entrySet()) {

                device = item.getValue();

                if (device.getVendorId() == 9025) {
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
    }

    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {

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
                                    serialPort.setStopBits(UsbSerialInterface.STOP_BITS_1);
                                    serialPort.setParity(UsbSerialInterface.PARITY_NONE);
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
    };

    private void setUiEnabled(boolean isEnabled) {
        mBeginBtn.setEnabled(!isEnabled);
        mSendBtn.setEnabled(isEnabled);
//        mWrittenMsg.setEnabled(isEnabled);
    }

    private int mCurrentMsgCardPos;
    private boolean mNewMessageAdded;

    UsbSerialInterface.UsbReadCallback mCallback = new UsbSerialInterface.UsbReadCallback() {

        //Defining a Callback which triggers whenever data is read.
        @Override
        public void onReceivedData(byte[] arg0) {
            final MessageCard msgCard = new MessageCard("", "received");
            mMessageCardList.add(msgCard);

            if (!mNewMessageAdded) {
                mCurrentMsgCardPos = mMessageCardList.indexOf(msgCard);
                mNewMessageAdded = true;
            }

            String data = null;
            try {
                data = new String(arg0, "UTF-8");
                receiveMessage(data);
//                int n = serialPort.syncRead(arg0, 0);
//                if (n > 0) {
//                    byte[] received = new byte[n];
//
//                }
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        receiveMessage(data);
//                    }
//                });
//                tvAppend(mWrittenMsg, data);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }


    };

//    public void insertMessage(String msg) {
//        new InsertMessageAsyncTask().;
//    }
//
//    private static class InsertMessageAsyncTask extends AsyncTask<Trail, Void, Void> {
//        @Override
//        protected Void doInBackground(final Trail... trails) {
//            mTrailDao.insert(trails[0]);
//            return null;
//        }
//    }

    private void receiveMessage(String msg) {
        final CharSequence finalMsg = msg;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (msg != null || !msg.equals(""))
                mMessageCardList.get(mCurrentMsgCardPos).setText(mMessageCardList.get(mCurrentMsgCardPos).getText() + msg);
                mMessengerAdapter.notifyDataSetChanged();
            }
        });
        lm.smoothScrollToPosition(mMessageFeed, null, mMessageCardList.size() - 1);
    }

    private void tvAppend(TextView tv, CharSequence text) {
        final TextView ftv = tv;
        final CharSequence ftext = text;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ftv.append(ftext);
            }
        });
    }

    @Override
    protected void onPause() {
//        Toast.makeText(this, "onPause", Toast.LENGTH_SHORT).show();
        super.onPause();
//        unregisterReceiver(usbReceiver);
    }

    @Override
    protected void onStop() {
        Toast.makeText(this, "onStop", Toast.LENGTH_SHORT).show();
        unregisterReceiver(usbReceiver);
        super.onStop();
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
