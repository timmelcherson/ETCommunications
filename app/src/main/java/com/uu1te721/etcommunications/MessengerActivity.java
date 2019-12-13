package com.uu1te721.etcommunications;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
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
import com.felhr.usbserial.UsbSerialInterface;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;


import me.aflak.arduino.Arduino;

import static com.uu1te721.etcommunications.utils.Constants.REQUEST_IMAGE_CAPTUTRE;
import static com.uu1te721.etcommunications.utils.Constants.IMAGE_DISPLAY_SCALE_FACTOR;

import static com.uu1te721.etcommunications.utils.Constants.REQUEST_TAKE_PHOTO;
import static com.uu1te721.etcommunications.utils.Constants.TAG;
import static com.uu1te721.etcommunications.utils.Constants.TRANSMISSION_FLAG_IMAGE;
import static com.uu1te721.etcommunications.utils.Constants.TRANSMISSION_FLAG_TEXT;

public class MessengerActivity extends AppCompatActivity implements View.OnClickListener, CustomArduinoListener { //UsbSerialInterface.UsbReadCallback


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
    private CustomArduino mArduino;
    private CustomArduinoListener mArduinoListener;
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

        mArduino = new CustomArduino(this, 115200);
        mArduino.addVendorId(10755);
        mArduino.addVendorId(9025);
        mArduino.setDelimiter((byte) '\r');
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

//    List<Byte> bytesReceived = new ArrayList<>();
//    byte delimiter = '\n';
//
//    @Override
//    public void onReceivedData(byte[] bytes) {
//        Log.d(TAG, "MESSENGER ONRECEIVEDDATA");
//        int matchnr = 1;
//        Log.d(TAG, "--------------------------");
//        for (byte bt : bytes) {
//            Log.d(TAG, String.valueOf((char) bt));
//            if (bt == (char) '\n') {
//                Log.d(TAG, "MATCH NEWLLINE");
//            }
//            if (bt == (char) '\r') {
//                Log.d(TAG, "MATCH CARRIAGE RETURN");
//            }
//        }
//        Log.d(TAG, "--------------------------");
//        Log.d(TAG, Arrays.toString(bytes));
//        Log.d(TAG, "--------------------------");
//
//        if (bytes.length != 0) {
//            List<Integer> idx = indexOf(bytes, delimiter);
//            if (idx.isEmpty()) {
//                bytesReceived.addAll(toByteList(bytes));
//            } else {
//                int offset = 0;
//                for (int index : idx) {
//                    byte[] tmp = Arrays.copyOfRange(bytes, offset, index);
//                    bytesReceived.addAll(toByteList(tmp));
//                    if (mArduinoListener != null) {
//                        mArduinoListener.onArduinoMessage(toByteArray(bytesReceived));
//                    }
//                    bytesReceived.clear();
//                    offset += index + 1;
//                }
//
//                if (offset < bytes.length - 1) {
//                    byte[] tmp = Arrays.copyOfRange(bytes, offset, bytes.length);
//                    bytesReceived.addAll(toByteList(tmp));
//                }
//            }
//        }
//    }
//
//    private List<Integer> indexOf(byte[] bytes, byte b) {
//        List<Integer> idx = new ArrayList<>();
//        for (int i = 0; i < bytes.length; i++) {
//            if (bytes[i] == b) {
//                idx.add(i);
//            }
//        }
//        return idx;
//    }
//
//    private List<Byte> toByteList(byte[] bytes) {
//        List<Byte> list = new ArrayList<>();
//        for (byte b : bytes) {
//            list.add(b);
//        }
//        return list;
//    }
//
//    private byte[] toByteArray(List<Byte> bytes) {
//        byte[] array = new byte[bytes.size()];
//        for (int i = 0; i < bytes.size(); i++) {
//            array[i] = bytes.get(i);
//        }
//        return array;
//    }

