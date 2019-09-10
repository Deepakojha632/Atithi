package com.example.newapp;

import android.Manifest;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import org.json.JSONException;
import org.json.JSONObject;

public class Splash extends AppCompatActivity {

    private SharedPreferences runtime;
    private SharedPreferences.Editor runtimeeditor;

    private Location_Service location_service;
    private ServiceConnection serviceConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        runtime = PreferenceManager.getDefaultSharedPreferences(this);

        //check if device is registered
        if(runtime.getBoolean("registered", false)){
            //device already registered
            if(checkLogin()){
                //true means user has already logged in
                loginSuccessful(AccessToken.getCurrentAccessToken());
            } else {
                //user not logged in. call login activity
                Intent i = new Intent(this, LoginActivity.class);
                startActivity(i);
            }
        } else {
            //device not registered. call onboarding activity
            Intent i = new Intent(this, Onboarding.class);
            startActivity(i);
        }
    }

    //Check if the user is logged into facebook
    private boolean checkLogin(){
        if(AccessToken.getCurrentAccessToken() != null){
            //user logged in
            return true;
        }else {
            //user not logged in
            return false;
        }
    }

    //If user is logged in
    private void loginSuccessful(AccessToken newtoken){
        GraphRequest request = GraphRequest.newMeRequest(newtoken, new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                try{
                    //set runtime object with user's details
                    runtimeeditor = runtime.edit();
                    runtimeeditor.putString("userid", object.getString("id"));
                    runtimeeditor.putString("first_name", object.getString("first_name"));
                    runtimeeditor.putString("last_name", object.getString("last_name"));
                    runtimeeditor.apply();
                    Toast.makeText(Splash.this, "Logged in as: " + object.getString("first_name") + " " + object.getString("last_name"), Toast.LENGTH_SHORT).show();
                    checkLocationPermission();

                    //call Home activity
                    Intent i = new Intent(Splash.this, Home.class);
                    startActivity(i);
                }catch (JSONException e){
                    e.printStackTrace();
                }
            }
        });
        Bundle params = new Bundle();
        params.putString("fields", "first_name,last_name, email, id");
        request.setParameters(params);
        request.executeAsync();
    }

    private void start_location_service() {
        //starting location tracking service
        Intent i = new Intent(getApplicationContext(), Location_Service.class);
        startService(i);
    }

    private void checkLocationPermission() {
        //check and ask for permission from the user
        if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(this, Manifest
                .permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.
                        ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, 2);
        } else {
            //if permission granted, start location tracking service
            start_location_service();
        }
    }

    //Handling permission request prompt result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 2) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED || grantResults[1] != PackageManager.PERMISSION_GRANTED) {
                //permission rejected. show a toast and ask again
                Toast.makeText(getApplicationContext(), "Please provide location access!", Toast.LENGTH_SHORT).show();
                checkLocationPermission();
            } else {
                //Permission granted. Start location tracking service
                start_location_service();
            }
        }
    }
}
