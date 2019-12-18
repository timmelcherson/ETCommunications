package com.uu1te721.etcommunications;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;


import androidx.annotation.Nullable;

public class CallActivity extends Activity {

    Button endcallBtn ;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.call_layout);
        endcallBtn = findViewById(R.id.btn_endcall);
        endcallBtn.setOnClickListener(view -> finish());
    }
}
