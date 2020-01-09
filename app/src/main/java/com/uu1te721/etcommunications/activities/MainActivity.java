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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, SensorEventListener, CustomArduinoListener {


    TextView accX, accY, accZ, txtPosition, txtAlias;
    ImageView aliasIV;

    public static final String TAG = "TAG";
    SensorManager sensorManager;
    Sensor accSensor, gyroSensor, rotationSensor;

    private CustomArduino marduino;

    private List<UwiBuddy> buddyList = new ArrayList<>();
    private List<View> viewBuddyList = new ArrayList<>();
    private Map<String, View> mapBuddyList = new HashMap<>();
    private UwiNeighborhood neighborhood;
    private RecyclerView buddiesfeed;
    private LinearLayoutManager lm;

    ImageView circle;
    Button increaseAngleBtn, decreaseAngleBtn, editAliasBtn;

    private String GET_ID_COMMAND = "00"; //ASCII for '0';
    private double initialAngle = 90;
    private int buddyLeft, buddyRight, buddyTop, buddyBottom, buddyWidth, buddyHeight, circleWidth, circleHeight
            , displayWidth, displayHeight;

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


        aliasIV = findViewById(R.id.imageViewAvatar);

        accX = findViewById(R.id.textAccelerometerX);
        accY = findViewById(R.id.textAccelerometerY);
        accZ = findViewById(R.id.textAccelerometerZ);
        //txtPosition = findViewById(R.id.txt_distance);
        txtAlias = findViewById(R.id.txt_alias);

        increaseAngleBtn = findViewById(R.id.increase_angle_btn);
        decreaseAngleBtn = findViewById(R.id.decrease_angle_btn);
        editAliasBtn = findViewById(R.id.btn_editAlias);
        increaseAngleBtn.setOnClickListener(this);
        decreaseAngleBtn.setOnClickListener(this);
        editAliasBtn.setOnClickListener(this);
        aliasIV.setOnClickListener(this);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);
//        sensorManager.registerListener(this, gyroSensor, SensorManager.SENSOR_DELAY_NORMAL);

        /* Serial connection to Arduino */
        marduino = new CustomArduino(this, 115200);
        marduino.addVendorId(10755);
        marduino.addVendorId(9025);
        marduino.setDelimiter((byte) '\r');
        marduino.setArduinoListener(this);

        setDisplayMeasurements();
        getCircleMeasurements();

//        viewBuddyList.add(initializeNewBuddy());
    }


    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.imageViewAvatar:
                marduino.send(GET_ID_COMMAND.getBytes());
                marduino.send(GET_ID_COMMAND.getBytes());

            case R.id.btn_editAlias:
                Toast.makeText(this, "Edit alias btn clicked!", Toast.LENGTH_SHORT).show();
                txtAlias.setCursorVisible(true);
                txtAlias.setFocusableInTouchMode(true);
                txtAlias.requestFocus();
                txtAlias.setEnabled(true);
                break;

            case R.id.init_buddy_msg_btn:
                marduino.send("ST1".getBytes());
                //marduino.close();
                Intent msgIntent = new Intent(MainActivity.this, MessengerActivity.class);
                startActivity(msgIntent);
                break;

            case R.id.init_buddy_call_btn:
                Toast.makeText(this, "Init call", Toast.LENGTH_SHORT).show();
                break;

            case R.id.increase_angle_btn:
                Intent msgintent = new Intent(MainActivity.this, MessengerActivity.class);
                startActivity(msgintent);
                break;

            case R.id.decrease_angle_btn:
//                decreaseAngle();
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "Main onStart");
        marduino.setArduinoListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, rotationSensor,
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

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

    public void setDisplayMeasurements() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        displayWidth = size.x;
        displayHeight = size.y;
    }

    public View initializeNewBuddy() {
        View v; // Creating an instance for View Object
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        v = inflater.inflate(R.layout.buddy_layout, null);

        ImageView initMsg = v.findViewById(R.id.init_buddy_msg_btn);
        ImageView initCall = v.findViewById(R.id.init_buddy_call_btn);
        ImageView uwiAvatar = v.findViewById(R.id.uwi_avatar);
        txtPosition = v.findViewById(R.id.txt_distance);

        initMsg.setOnClickListener(this);
        initCall.setOnClickListener(this);

        addBuddyToLayout(v);
        return v;
    }

    public void addBuddyToLayout(View view) {
        ConstraintLayout layout = findViewById(R.id.main_layout);
        getViewMeasurements(view);
        runOnUiThread(() -> layout.addView(view));
    }

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
                setBuddyAngle(view, initialAngle);
            }
        });
    }



    @Override
    public void onArduinoAttached(UsbDevice device) {

        marduino.open(device);
    }


    @Override
    public void onArduinoDetached() {
        Toast.makeText(this, "Arduino Detached", Toast.LENGTH_SHORT).show();
        marduino.close();
    }

    @Override
    public void onArduinoMessage(byte[] bytes) {

        Log.d(TAG, "MSG FROM ARDUINO ON MAIN ACTIVITY: " + Arrays.toString(bytes));

        // display in Layaout

        if (bytes[0] == 'I' && bytes[1] == 'D') {
            String str = "";
            for (int i = 3; i < bytes.length - 3; i++) {
                str += (char) bytes[i];
            }

            txtAlias.setText(str);

        } else if (bytes[0] == 'D' && bytes[1] == 'S') {
            String str = "";
            for (int i = 3; i < bytes.length - 5; i++) {
                str += (char) bytes[i];
            }
            if (viewBuddyList.isEmpty()) {
                viewBuddyList.add(initializeNewBuddy());
            }
            updateDistanceToBuddy(str);
        }
    }

    private void updateDistanceToBuddy(String str) {
        runOnUiThread(() -> {
            txtPosition.setText(str);
        });
    }

    @Override
    public void onAccuracyChanged(Sensor accSensor, int i) {

    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_GAME_ROTATION_VECTOR) {
            float[] rotMatrix = new float[9];
            float[] rotVals = new float[3];

            SensorManager.getRotationMatrixFromVector(rotMatrix, event.values);
            SensorManager.remapCoordinateSystem(rotMatrix,
                    SensorManager.AXIS_X, SensorManager.AXIS_Y, rotMatrix);

            SensorManager.getOrientation(rotMatrix, rotVals);
            float yaw = (float) Math.toDegrees(rotVals[0]);

            double angle = (double)(initialAngle + yaw);
            if (!viewBuddyList.isEmpty())
                setBuddyAngle(viewBuddyList.get(0), angle);
        }
    }

    public void setBuddyAngle(View view, double angle) {
        Log.d(TAG, "Setting buddy angle to: " + angle + " degrees");
        double radians = Math.toRadians(angle);
        view.setX((float) (displayWidth / 2) - (float) (view.getWidth() / 2) + (float) Math.cos(radians) * ((float) circleWidth / 2));
        view.setY((float) (displayHeight / 2) - (float) (view.getHeight()) / 2 - (float) Math.sin(radians) * ((float) circleHeight / 2));
    }

    @Override
    public void onArduinoOpened() {
        /* GET device ID */
        Toast.makeText(this, "Arduino OPENED", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onUsbPermissionDenied() {
        marduino.reopen();
    }

    @Override
    protected void onDestroy() {
        marduino.unsetArduinoListener();
        marduino.close();
        Toast.makeText(this, "Main Activity destroyed", Toast.LENGTH_SHORT).show();
        super.onDestroy();

    }

}
