package com.example.newapp;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
//import androidx.appcompat.widget.Toolbar;

import android.preference.PreferenceManager;
import android.view.Menu;
import android.widget.Toast;
import android.widget.VideoView;
//import android.view.MenuItem;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONException;
import org.json.JSONObject;


public class LoginActivity extends AppCompatActivity {

    private VideoView videoBg;
    MediaPlayer videoBgPlayer;

    //private Button btn = null;
    CallbackManager callbackManager;
    private SharedPreferences sharedPreferences;

    LoginButton btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initialize objects
        callbackManager = CallbackManager.Factory.create();
        btn = findViewById(R.id.login_button);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        //register button callback handling login result
        btn.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {

            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });
        //runtime_permission();
        checkLogin();
    }

    private void checkLogin(){
        if(AccessToken.getCurrentAccessToken() != null){
            loginSuccessful(AccessToken.getCurrentAccessToken());
        }
    }


    private void runtime_permission() {
        if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(this, Manifest
                .permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.
                        ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, 2);
        } else {
            start_location_service();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 2) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED || grantResults[1] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(), "Please provide location permission", Toast.LENGTH_SHORT).show();
                runtime_permission();
            } else {
                start_location_service();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    AccessTokenTracker tokenTracker = new AccessTokenTracker() {
        @Override
        protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
            if(currentAccessToken != null){
                loginSuccessful(currentAccessToken);
            }
        }
    };

    private void loginSuccessful(AccessToken newtoken){
        GraphRequest request = GraphRequest.newMeRequest(newtoken, new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                try{
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    String firstname = object.getString("first_name");
                    editor.putString("first_name", object.getString("first_name"));
                    editor.putString("last_name", object.getString("last_name"));
                    editor.putString("userid", object.getString("id"));
                    editor.apply();
                    Toast.makeText(LoginActivity.this, "Logged in as: " + firstname + " " + object.getString("last_name"), Toast.LENGTH_SHORT).show();
                    start_location_service();
                    Intent i = new Intent(LoginActivity.this, Home.class);
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



}
