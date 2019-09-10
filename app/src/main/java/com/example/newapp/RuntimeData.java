package com.example.newapp;

public class RuntimeData {
    public static RuntimeData runtimeData = null;
    public double locationlat, locationlon;

    //Constructor
    private RuntimeData(){
        locationlat = 0.0;
        locationlon = 0.0;
    }

    public static synchronized RuntimeData getInstance(){
        if (runtimeData == null){
            runtimeData = new RuntimeData();
        }
        return runtimeData;
    }
}
