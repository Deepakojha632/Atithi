package com.example.newapp.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.newapp.R;
import com.example.newapp.callbackinterface.OnboardingInterface;
import com.example.newapp.fragment.OnboardingScreen1;
import com.example.newapp.fragment.OnboardingScreen2;
import com.example.newapp.fragment.OnboardingScreen3;

public class Onboarding extends AppCompatActivity implements OnboardingInterface {

    private SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
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
