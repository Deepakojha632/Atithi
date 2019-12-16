package com.example.newapp.activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.newapp.R;
import com.example.newapp.callbackinterface.HomeActivityCallback;
import com.example.newapp.fragment.BotFragment;
import com.example.newapp.fragment.HomeFragment;
import com.example.newapp.fragment.UserFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class Home extends AppCompatActivity implements HomeActivityCallback {
    public static String PACKAGE_NAME;
    FragmentManager fm = getSupportFragmentManager();
    private Fragment f;
    private HomeFragment homeFragment;
    private UserFragment userFragment;
    private BotFragment botFragment;
    String TAG = "Home";
    private BottomNavigationView.OnNavigationItemSelectedListener mOnItemSelected = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.navigation_home:
                    f = new HomeFragment();
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

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        PACKAGE_NAME = getApplicationContext().getPackageName();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        navView.setOnNavigationItemSelectedListener(mOnItemSelected);
        addHomeFragment();
    }

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
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragment != null) {
            fragmentManager.beginTransaction()
                    .replace(R.id.fragmentcontainer, fragment).addToBackStack(null)
                    .commit();
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
        FragmentManager fm = getSupportFragmentManager();
        int count = fm.getBackStackEntryCount();
        if (count > 0) {
            fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        } else {
            super.onBackPressed();
        }
    }
}
