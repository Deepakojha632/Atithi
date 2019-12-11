package com.example.newapp.activity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.newapp.R;
import com.example.newapp.service.Location_Service;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

//import androidx.appcompat.widget.Toolbar;
//import android.view.MenuItem;


public class LoginActivity extends AppCompatActivity {
    String TAG = "LoginActivity";

    CallbackManager callbackManager;
    private SharedPreferences sharedPreferences;

    LoginButton btn;
    //private Button btn = null;
    private String serverurl = "http://arnab882.heliohost.org";
    AccessTokenTracker tokenTracker = new AccessTokenTracker() {
        @Override
        protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
            if (currentAccessToken != null) {
                loginSuccessful(currentAccessToken);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initialize objects
        callbackManager = CallbackManager.Factory.create();
        btn = findViewById(R.id.login_button);
        btn.setPermissions("user_photos");

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

    private void checkLogin() {
        if (AccessToken.getCurrentAccessToken() != null) {
            Log.e(TAG, "Access Token: " + AccessToken.getCurrentAccessToken());
            loginSuccessful(AccessToken.getCurrentAccessToken());
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

    private void runtime_permission() {
        if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(this, Manifest
                .permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.
                        ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, 2);
        } else {
            start_location_service();
        }
    }

    private void loginSuccessful(final AccessToken newtoken) {
        GraphRequest request = GraphRequest.newMeRequest(newtoken, new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                try {
                    Log.e(TAG, "onCompleted1: " + response.toString());
                    String data = "[{\"userid\":\"" + object.getString("id") +
                            "\",\"firstname\":\"" + object.getString("first_name") +
                            "\",\"lastname\":\"" + object.getString("last_name") + "\"}]";
                    FileOutputStream fos = getApplicationContext().openFileOutput("user", MODE_PRIVATE);
                    fos.write(data.getBytes());
                    fos.close();
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    String firstname = object.getString("first_name");
                    editor.putString("first_name", object.getString("first_name"));
                    editor.putString("last_name", object.getString("last_name"));
                    editor.putString("userid", object.getString("id"));
                    editor.apply();
                    //check registration status
                    checkRegistration(object.getString("id"));

                    Toast.makeText(LoginActivity.this, "Logged in as: " + firstname + " " + object.getString("last_name"), Toast.LENGTH_SHORT).show();
                    //
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            }
        });

        Bundle params = new Bundle();
        params.putString("fields", "first_name,last_name, email, id");
        request.setParameters(params);
        request.executeAsync();
    }

    public void checkRegistration(String id) {
        OkHttpClient client = new OkHttpClient();
        final boolean[] registrationstatus = new boolean[1];
        Request request = new Request.Builder()
                .url(serverurl + "/getregstatus.php?userid=" + id)
                .addHeader("Accept", "application/json; q=0.5")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                ResponseBody responseBody = response.body();
                JSONObject object;
                try {
                    object = new JSONObject(responseBody.string());
                    Log.e(TAG, "onResponse: " + object.toString());
                    if ((boolean) object.get("registered")) {
                        //user registered
                        Log.e(TAG, "onCompleted2: user registered");
                        start_location_service();
                        Intent i = new Intent(LoginActivity.this, Home.class);
                        startActivity(i);
                    } else {
                        Log.e(TAG, "onCompleted3: user not registered");
                        registerUser();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void registerUser() {
        //register newly logged in user
        Log.e(TAG, "registerUser: " + AccessToken.getCurrentAccessToken().getToken());
        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("token", AccessToken.getCurrentAccessToken().getToken())
                .build();
        Request request = new Request.Builder()
                .url(serverurl + "/userregister.php")
                .post(body)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String resp = response.body().string();
                Log.d(TAG, "onResponse: " + resp);
                if (response.code() == 200) {
                    start_location_service();
                    Intent i = new Intent(LoginActivity.this, Home.class);
                    startActivity(i);
                }
            }
        });
    }

//    private void registerInterest(){
//        Log.e(TAG, "registerInterest: called");
//        final String[] id = new String[1];
//        GraphRequest request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
//            @Override
//            public void onCompleted(JSONObject object, GraphResponse response) {
//                try {
//                    id[0] = object.getString("id");
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//        Bundle params = new Bundle();
//        params.putString("fields", "id");
//        request.setParameters(params);
//        request.executeAsync();
//        GraphRequest request1 = GraphRequest.newGraphPathRequest(AccessToken.getCurrentAccessToken(), "/me/photos", new GraphRequest.Callback() {
//            @Override
//            public void onCompleted(GraphResponse response) {
//                //JSONObject object = response.getJSONObject();
//                Log.e(TAG, "onCompleted: "+ response.toString() );
//            }
//        });
//        Bundle params1 = new Bundle();
//        params1.putString("fields", "place");
//        params.putString("limit", "1000");
//        params.putString("type", "uploaded");
//        request1.setParameters(params1);
//        request1.executeAsync();
//    }

    private void start_location_service() {
        //starting location tracking service
        Intent i = new Intent(getApplicationContext(), Location_Service.class);
        startService(i);
    }


}
