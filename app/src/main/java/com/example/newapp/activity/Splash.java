package com.example.newapp.activity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.newapp.R;
import com.example.newapp.service.Location_Service;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Splash extends AppCompatActivity {
    private String serverurl = "http://arnab882.heliohost.org";
    private SharedPreferences runtime;
    private SharedPreferences.Editor runtimeeditor;
    private String TAG = "Splash";
    private static boolean result = false;
    private static AlertDialog alertDialog;

    private static boolean isConnectionAvailable(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();
        return info != null && manager.getActiveNetworkInfo().isConnected();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        runtime = PreferenceManager.getDefaultSharedPreferences(this);

        //checking internet connectivity
        final AlertDialog.Builder b = new AlertDialog.Builder(this);
        if (!InternetConnection.checkConnection(getApplicationContext())) {
            b.setCancelable(false);
            b.setTitle(getResources().getString(R.string.app_name));
            b.setMessage("It seems you dont have internet connection. Please connect to the internet and retry.");
            b.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (InternetConnection.checkConnection(getApplicationContext())) {
                        //post activity data to server
                        postActivityToServer();
                        //check if device is registered
                        checkDeviceRegistration();
                    } else {
                        alertDialog = b.create();
                        alertDialog.show();
                    }
                }
            });
            b.setNegativeButton("Close", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Splash splash = new Splash();
                    splash.finishAffinity();
                    System.exit(0);
                }
            });
            alertDialog = b.create();
            alertDialog.show();
        } else {
            //post activity data to server
            postActivityToServer();

            //check if device is registered
            checkDeviceRegistration();
        }
    }

    private void checkDeviceRegistration() {
        if (runtime.getBoolean("registered", false)) {
            //device already registered
            if (checkLogin()) {
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
    private boolean checkLogin() {
        //user logged in
        //user not logged in
        return AccessToken.getCurrentAccessToken() != null;
    }

    //If user is logged in
    private void loginSuccessful(AccessToken newtoken) {
        GraphRequest request = GraphRequest.newMeRequest(newtoken, new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                try {
                    if (object != null) {
                        //set runtime object with user's details
                        runtimeeditor = runtime.edit();
                        runtimeeditor.putString("userid", object.getString("id"));
                        runtimeeditor.putString("first_name", object.getString("first_name"));
                        runtimeeditor.putString("last_name", object.getString("last_name"));
                        runtimeeditor.apply();
                        Toast.makeText(Splash.this, "Logged in as: " + object.getString("first_name") + " " + object.getString("last_name"), Toast.LENGTH_SHORT).show();
                        checkLocationPermission();
                        //Start background service
                        start_location_service();
                        //call Home activity
                        Intent i = new Intent(Splash.this, Home.class);
                        startActivity(i);
                        finish();
                    }
                } catch (JSONException | NullPointerException e) {
                    Toast.makeText(getApplicationContext(), "Oops! Something went wrong!" + e.getMessage(), Toast.LENGTH_SHORT).show();
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
        ContextCompat.startForegroundService(getApplicationContext(), new Intent(getApplicationContext(), Location_Service.class));
    }

    private void checkLocationPermission() {
        //check and ask for permission from the user
        if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(this, Manifest
                .permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.
                        ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, 2);
        }
    }

    private void postActivityToServer() {
        final File file = new File(getFilesDir(), "activity");
        if (file.exists()) {
            try {
                String data = "";
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String line;
                while ((line = reader.readLine()) != null) {
                    data += line;
                }
                reader.close();
                OkHttpClient client = new OkHttpClient();
                RequestBody formbody = new FormBody.Builder()
                        .add("activity", data)
                        .build();
                Request request = new Request.Builder()
                        .url(serverurl + "/trackactivity.php")
                        .post(formbody)
                        .build();
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        Log.d(TAG, "onFailure: Call failed");
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        String resp = response.body().string();
                        Log.d(TAG, "onResponse: " + resp);
                        if (Integer.parseInt(resp) == 1) {
                            file.delete();
                        }
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
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
            }
        }
    }

    /*public static boolean showNoConnectionDialog(final Context c) {
        if (isConnectionAvailable(c)) {
            result = true;
            return result;
        } else {
            Splash.b.setTitle("No Connection");
            Splash.b.setMessage("Cannot connect to the internet!");
            Splash.b.setPositiveButton("Retry", new AlertDialog.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (!isConnectionAvailable(c))
                        showNoConnectionDialog(c);
                    else {
                        result = true;
                    }
                }
            });
            Splash.b.setNegativeButton("Exit", new AlertDialog.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    ((Activity) c).finish();
                }
            });
            Splash.alertDialog = Splash.b.create();
            Splash.alertDialog.show();
        }
        return result;
    }*/
}
