package com.example.newapp;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.MenuItem;
import android.widget.Toast;

public class Home extends AppCompatActivity implements HomeActivityCallback{

    public Fragment f;

    private BroadcastReceiver broadcastReceiver;

    /*@Override
    public void onResume() {
        super.onResume();
        if (broadcastReceiver == null){
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    lat = intent.getExtras().getDouble("location_lat");
                    lon = intent.getExtras().getDouble("location_lon");
                    passedobject.putDouble("location_lat", lat);
                    passedobject.putDouble("location_lon", lon);
                    //addHomeFragment();
                }
            };
            registerReceiver(broadcastReceiver, new IntentFilter("location_update"));
        }
    }*/

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        navView.setOnNavigationItemSelectedListener(mOnItemSelected);
        //final Intent thisintent = getIntent();
        //passedobject = thisintent.getExtras();
        //while (lat == 0.0 && lon == 0.0);

        addHomeFragment();

        //debug code for place detail
        //Intent i = new Intent(getApplicationContext(), PlaceDetail.class);
        //startActivity(i);
    }

    /*private void start_location_service() {
        Intent i = new Intent(getApplicationContext(), Location_Service.class);
        startService(i);
    }*/

    private BottomNavigationView.OnNavigationItemSelectedListener mOnItemSelected = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            switch (menuItem.getItemId()){
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

    private void addHomeFragment(){
        try{
            HomeFragment h = new HomeFragment();
            h.getCallBackInterface(this);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentcontainer, h)
                    .commit();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private void switchFragment(Fragment fragment){
        if (fragment != null){
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentcontainer, fragment)
                    .commit();
        }

    }

    @Override
    public void showPlaceDetails(int placeid) {
        Intent intent = new Intent(this, PlaceDetail.class);
        intent.putExtra("placeid", placeid);
        startActivity(intent);
    }
}
