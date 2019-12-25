package com.uu1te721.etcommunications;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.linroid.filtermenu.library.FilterMenu;
import com.linroid.filtermenu.library.FilterMenuLayout;
import com.uu1te721.etcommunications.activities.CallerActivity;
import com.uu1te721.etcommunications.activities.MessengerActivity;

public class MainActivity extends AppCompatActivity {

    private FilterMenuLayout mLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLayout = findViewById(R.id.filter_menu);

        buildFilterMenu();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mLayout.setVisibility(View.VISIBLE);
    }


    private void buildFilterMenu() {
        final FilterMenu menu = new FilterMenu.Builder(this)
                .inflate(R.menu.filter_menu_items)//inflate  menu resource
                .attach(mLayout)
                .withListener(new FilterMenu.OnMenuChangeListener() {
                    @Override
                    public void onMenuItemClick(View view, int position) {
                        switch (position) {
                            case 0:
                                Intent callIntent = new Intent(MainActivity.this, CallerActivity.class);
                                startActivity(callIntent);
                                break;

                            case 1:
                                Intent messageIntent = new Intent(MainActivity.this, MessengerActivity.class);
                                startActivity(messageIntent);
                                break;
                        }
                    }

                    @Override
                    public void onMenuCollapse() {
                    }

                    @Override
                    public void onMenuExpand() {
                    }
                })
                .build();
    }





}
