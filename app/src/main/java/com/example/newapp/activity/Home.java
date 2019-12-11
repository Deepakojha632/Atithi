package com.example.newapp.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.newapp.R;
import com.example.newapp.callbackinterface.HomeActivityCallback;
import com.example.newapp.fragment.BotFragment;
import com.example.newapp.fragment.HomeFragment;
import com.example.newapp.fragment.UserFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class Home extends AppCompatActivity implements HomeActivityCallback {

    public Fragment f;
    String TAG = "Home";

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        navView.setOnNavigationItemSelectedListener(mOnItemSelected);
        addHomeFragment();
    }

    /*private void start_location_service() {
        Intent i = new Intent(getApplicationContext(), Location_Service.class);
        startService(i);
    }*/

    private BottomNavigationView.OnNavigationItemSelectedListener mOnItemSelected = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.navigation_home:
                    addHomeFragment();
                    break;

                case R.id.navigation_bot:
                    f = new BotFragment();
                    break;

                case R.id.navigation_user:
                    f = new UserFragment();
                    break;
            }
            switchFragment(f);
            return true;
        }
    };

    private void addHomeFragment() {
        try {
            HomeFragment h = new HomeFragment();
            h.getCallBackInterface(this);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentcontainer, h)
                    .commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void switchFragment(Fragment fragment) {
        if (fragment != null) {
            //FragmentManager fragmentManager = this.getSupportFragmentManager();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentcontainer, fragment)
                    .commit();

            int count = getFragmentManager().getBackStackEntryCount();
            if (count > 0) {
                for (int i = 0; i < count; i++) {
                    getFragmentManager().popBackStack();
                }
            }
        }
    }

    //Home Activity Callback for place details
    @Override
    public void showPlaceDetails(int placeid) {
        Intent intent = new Intent(this, PlaceDetail.class);
        intent.putExtra("placeid", placeid);
        startActivity(intent);
    }

    @Override
    public void showPlacesByCategory(String catid, String catname) {
        Intent intent = new Intent(this, PlacesByCategory.class);
        intent.putExtra("cat_id", catid);
        intent.putExtra("cat_name", catname);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }
}
