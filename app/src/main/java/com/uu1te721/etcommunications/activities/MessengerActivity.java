package com.uu1te721.etcommunications.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.usb.UsbDevice;
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

import com.uu1te721.etcommunications.R;
import com.uu1te721.etcommunications.adapters.MessengerRecyclerViewAdapter;
import com.uu1te721.etcommunications.arduino.CustomArduino;
import com.uu1te721.etcommunications.arduino.CustomArduinoListener;
import com.uu1te721.etcommunications.uicomponents.MessageCard;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.uu1te721.etcommunications.utils.Constants.IMAGE_DISPLAY_SCALE_FACTOR;
import static com.uu1te721.etcommunications.utils.Constants.REQUEST_TAKE_PHOTO;
import static com.uu1te721.etcommunications.utils.Constants.TAG;
import static com.uu1te721.etcommunications.utils.Constants.TRANSMISSION_FLAG_IMAGE;
import static com.uu1te721.etcommunications.utils.Constants.TRANSMISSION_FLAG_TEXT;
import static com.uu1te721.etcommunications.utils.ImageUtils.getResizedBitmap;
import static com.uu1te721.etcommunications.utils.TransmissionUtils.addTransmissionFlagToByteArray;

public class MessengerActivity extends AppCompatActivity implements View.OnClickListener, CustomArduinoListener {

    // Views
    private Button mSendBtn, mCameraBtn;
    private EditText mWriteMessageEt;
    private RecyclerView mMessageFeed;
    private MessengerRecyclerViewAdapter mMessengerAdapter;
    private LinearLayoutManager lm;

    // Variables
    private List<String> mMessageList = new ArrayList<>();
    private List<MessageCard> mMessageCardList = new ArrayList<>();
    private boolean isFlagSet = false;
    private String currentPhotoPath;

    // Other
    private CustomArduino mArduino;


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
        mArduino.setArduinoListener(this);
        super.onStart();
    }

    @Override
    public void onArduinoAttached(UsbDevice device) {
        mArduino.open(device);
    }

    @Override
    public void onArduinoDetached() {}

    @Override
    public void onArduinoMessage(byte[] bytes) {

        if (bytes.length != 0) {
            char flag = 0; // No flag set
            if (!isFlagSet) {
                flag = (char) bytes[0];
                isFlagSet = true;
            }

            switch (flag) {

                case 'i':
                    byte[] bbi = Arrays.copyOfRange(bytes, 5, (int) bytes.length-3);
                    receiveImage(bbi);
                    break;

                case 't':

                    byte[] bbt = Arrays.copyOfRange(bytes, 1, (int) bytes.length-3);
                    receiveMessage(bbt);
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
    }

    @Override
    public void onUsbPermissionDenied() {
        mArduino.reopen();
    }

    @Override
    protected void onDestroy() {
        mArduino.unsetArduinoListener();
        mArduino.close();
        super.onDestroy();
    }

    public void sendMessage() {

        if (mWriteMessageEt.getText().toString().equals(""))
            return;

        // This method called when send button pressed.
        String msg = mWriteMessageEt.getText().toString();
        byte[] arrayForTransmission = addTransmissionFlagToByteArray(TRANSMISSION_FLAG_TEXT, msg.getBytes()); //'t' + msg + '>>>'

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

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        multimediaMessage = getResizedBitmap(multimediaMessage, 200);
        multimediaMessage.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        byte[] arrayForTransmission = addTransmissionFlagToByteArray(TRANSMISSION_FLAG_IMAGE, byteArray);
        Log.d(TAG, "sendPhoto: LOGGING TRANSMISSION BYTES, size of picture: " + multimediaMessage.getByteCount());

        if ((char) arrayForTransmission[0] == 'i') {
            mArduino.send(arrayForTransmission);
            MessageCard card = new MessageCard(multimediaMessage, currentPhotoPath, "sent");
            mMessageCardList.add(card);
            mMessengerAdapter.notifyDataSetChanged();
            lm.smoothScrollToPosition(mMessageFeed, null, mMessageCardList.size() - 1);
            hideSoftKeyboard();
//            receiveImage(byteArray);
        } else {
            Log.d(TAG, "sendMessage: WRONG FLAG: " + (char) arrayForTransmission[0]);
        }
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
        Bitmap bmp = BitmapFactory.decodeByteArray(bitmapArray, 0, bitmapArray.length);

        File photoFile = null;
        try {
            photoFile = createImageFile();
            Log.d(TAG, "Created image file");
        } catch (IOException ex) {
            ex.printStackTrace();
            Log.d(TAG, "Failed to create image file, msg: " + ex);
        }

        // Continue only if the File was successfully created
        if (photoFile != null) {
            Intent saveImageIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                Log.d(TAG, "Image taken, uri is: " + photoURI);
                saveImageIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            runOnUiThread(() -> {
                Toast.makeText(MessengerActivity.this, "received image", Toast.LENGTH_SHORT).show();
                MessageCard card = new MessageCard(bmp, currentPhotoPath, "received");
                mMessageCardList.add(card);
                mMessengerAdapter.notifyDataSetChanged();
            });
        }
        else {
            Log.d(TAG, "Photofile was null, image was not created");
        }
    }

    private void receiveMessage(byte[] byteArray) {

        String msg = new String(byteArray);

        runOnUiThread(() -> {
            MessageCard msgCard = new MessageCard(msg, "received");
            mMessageCardList.add(msgCard);
            mMessengerAdapter.notifyDataSetChanged();
        });
        lm.smoothScrollToPosition(mMessageFeed, null, mMessageCardList.size());
    }

    @Override
    public boolean onSupportNavigateUp() {
        mArduino.send("ST0".getBytes());
        //mArduino.close();
        super.onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


    // *********************************************************************************************
    // Function: Take picture


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
        Log.d(TAG, "Current photo path: " + currentPhotoPath);
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
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                Log.d(TAG, "Image taken, uri is: " + photoURI);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
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

}