package com.example.newapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class Onboarding extends AppCompatActivity implements OnboardingInterface{

    private SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);
        switchScreen(1);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    }


    @Override
    public void switchScreen(int flag) {
        switch (flag)
        {
            case 1:
                //switch to first screen
                OnboardingScreen1 screen1 = new OnboardingScreen1();
                screen1.setOnboardingInterface(this);
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.onboarding_container, screen1)
                        .commit();
                break;
            case 2:
                //switch to second fragment
                OnboardingScreen2 screen2 = new OnboardingScreen2();
                screen2.setOnboardingInterface(this);
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.onboarding_container, screen2)
                        .commit();
                break;
            case 3:
                //switch to third fragment
                OnboardingScreen3 screen3 = new OnboardingScreen3();
                screen3.setOnboardingInterface(this);
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.onboarding_container, screen3)
                        .commit();
                break;
            default:
                Log.e("Onboarding", "Switch screen flag out of bound");

        }

    }

    @Override
    public void deviceRegistered() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("registered", true);
        editor.apply();
        Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);
    }
}
