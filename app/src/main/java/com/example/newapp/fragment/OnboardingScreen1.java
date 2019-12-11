package com.example.newapp.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import com.example.newapp.R;
import com.example.newapp.callbackinterface.OnboardingInterface;

public class OnboardingScreen1 extends Fragment {

    private Button nextbtn;

    private OnboardingInterface onboardingInterface;

    public void setOnboardingInterface(OnboardingInterface onboardingInterface){
        this.onboardingInterface = onboardingInterface;
    }

    public OnboardingScreen1() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_onboarding_screen1, container, false);
        nextbtn = v.findViewById(R.id.nextbtn1);
        nextbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onboardingInterface != null){
                    try{
                        onboardingInterface.switchScreen(2);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }else {
                    Log.e("Onboarding", "callback interface is none");
                }

            }
        });
        return v;
    }
}
