package com.uu1te721.etcommunications.activities;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.uu1te721.etcommunications.R;
import com.uu1te721.etcommunications.adapters.UwiBuddy;
import com.uu1te721.etcommunications.adapters.UwiNeighborhood;
import com.uu1te721.etcommunications.arduino.CustomArduino;
import com.uu1te721.etcommunications.arduino.CustomArduinoListener;

import java.util.ArrayList;
import java.util.List;

import static com.uu1te721.etcommunications.utils.Constants.TAG;


public class PositionActivity extends AppCompatActivity implements SensorEventListener, CustomArduinoListener {


     TextView accX;
     TextView accY;
     TextView accZ;
    TextView txtPosition;


    SensorManager sensorManager;
    Sensor sensor;

    CustomArduino marduino;
    private List<UwiBuddy> buddyList = new ArrayList<>();
    private UwiNeighborhood neighborhood;
    private RecyclerView buddiesfeed;
    private LinearLayoutManager lm;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);

        DisplayMetrics dm = new DisplayMetrics();

        getWindowManager().getDefaultDisplay().getRealMetrics(dm);

        int width = (int) (dm.widthPixels * 0.8);
        int height = (int) (dm.heightPixels * 0.4);

        getWindow().setLayout(dm.widthPixels, dm.heightPixels);
        // TODO: Dismiss when dm.widthPixels x or tapped outside the pop up windows.


        accX = findViewById(R.id.textAccelerometerX);
        accY = findViewById(R.id.textAccelerometerY);
        accZ = findViewById(R.id.textAccelerometerZ);
        txtPosition = findViewById(R.id.txt_distance);


        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);


        buddiesfeed = findViewById(R.id.buddies_feed_layout);


        lm = new LinearLayoutManager(this);
        buddiesfeed.setLayoutManager(lm);
        neighborhood = new UwiNeighborhood(this, buddyList);
        buddiesfeed.setAdapter(neighborhood);



    }

    public void displayNewBuddy(int ID){
        /* This adds and displays the buddy in the radar */
        UwiBuddy bdy = new UwiBuddy(ID);
        buddyList.add(bdy);
        neighborhood.notifyDataSetChanged();
    }



    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        accX.setText("X: " + sensorEvent.values[0]);
        accY.setText("Y: " + sensorEvent.values[1]);
        accZ.setText("Z: " + sensorEvent.values[2]);

    }

    @Override
    public void onArduinoAttached(UsbDevice device) {

    }

    @Override
    public void onArduinoDetached() {

    }

    @Override
    public void onArduinoMessage(byte[] bytes) {
         // display in Layaout

        Log.d(TAG, String.valueOf(bytes));

    }

    @Override
    public void onArduinoOpened() {

    }

    @Override
    public void onUsbPermissionDenied() {

    }
}
