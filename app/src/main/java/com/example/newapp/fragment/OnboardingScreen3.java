package com.example.newapp.fragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.newapp.R;
import com.example.newapp.callbackinterface.OnboardingInterface;

public class OnboardingScreen3 extends Fragment {

    private TextView caption;

    private OnboardingInterface onboardingInterface;

    public void setOnboardingInterface(OnboardingInterface onboardingInterface) {
        this.onboardingInterface = onboardingInterface;
    }

    public OnboardingScreen3() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View v = inflater.inflate(R.layout.fragment_onboarding_screen3, container, false);

        //Initialise view elements
        Button nextbtn = v.findViewById(R.id.nextbtn3);
        caption = v.findViewById(R.id.description3);

        //set onclick listener for next button
        nextbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //if permission not given, ask for permission
                if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(v.getContext(), Manifest
                        .permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(v.getContext(), Manifest.permission.
                                ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION
                    }, 2);
                } else {
                    onboardingInterface.deviceRegistered();
                }
            }
        });
        return v;
    }

    //check result of permission prompt
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 2) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED || grantResults[1] != PackageManager.PERMISSION_GRANTED) {

                //permission not given
                String str = "We can not work without the permissions. Please allow them.";
                caption.setText(str);
            } else {

                //permission given. ready to go
                onboardingInterface.deviceRegistered();
            }
        }
    }
}
