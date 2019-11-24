package com.uu1te721.etcommunications;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.felhr.usbserial.UsbSerialDevice;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import me.aflak.arduino.Arduino;
import me.aflak.arduino.ArduinoListener;

import static com.uu1te721.etcommunications.utils.Constants.REQUEST_IMAGE_CAPTUTRE;
import static com.uu1te721.etcommunications.utils.Constants.IMAGE_DISPLAY_SCALE_FACTOR;

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
        String data;

        if (bytes.length > 40) {
            Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            receiveImage(bmp);
        }

        else {
            data = new String(bytes, StandardCharsets.UTF_8);
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

        MessageCard msgCard = new MessageCard(msg, "sent");
        mMessageCardList.add(msgCard);
        mMessengerAdapter.notifyDataSetChanged();
        lm.smoothScrollToPosition(mMessageFeed, null, mMessageCardList.size() - 1);
        mWriteMessageEt.setText("");
        hideSoftKeyboard();
    }

    public void sendPhoto() {
        // Called when picture taken
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inSampleSize = IMAGE_DISPLAY_SCALE_FACTOR ;
        Bitmap multimediaMessage = BitmapFactory.decodeFile(currentPhotoPath, opts );
        // Displaying multimedia object (Only support image for now).
        MessageCard card = new MessageCard(multimediaMessage, currentPhotoPath, "sent");
        mMessageCardList.add(card);
        mMessengerAdapter.notifyDataSetChanged();
        hideSoftKeyboard();

        // Send to Arduino
        ByteBuffer byteBuffer = ByteBuffer.allocate(multimediaMessage.getByteCount());
        multimediaMessage.copyPixelsToBuffer(byteBuffer);
        byte[] byteArray = byteBuffer.array();
        mArduino.send(byteArray);
    }

    private void hideSoftKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void receiveImage(Bitmap bmp) {
        // Displaying multimedia object (Only support image for now).
        runOnUiThread(() -> {
            Toast.makeText(MessengerActivity.this, "received image", Toast.LENGTH_SHORT).show();
            MessageCard card = new MessageCard(bmp, "received");
            mMessageCardList.add(card);
            mMessengerAdapter.notifyDataSetChanged();
        });

    }

    private void receiveMessage(String msg) {

        runOnUiThread(() -> {
            Log.d(TAG, "run msg: " + msg);
            MessageCard msgCard = new MessageCard(msg, "received");
            mMessageCardList.add(msgCard);
            mMessengerAdapter.notifyDataSetChanged();
            isMessageReceived = false;
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


    // *********************************************************************************************
    // Function: Take picture

    String currentPhotoPath;
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        Toast.makeText(this, "image stored.", Toast.LENGTH_SHORT).show();
        return image;
    }

    static final int REQUEST_TAKE_PHOTO = 1;
    public void takePicture() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(this, "Error while creating the File", Toast.LENGTH_SHORT).show();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                Toast.makeText(this, "File created.", Toast.LENGTH_SHORT).show();
            }
        }

    }

        @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
            sendPhoto();
    }
    // END: take picture
    // *********************************************************************************************

    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }
}