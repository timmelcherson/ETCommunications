package com.uu1te721.etcommunications.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.uu1te721.etcommunications.R;

public class CallerActivity extends AppCompatActivity implements View.OnClickListener{

    private ImageView mInitiateCallBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_caller);

        mInitiateCallBtn = findViewById(R.id.initiate_call_btn);
        mInitiateCallBtn.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.initiate_call_btn:
                Toast.makeText(this, "CALLING", Toast.LENGTH_SHORT).show();
                break;


        }
    }
}
