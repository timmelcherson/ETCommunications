package com.uu1te721.etcommunications;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.felhr.usbserial.UsbSerialDevice;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.aflak.arduino.Arduino;
import me.aflak.arduino.ArduinoListener;

import static com.uu1te721.etcommunications.utils.Constants.REQUEST_IMAGE_CAPTUTRE;
import static com.uu1te721.etcommunications.utils.Constants.TAG;

public class MessengerActivity extends AppCompatActivity implements View.OnClickListener, ArduinoListener {


    private Button mSendBtn, mCameraBtn;
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

    private UsbService usbService;
    private Arduino mArduino;
    private ArduinoListener mArduinoListener;
    private boolean isMessageReceived = false;


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
        mCameraBtn = findViewById(R.id.camera_btn);
        mWriteMessageEt = findViewById(R.id.write_message_et);
        mMessageFeed = findViewById(R.id.message_feed_layout);

        mArduino = new Arduino(this);
        mArduino.addVendorId(10755);
        mArduino.addVendorId(9025);
        mArduino.setArduinoListener(this);

        mSendBtn.setOnClickListener(this);
        mCameraBtn.setOnClickListener(this);

        buildRecyclerView();
//        setupArduino();
    }

    private void buildRecyclerView() {
        lm = new LinearLayoutManager(this);
        mMessageFeed.setLayoutManager(lm);
        mMessengerAdapter = new MessengerRecyclerViewAdapter(this, mMessageCardList);
        mMessageFeed.setAdapter(mMessengerAdapter);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.send_message_btn:
                sendMessage();
                break;

            case R.id.camera_btn:
                takePicture();
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
//        setupArduino();
        mArduino.setArduinoListener(this);
    }

    @Override
    public void onArduinoAttached(UsbDevice device) {
        mArduino.open(device);
    }

    @Override
    public void onArduinoDetached() {

    }

    @Override
    public void onArduinoMessage(byte[] bytes) {
        String data = null;

        try {
            data = new String(bytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }


        Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        recieveImage(bmp);
        Log.d(TAG, "Bytes.length: "+ String.valueOf(bytes.length));
        Log.d(TAG, "Bytes: " + bytes);

//        Log.d(TAG, "onArduinoMessage: isMessageReceived: " + isMessageReceived);
//        if (!isMessageReceived) {
//            isMessageReceived = true;
            if (!(Arrays.toString(bytes).equals("[13]") || Arrays.toString(bytes).equals("[0, 13]"))) {

                receiveMessage(data);
            }
//        }
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mArduino.unsetArduinoListener();
        mArduino.close();
    }

    public void sendMessage() {

        if (mWriteMessageEt.getText().toString().equals(""))
            return;

        // This method called when send button pressed.
        String msg = mWriteMessageEt.getText().toString();

        mArduino.send(msg.getBytes());
        mWriteMessageEt.setText("");
        mWriteMessageEt.clearFocus();

        MessageCard msgCard = new MessageCard(msg, "sent");
        mMessageCardList.add(msgCard);
        mMessengerAdapter.notifyDataSetChanged();
        lm.smoothScrollToPosition(mMessageFeed, null, mMessageCardList.size() - 1);
    }


    public void sendMultimediaMessage(Bitmap multimediaMessage) {
        // Called when picture taken

        int width = multimediaMessage.getWidth();
        int height = multimediaMessage.getHeight();

        int size = multimediaMessage.getRowBytes() * multimediaMessage.getHeight();
        ByteBuffer byteBuffer = ByteBuffer.allocate(size);
        multimediaMessage.copyPixelsToBuffer(byteBuffer);
        byte[] byteArray = byteBuffer.array();

        mArduino.send(byteArray);

        // Displaying multimedia object (Only support image for now).
        MessageCard card = new MessageCard(multimediaMessage, "sent");
        mMessageCardList.add(card);
        mMessengerAdapter.notifyDataSetChanged();
    }


    private void recieveImage(Bitmap bmp){
        // Displaying multimedia object (Only support image for now).
        MessageCard card = new MessageCard(bmp, "received");
        mMessageCardList.add(card);
        mMessengerAdapter.notifyDataSetChanged();
    }
    private void receiveMessage(String msg) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run msg: " + msg);
                MessageCard msgCard = new MessageCard(msg, "received");
                mMessageCardList.add(msgCard);
                mMessengerAdapter.notifyDataSetChanged();
                isMessageReceived = false;
            }
        });
//        Log.d(TAG, "onArduinoMessage: isMessageReceived: " + isMessageReceived);
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

    public void takePicture() {
        Intent imageTakeIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (imageTakeIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(imageTakeIntent, REQUEST_IMAGE_CAPTUTRE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode == REQUEST_IMAGE_CAPTUTRE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();

            Bitmap imageBitmap = (Bitmap) extras.get("data");
            // HERE put a code that sends the picture and displays in the messenger.
            sendMultimediaMessage(imageBitmap);
            Toast.makeText(this, "Picture Taken", Toast.LENGTH_SHORT).show();
        }
    }
}