    public static void logBytes(byte[] arr) {

        String str = "";
        String innerStr = "";

        if (arr.length > 50) {
            for (int i = 1; i <= 10; i++) {
                str = str.concat(Integer.toBinaryString(arr[i - 1] & 255 | 256).substring(1) + " ");

                if (i >= arr.length - 4) {
                    innerStr = innerStr.concat(Integer.toBinaryString(arr[i - 1] & 255 | 256).substring(1) + " ");
                    if (i >= arr.length) {
                        Log.d(TAG, innerStr);
                        innerStr = "";
                    }
                } else if (i % 4 == 0) {
                    Log.d(TAG, str);
                    str = "";
                }
            }
            Log.d(TAG, ". . .");
            for (int i = arr.length - 10; i <= arr.length; i++) {
                str = str.concat(Integer.toBinaryString(arr[i - 1] & 255 | 256).substring(1) + " ");

                if (i >= arr.length - 4) {
                    innerStr = innerStr.concat(Integer.toBinaryString(arr[i - 1] & 255 | 256).substring(1) + " ");
                    if (i >= arr.length) {
                        Log.d(TAG, innerStr);
                        innerStr = "";
                    }
                } else if (i % 4 == 0) {
                    Log.d(TAG, str);
                    str = "";
                }
            }
        } else {
            for (int i = 1; i <= arr.length; i++) {
                str = str.concat(Integer.toBinaryString(arr[i - 1] & 255 | 256).substring(1) + " ");

                if (i >= arr.length - 4) {
                    innerStr = innerStr.concat(Integer.toBinaryString(arr[i - 1] & 255 | 256).substring(1) + " ");
                    if (i >= arr.length) {
                        Log.d(TAG, innerStr);
                        innerStr = "";
                    }
                } else if (i % 4 == 0) {
                    Log.d(TAG, str);
                    str = "";
                }
            }
        }

    }

    boolean isFlagSet = false;

    @Override
    public void onArduinoMessage(byte[] bytes) {
//        String data;

        Log.d(TAG, "ON ARDUINO MESSAGE");
        if (bytes.length != 0) {
            logBytes(bytes);

//
//
            char flag = 0; // No flag set

            if (!isFlagSet) {
                flag = (char) bytes[0];
                isFlagSet = true;
                Log.d(TAG, "Flag is: " + flag);
                bytes = Arrays.copyOfRange(bytes, 1, bytes.length);
            }
//
//        for (byte bt : bytes) {
//            if (bt == 62) {
//                Log.d(TAG, "End marker detected, transmission complete");
//            }
//        }


            switch (flag) {

                case 'i':
                    receiveImage(bytes);
                    Log.d(TAG, "Received Image");
                    break;

                case 't':
                    receiveMessage(bytes);
                    break;

                default:
                    runOnUiThread(() -> {
                        Toast.makeText(this, "No flag detected", Toast.LENGTH_SHORT).show();
                    });
                    break;

            }
            isFlagSet = false;
        }
    }

