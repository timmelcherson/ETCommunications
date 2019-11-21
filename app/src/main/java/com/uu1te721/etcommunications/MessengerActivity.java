package com.uu1te721.etcommunications;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.felhr.usbserial.UsbSerialDevice;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import me.aflak.arduino.Arduino;
import me.aflak.arduino.ArduinoListener;

public class MessengerActivity extends AppCompatActivity{

    public static final String TAG = "TAG";
    private static final String ACTION_USB_PERMISSION = "com.uu1te721.etcommunications.USB_PERMISSION";
    private static final String ACTION_USB_NOT_SUPPORTED = "com.uu1te721.etcommunications.USB_NOT_SUPPORTED";
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
    private Arduino mArduino;
    private boolean isMessageReceived = false;


    // for taking picutre
    private static final int REQUEST_IMAGE_CAPTUTRE = 101;

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
        mBeginBtn = findViewById(R.id.camera_btn);
        mWriteMessageEt = findViewById(R.id.write_message_et);
        mMessageFeed = findViewById(R.id.message_feed_layout);

        mArduino = new Arduino(this);
        mArduino.addVendorId(10755);
        mArduino.addVendorId(9025);


        buildRecyclerView();
    }


    @Override
    protected void onStart() {
        super.onStart();
        setupArduino();
    }

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

                if (!isMessageReceived) {
                    isMessageReceived = true;
                    receiveMessage(data);
                }
            }

            @Override
            public void onArduinoOpened() {
                // you can start the communication
                String str = "Hello Arduino";
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

    public void sendMessage(View view) {
        // This method called when send button pressed.
        String msg = "";

        // Sending to Arduino
        if (!mWriteMessageEt.getText().toString().equals("")) {
            msg = mWriteMessageEt.getText().toString();
            if (usbService != null) { // if UsbService was correctly binded, Send data
                usbService.write(msg.getBytes());
            }
        }

        // Display sent message
        MessageCard msgCard = new MessageCard(msg, "sent");
        mMessageCardList.add(msgCard);
        mMessengerAdapter.notifyDataSetChanged();
        lm.smoothScrollToPosition(mMessageFeed, null, mMessageCardList.size() - 1);
    }

    public void sendMessage(Bitmap multimediaMessage){
        // Called when picture taken
        if (usbService != null) { // if UsbService was correctly binded, Send data
            // TODO: Send Bitmap over USB
        }

        // Displaying multimedia object (Only support image for now).
        MessageCard msgCard = new MessageCard(multimediaMessage, "sent");
        mMessageCardList.add(msgCard);
        mMessengerAdapter.notifyDataSetChanged();
    }

    private void buildRecyclerView() {
        lm = new LinearLayoutManager(this);
        mMessageFeed.setLayoutManager(lm);
        mMessengerAdapter = new MessengerRecyclerViewAdapter(this, mMessageCardList);
        mMessageFeed.setAdapter(mMessengerAdapter);
    }

    private void setUiEnabled(boolean isEnabled) {
        mBeginBtn.setEnabled(!isEnabled);
        mSendBtn.setEnabled(isEnabled);
    }


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

        lm.smoothScrollToPosition(mMessageFeed, null, mMessageCardList.size());
    }


    @Override
    public boolean onSupportNavigateUp() {
        super.onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void takePicture(View view) {
        Intent imageTakeIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (imageTakeIntent.resolveActivity(getPackageManager()) != null)
        {
        startActivityForResult(imageTakeIntent, REQUEST_IMAGE_CAPTUTRE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode == REQUEST_IMAGE_CAPTUTRE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            // HERE put a code that sends the picture and displays in the messenger.
            sendMessage(imageBitmap);
            Toast.makeText(this, "Picture Taken", Toast.LENGTH_SHORT).show();
        }
    }
}
