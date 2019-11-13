package com.uu1te721.etcommunications;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.linroid.filtermenu.library.FilterMenu;
import com.linroid.filtermenu.library.FilterMenuLayout;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private FilterMenuLayout mLayout;
    private Button mSendButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // mLayout = (FilterMenuLayout) findViewById(R.id.filter_menu);

         // buildFilterMenu();

        mSendButton = findViewById(R.id.send_message_btn);

        mSendButton.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.send_message_btn:
                sendMessage();
            break;

            default:
                return;
        }
    }


    private void sendMessage() {

    }


    /*private void buildFilterMenu() {
        final FilterMenu menu = new FilterMenu.Builder(this)
                .inflate(R.menu.filter_menu_items)//inflate  menu resource
                .attach(mLayout)
                .withListener(new FilterMenu.OnMenuChangeListener() {
                    @Override
                    public void onMenuItemClick(View view, int position) {
                        switch (position) {
                            case 0:
                                Toast.makeText(MainActivity.this, "CLICK 1", Toast.LENGTH_SHORT).show();
                                break;

                            case 1:
                                Toast.makeText(MainActivity.this, "CLICK 2", Toast.LENGTH_SHORT).show();
                                break;
                        }
                    }

                    @Override
                    public void onMenuCollapse() {}

                    @Override
                    public void onMenuExpand() {}
                })
                .build();
    }*/
}
