package com.uu1te721.etcommunications;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.linroid.filtermenu.library.FilterMenu;
import com.linroid.filtermenu.library.FilterMenuLayout;

public class MainActivity extends AppCompatActivity {

    private FilterMenuLayout mLayout;

    public static final String TAG = "TAG";
    private static final String ACTION_USB_PERMISSION = "com.uu1te721.etcommunications.USB_PERMISSION";

    private TextView tv1, tv3, tv4, tv5, tv6, tv7;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLayout = (FilterMenuLayout) findViewById(R.id.filter_menu);
        tv1 = findViewById(R.id.device_list);
        tv3 = findViewById(R.id.device_id_tv);
        tv4 = findViewById(R.id.vendor_tv);
        tv5 = findViewById(R.id.product_tv);
        tv6 = findViewById(R.id.class_tv);
        tv7 = findViewById(R.id.sublcass_tv);


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
                                Toast.makeText(MainActivity.this, "CLICK 1", Toast.LENGTH_SHORT).show();
                                break;

                            case 1:
                                Intent intent = new Intent(MainActivity.this, MessengerActivity.class);
                                startActivity(intent);
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
