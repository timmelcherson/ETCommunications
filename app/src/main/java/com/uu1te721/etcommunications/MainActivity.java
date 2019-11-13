package com.uu1te721.etcommunications;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.linroid.filtermenu.library.FilterMenu;
import com.linroid.filtermenu.library.FilterMenuLayout;

public class MainActivity extends AppCompatActivity {

    private FilterMenuLayout mLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLayout = (FilterMenuLayout) findViewById(R.id.filter_menu);

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
    }
}
