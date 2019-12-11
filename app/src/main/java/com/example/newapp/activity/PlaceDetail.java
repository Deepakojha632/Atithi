package com.example.newapp.activity;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.newapp.R;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PlaceDetail extends AppCompatActivity {
    String serverurl = "http://arnab882.heliohost.org";
    private RelativeLayout panelbg;
    private ImageView bgoverlay, bgimage;
    private TextView placetitle, placerating, placedistance, placedetails, placetags;
    //private Button nav, rating;
    private Location curlocation;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private String TAG = "PlaceDetail";
    private int place_id;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_detail2);

        placetitle = findViewById(R.id.place_title);
        placerating = findViewById(R.id.place_rating);
        placedistance = findViewById(R.id.place_distance);
        placedetails = findViewById(R.id.place_details);
        placetags = findViewById(R.id.tags);
        bgimage = findViewById(R.id.place_image);
        //nav = findViewById(R.id.navigation);
        //rating = findViewById(R.id.rating);


        //getting values passed to this intent
        place_id = getIntent().getIntExtra("placeid", 0);
        SlidingUpPanelLayout slidingUpPanelLayout = findViewById(R.id.place_panel);
        bgoverlay = findViewById(R.id.bgoverlay);
        panelbg = findViewById(R.id.place_slide_panel);

        //set content background
        panelbg.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int h = panelbg.getHeight();
                int w = panelbg.getWidth();
                setImageHeight(h, w);
            }
        });

        //get current location
        Log.e(TAG, "onCreate: initialising location manager");
        locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                curlocation = location;
                Log.e(TAG, "onLocationChanged: got location");
                Log.e(TAG, "onLocationChanged: " + location.toString());
                locationManager.removeUpdates(locationListener);
                getInfo(location);
                //fetchdata f = new fetchdata();
                //f.execute();
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
        if (locationManager != null) {
            Log.e(TAG, "onCreate: Calling location");
            locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, locationListener, null);
        }

        //handle sliding event
        slidingUpPanelLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                bgoverlay.setAlpha(slideOffset);
                TextView slidemsg = findViewById(R.id.place_scroll_msg);
                if (slideOffset == 0) {
                    placetitle.setTextColor(Color.parseColor("#ffffff"));
                    placerating.setTextColor(Color.parseColor("#ffffff"));
                    placedistance.setTextColor(Color.parseColor("#ffffff"));
                    slidemsg.setVisibility(View.VISIBLE);
                } else {
                    placetitle.setTextColor(Color.parseColor("#000000"));
                    placerating.setTextColor(Color.parseColor("#000000"));
                    placedistance.setTextColor(Color.parseColor("#000000"));
                    slidemsg.setVisibility(View.GONE);
                }
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {

            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    private void getInfo(Location location) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(serverurl + "/getplacedetail.php?place_id=" + place_id + "&lat=" + location.getLatitude() + "&lon=" + location.getLongitude())
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.e(TAG, "onFailure: Could not fetch data");
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                final String data = response.body().string();
                Log.e(TAG, "onResponse: " + data);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            loadUi(data);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    private void loadUi(String data) throws JSONException {
        JSONObject jsonObject = new JSONObject(data);
        if (!jsonObject.getBoolean("error")) {
            JSONArray jsonArray = jsonObject.getJSONArray("message");
            jsonObject = jsonArray.getJSONObject(0);
            Log.e(TAG, "loadUi: " + jsonObject.get("place_name").toString());
            placetitle.setText(jsonObject.get("place_name").toString());
            placedetails.setText(jsonObject.get("place_description").toString());
            String dist = jsonObject.get("distance").toString() + " KM";
            placedistance.setText(dist);
            placerating.setText(jsonObject.get("place_rating").toString());
            jsonArray = jsonObject.getJSONArray("tags");
            String tags = "";
            for (int i = 0; i < jsonArray.length() - 1; i++) {
                String tag = jsonArray.get(i).toString();
                char[] temp = tag.toCharArray();
                temp[0] = Character.toUpperCase(temp[0]);
                tag = String.valueOf(temp);
                tags += tag + ", ";
            }
            String tag = jsonArray.get(jsonArray.length() - 1).toString();
            char[] temp = tag.toCharArray();
            temp[0] = Character.toUpperCase(temp[0]);
            tag = String.valueOf(temp);
            tags += tag;
            placetags.setText(tags);
            Glide.with(this)
                    .asBitmap()
                    .load(serverurl + "/image/" + place_id + ".jpeg")
                    .into(bgimage);
        }
    }

    //method that sets the background of sliding panel
    private void setImageHeight(int h, int w) {
        RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(w, h);
        bgoverlay.setLayoutParams(p);
    }

}
