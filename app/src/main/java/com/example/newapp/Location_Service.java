package com.example.newapp;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class Location_Service extends Service {

    private LocationManager locationManager;
    private LocationListener locationListener;

    double lat, lon;

    String provider;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onCreate() {
        Log.d("LocationManager", "Location service started");
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                lat = location.getLatitude();
                lon = location.getLongitude();
                RuntimeData.getInstance().locationlat = location.getLatitude();
                RuntimeData.getInstance().locationlon = location.getLongitude();
                Log.d("LocationManager", lat + ", " + lon);
                Log.d("LocationManager", provider);
                //Toast.makeText(getApplicationContext(), lat + ", " + lon, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {
                Toast.makeText(getApplicationContext(), "Please turn on GPS!", Toast.LENGTH_SHORT).show();
            }
        };

        Criteria c = new Criteria();
        c.setAltitudeRequired(false);
        c.setAccuracy(Criteria.ACCURACY_FINE);

        try{
            locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
            if (locationManager != null) {
                provider = locationManager.getBestProvider(c, true);
                Log.i("LocationService", provider);
                locationManager.requestLocationUpdates(provider, 0, 5, locationListener);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onDestroy() {
        stopSelf();
        super.onDestroy();
        if (locationManager != null){
            locationManager.removeUpdates(locationListener);
        }
    }
}
