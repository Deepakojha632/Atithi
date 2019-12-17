package com.example.newapp.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.newapp.R;
import com.example.newapp.activity.Splash;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;


public class Location_Service extends Service {
    LocationManager locationManager = null;
    private String TAG = "Location_Service";
    private String userid;
    private String CHANNEL_ID = "atithinotification";

    @SuppressLint("MissingPermission")
    @Override
    public void onCreate() {
        super.onCreate();
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        getUserInfo();
        createNotificationChannel();
        showPersistentNotification();

        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                writeLocation(location);
//                File file = new File(getFilesDir(), "activity");
//                file.delete();
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };
        locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 5000, 0, locationListener);
    }

    private void getUserInfo() {
        File userfile = new File(getFilesDir(), "user");
        if (userfile.exists()) {
            try {
                String data = "";
                BufferedReader reader = new BufferedReader(new FileReader(userfile));
                String line;
                while ((line = reader.readLine()) != null) {
                    data += line;
                }
                reader.close();
                JSONArray array = new JSONArray(data);
                JSONObject userdata = array.getJSONObject(0);
                Log.d(TAG, "initUi: JSON user object: " + userdata.toString());
                userid = userdata.getString("userid");
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void writeLocation(Location location) {
        File file = new File(getFilesDir(), "activity");
        if (file.exists()) {
            try {
                String data = "";
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String line;
                while ((line = reader.readLine()) != null) {
                    data += line;
                }
                reader.close();
                Log.d(TAG, "writeLocation: Filedata = " + data);
//                String writedata = data.toString();
                char[] temp = data.toCharArray();
                temp[temp.length - 1] = ',';
                data = String.valueOf(temp);
                data += "{\"userid\":\"" + userid + "\",\"lat\":\"" + location.getLatitude() + "\",\"lon\":\"" + location.getLongitude() + "\"}]";
                Log.d(TAG, "onLocationChanged: filedata to write: " + data);
                FileOutputStream fos = getApplicationContext().openFileOutput("activity", MODE_PRIVATE);
                fos.write(data.getBytes());
                fos.close();
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
        } else {
            try {
                FileOutputStream fos = getApplicationContext().openFileOutput("activity", MODE_PRIVATE);
                String data = "[{\"userid\":\"" + userid + "\",\"lat\":\"" + location.getLatitude() + "\",\"lon\":\"" + location.getLongitude() + "\"}]";
                fos.write(data.getBytes());
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void showPersistentNotification() {
        Intent intent = new Intent(this, Splash.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Atithi at your service")
                .setContentText("Tap to open the app")
                .setSmallIcon(R.drawable.ic_explore)
                .setContentIntent(pendingIntent)
                .build();
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        startForeground(1, notification);
    }

    private void createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Atithi",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(notificationChannel);
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