    @Override
    public void onArduinoOpened() {
        // you can start the communication
//        String str = "Hello CustomArduino";
//        mArduino.send(str.getBytes());
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

    public void receptionRouter(char flag) {

        switch (flag) {

            case 'i':
                runOnUiThread(() -> {
                    Toast.makeText(this, "Received Image", Toast.LENGTH_SHORT).show();
                });

                Log.d(TAG, "Received Image");
                break;

            case 't':
                runOnUiThread(() -> {
                    Toast.makeText(this, "Received Text", Toast.LENGTH_SHORT).show();
                });

                Log.d(TAG, "Received Text");
                break;
        }
    }

    public void sendMessage() {

        if (mWriteMessageEt.getText().toString().equals(""))
            return;

        // This method called when send button pressed.
        String msg = mWriteMessageEt.getText().toString();
//        Log.d(TAG, "sendMessage - msg in bytes (before flag): " + Arrays.toString(msg.getBytes()));

        byte[] arrayForTransmission = addTransmissionFlagToByteArray(TRANSMISSION_FLAG_TEXT, msg.getBytes());
//        Log.d(TAG, "sendMessage: LOGGING TRANSMISSION BYTES:");
//        logBytes(arrayForTransmission);
        if ((char) arrayForTransmission[0] == 't') {
            mArduino.send(arrayForTransmission);
            Log.d(TAG, "sent this msg: " + Arrays.toString(arrayForTransmission));
            MessageCard msgCard = new MessageCard(msg, "sent");
            mMessageCardList.add(msgCard);
            mMessengerAdapter.notifyDataSetChanged();
            lm.smoothScrollToPosition(mMessageFeed, null, mMessageCardList.size() - 1);
            mWriteMessageEt.setText("");
            hideSoftKeyboard();
        } else {
            Log.d(TAG, "sendMessage: WRONG FLAG: " + (char) arrayForTransmission[0]);
        }
    }

    public void sendPhoto() {
        // Called when picture taken
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inSampleSize = IMAGE_DISPLAY_SCALE_FACTOR;
        Bitmap multimediaMessage = BitmapFactory.decodeFile(currentPhotoPath, opts);
//        multimediaMessage = getResizedBitmap(multimediaMessage, 100);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        multimediaMessage = getResizedBitmap(multimediaMessage, 300);
        multimediaMessage.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        // Displaying multimedia object (Only support image for now).

        // Send to CustomArduino
//        ByteBuffer byteBuffer = ByteBuffer.allocate(multimediaMessage.getByteCount());
//        multimediaMessage.copyPixelsToBuffer(byteBuffer);
//        byte[] byteArray = byteBuffer.array();
        byte[] arrayForTransmission = addTransmissionFlagToByteArray(TRANSMISSION_FLAG_IMAGE, byteArray);
//        receiveImage(byteArray);
        Log.d(TAG, "sendPhoto: LOGGING TRANSMISSION BYTES, size of picture: " + multimediaMessage.getByteCount());
        logBytes(arrayForTransmission);
//        Toast.makeText(this, "Sending photo with FLAG: " + (char) arrayForTransmission[0], Toast.LENGTH_SHORT).show();
        if ((char) arrayForTransmission[0] == 'i') {
            mArduino.send(arrayForTransmission);
            MessageCard card = new MessageCard(multimediaMessage, currentPhotoPath, "sent");
            mMessageCardList.add(card);
            mMessengerAdapter.notifyDataSetChanged();
            lm.smoothScrollToPosition(mMessageFeed, null, mMessageCardList.size() - 1);
            hideSoftKeyboard();
        } else {
            Log.d(TAG, "sendMessage: WRONG FLAG: " + (char) arrayForTransmission[0]);
        }
    }

    private byte[] addTransmissionFlagToByteArray(byte flag, byte[] arr) {

        ByteBuffer combined = ByteBuffer.allocate(1 + arr.length + 3);
        combined.put(flag);
        combined.put(arr);
        combined.put((byte) '>');
        combined.put((byte) '>');
        combined.put((byte) '>');
        return combined.array();
    }

    private void hideSoftKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void receiveImage(byte[] bitmapArray) {
        // Displaying multimedia object (Only support image for now).
        Log.d(TAG, "RECEIVE IMAGE");
        logBytes(bitmapArray);
        Bitmap bmp = BitmapFactory.decodeByteArray(bitmapArray, 0, bitmapArray.length);
//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inMutable = true;
//        Bitmap bmp = BitmapFactory.decodeByteArray(bitmapArray, 0, bitmapArray.length, options);
//        Canvas canvas = new Canvas(bmp);
        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            ex.printStackTrace();
//                Toast.makeText(this, "Error while creating the File", Toast.LENGTH_SHORT).show();
        }

        // Continue only if the File was successfully created
        if (photoFile != null) {
            Uri photoURI = FileProvider.getUriForFile(this,
                    "com.example.android.fileprovider",
                    photoFile);
//                Toast.makeText(this, "File created.", Toast.LENGTH_SHORT).show();
            runOnUiThread(() -> {
                Toast.makeText(MessengerActivity.this, "received image", Toast.LENGTH_SHORT).show();
                MessageCard card = new MessageCard(bmp, photoURI.toString(), "received");
                mMessageCardList.add(card);
                mMessengerAdapter.notifyDataSetChanged();
            });
        }
    }

    private void receiveMessage(byte[] byteArray) {

        String msg = new String(byteArray);

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
//        Toast.makeText(this, "image stored.", Toast.LENGTH_SHORT).show();
        return image;
    }


    public void takePicture() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.printStackTrace();
//                Toast.makeText(this, "Error while creating the File", Toast.LENGTH_SHORT).show();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
//                Toast.makeText(this, "File created.", Toast.LENGTH_SHORT).show();
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: here");
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK)
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