package com.example.newapp.callbackinterface;

public interface HomeActivityCallback {
    void showPlaceDetails(int placeid);

    void showPlacesByCategory(String catid, String catname);
}
