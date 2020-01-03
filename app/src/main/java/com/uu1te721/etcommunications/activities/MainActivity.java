package com.uu1te721.etcommunications.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.uu1te721.etcommunications.R;
import com.uu1te721.etcommunications.adapters.UwiBuddy;
import com.uu1te721.etcommunications.adapters.UwiNeighborhood;
import com.uu1te721.etcommunications.arduino.CustomArduino;
import com.uu1te721.etcommunications.arduino.CustomArduinoListener;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, SensorEventListener, CustomArduinoListener {


    TextView accX;
    TextView accY;
    TextView accZ;
    TextView txtPosition;

    public static final String TAG = "TAG";
    SensorManager sensorManager;
    Sensor sensor;

    CustomArduino marduino;
    private List<UwiBuddy> buddyList = new ArrayList<>();
    private List<View> viewBuddyList = new ArrayList<>();
    private UwiNeighborhood neighborhood;
    private RecyclerView buddiesfeed;
    private LinearLayoutManager lm;

    ImageView circle;
    Button increaseAngleBtn, decreaseAngleBtn;

    private String GET_ID_COMMAND = "48"; //ASCII for '0';

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
        increaseAngleBtn = findViewById(R.id.increase_angle_btn);
        decreaseAngleBtn = findViewById(R.id.decrease_angle_btn);
        increaseAngleBtn.setOnClickListener(this);
        decreaseAngleBtn.setOnClickListener(this);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);


        buddiesfeed = findViewById(R.id.buddies_feed_layout);


        lm = new LinearLayoutManager(this);
        buddiesfeed.setLayoutManager(lm);
        neighborhood = new UwiNeighborhood(this, buddyList);
        buddiesfeed.setAdapter(neighborhood);



        /* Serial connection to Arduino */
        marduino = new CustomArduino(this, 115200);
        marduino.addVendorId(10755);
        marduino.addVendorId(9025);
        marduino.setDelimiter((byte) '\r');
        marduino.setArduinoListener(this);

        buddyAngle = 225.0;
        setDisplayMeasurements();
        getCircleMeasurements();

        viewBuddyList.add(initializeNewBuddy());
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.init_buddy_msg_btn:
                Intent msgIntent = new Intent(MainActivity.this, MessengerActivity.class);
                startActivity(msgIntent);
                break;

            case R.id.init_buddy_call_btn:
                Toast.makeText(this, "Init call", Toast.LENGTH_SHORT).show();
                break;

            case R.id.increase_angle_btn:
                increaseAngle();
                break;

            case R.id.decrease_angle_btn:
                decreaseAngle();
                break;
        }
    }

    private int circleWidth;
    private int circleHeight;

    public void getCircleMeasurements() {
        circle = findViewById(R.id.main_circle);
        circle.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                circleWidth = circle.getWidth();
                circleHeight = circle.getHeight();
                circle.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

    private int buddyLeft;
    private int buddyRight;
    private int buddyTop;
    private int buddyBottom;
    private int buddyWidth;
    private int buddyHeight;

    public void getViewMeasurements(View view) {
        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                buddyLeft = view.getLeft();
                buddyRight = view.getRight();
                buddyTop = view.getTop();
                buddyBottom = view.getBottom();
                buddyWidth = view.getWidth();
                buddyHeight = view.getHeight();
                view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                Log.d(TAG, "unlistened to changes");
                setBuddyAngle(view, buddyAngle);
            }
        });
    }

    int displayWidth;
    int displayHeight;
    double buddyAngle;

    public void setDisplayMeasurements() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        displayWidth = size.x;
        displayHeight = size.y;
    }

    public void increaseAngle() {
        buddyAngle = buddyAngle + 10;
        if (buddyAngle >= 360) {
            buddyAngle = 0;
        }
        setBuddyAngle(viewBuddyList.get(0), buddyAngle);
    }
    public void decreaseAngle() {
        buddyAngle = buddyAngle - 10;
        if (buddyAngle <= 0) {
            buddyAngle = 360;
        }
        setBuddyAngle(viewBuddyList.get(0), buddyAngle);
    }

    public void setBuddyAngle(View view, double angle) {
        double radians = Math.toRadians(angle);
        view.setX((float) (displayWidth / 2) - (float) (view.getWidth() / 2) + (float) Math.cos(radians) * ((float) circleWidth / 2));
        view.setY((float) (displayHeight / 2) - (float) (view.getHeight()) / 2 - (float) Math.sin(radians) * ((float) circleHeight / 2));
    }

    public void addBuddyToLayout(View view) {
        ConstraintLayout layout = findViewById(R.id.main_layout);
        getViewMeasurements(view);
        layout.addView(view);
    }

    public View initializeNewBuddy() {
        View v; // Creating an instance for View Object
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        v = inflater.inflate(R.layout.buddy_layout, null);

        ImageView initMsg = v.findViewById(R.id.init_buddy_msg_btn);
        ImageView initCall = v.findViewById(R.id.init_buddy_call_btn);
        ImageView uwiAvatar = v.findViewById(R.id.uwi_avatar);
        TextView buddyDistance = v.findViewById(R.id.txt_distance);

        initMsg.setOnClickListener(this);
        initCall.setOnClickListener(this);


        addBuddyToLayout(v);
        return v;
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
        marduino.open(device);
    }

    @Override
    public void onArduinoDetached() {
        Toast.makeText(this, "Arduino Detached", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onArduinoMessage(byte[] bytes) {
        // display in Layaout
        Toast.makeText(this, String.valueOf(bytes), Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onArduinoOpened() {
        /* GET device ID */
        marduino.send(GET_ID_COMMAND.getBytes());
    }

    @Override
    public void onUsbPermissionDenied() {

    }
}
