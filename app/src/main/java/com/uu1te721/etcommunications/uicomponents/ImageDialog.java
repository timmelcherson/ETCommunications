package com.uu1te721.etcommunications.uicomponents;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.uu1te721.etcommunications.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class ImageDialog extends Activity {

    private ImageView mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_previous);

        mDialog = findViewById(R.id.prev_image);

        String path = getIntent().getStringExtra("imagePath");
//        File photoFile = new File(path);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeFile(path, options);
        mDialog.setImageBitmap(bitmap);
//        try {
//            Bitmap photoBitmap = BitmapFactory.decodeStream(new FileInputStream(photoFile));
//            mDialog = findViewById(R.id.prev_image);
//            mDialog.setImageBitmap(photoBitmap);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }


        //finish the activity (dismiss the image dialog) if the user clicks
        //anywhere on the image
        mDialog.setOnClickListener(v -> finish());

    }
}