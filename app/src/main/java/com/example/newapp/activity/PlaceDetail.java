package com.example.newapp.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.example.newapp.R;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PlaceDetail extends AppCompatActivity implements View.OnClickListener {
    String serverurl = "http://arnab882.heliohost.org";
    private RelativeLayout panelbg;
    private static String dest = "";
    FrameLayout.LayoutParams params;
    private ImageView bgoverlay, bgimage;
    private TextView placetitle, placerating, placedistance, placedetails, placetags;
    //private Button nav, rating;
    private Location curlocation;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private String TAG = "PlaceDetail";
    private int place_id;
    private ImageButton navigateBtn;
    private CardView placePic, slidingPane, placeImageHolder;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_detail2);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        placetitle = findViewById(R.id.place_title);
        placerating = findViewById(R.id.place_rating);
        placedistance = findViewById(R.id.place_distance);
        placedetails = findViewById(R.id.place_details);
        placePic = findViewById(R.id.place_pic);
        placetags = findViewById(R.id.tags);
        bgimage = findViewById(R.id.place_image);
        slidingPane = findViewById(R.id.slidingPane);
        navigateBtn = findViewById(R.id.navigation);
        bgoverlay = findViewById(R.id.bgoverlay);
        panelbg = findViewById(R.id.place_slide_panel);
        placeImageHolder = findViewById(R.id.place_image_holder);
        params = (FrameLayout.LayoutParams) placeImageHolder.getLayoutParams();
        placePic.setCardBackgroundColor(Color.TRANSPARENT);

        //getting values passed to this intent
        place_id = getIntent().getIntExtra("placeid", 0);
        SlidingUpPanelLayout slidingUpPanelLayout = findViewById(R.id.place_panel);

        //set content background
        panelbg.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int h = panelbg.getHeight();
                int w = panelbg.getWidth();
                setImageHeight(h, w);
            }
        });
        navigateBtn.setOnClickListener(this);

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
                    slidingPane.setAlpha(1f);
                    slidemsg.setVisibility(View.VISIBLE);
                    if (params.gravity == Gravity.TOP)
                        slideDownAnimation();

                } else {
                    placetitle.setTextColor(Color.parseColor("#000000"));
                    placerating.setTextColor(Color.parseColor("#000000"));
                    placedistance.setTextColor(Color.parseColor("#000000"));
                    slidingPane.setAlpha(slideOffset);
                    slidemsg.setVisibility(View.GONE);
                    if (params.gravity == Gravity.CENTER)
                        slideUpAnimation();
                }
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {

            }
        });
    }

    private void slideUpAnimation() {
        TranslateAnimation animation = new TranslateAnimation(0, 0, 0, -480);
        animation.setDuration(300);
        animation.setFillAfter(false);
        animation.setAnimationListener(new MyAnimationListener());
        placeImageHolder.startAnimation(animation);
    }

    private void slideDownAnimation() {
        TranslateAnimation animation = new TranslateAnimation(0, 0, 0, 480);
        animation.setDuration(300);
        animation.setFillAfter(false);
        animation.setAnimationListener(new DownAnimationListener());
        placeImageHolder.startAnimation(animation);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.navigation) {
            String address = placetitle.getText().toString();
            if (!address.equalsIgnoreCase("Atithi")) {
                String locAdd = PlaceDetail.dest;
                String[] latLong = locAdd.split(":");
                Log.e(TAG, "latitude:longitude: " + latLong[0] + ":" + latLong[1]);
                /*String[] location = locAdd.split(" ");
                System.out.println("Length of Address: " + location.length);
                for (String i : location) {
                    System.out.print(i + " ");
                }*/

                Uri.Builder builder = new Uri.Builder();
                builder.scheme("https")
                        .authority("www.google.com")
                        .appendPath("maps")
                        .appendPath("dir")
                        .appendPath("")
                        .appendQueryParameter("api", "1")
                        .appendQueryParameter("destination", latLong[0] + "," + latLong[1]);
                String url = builder.build().toString();
                Log.d("Directions", url);
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        }
    }

    private void getInfo(Location location) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(1, TimeUnit.MINUTES).writeTimeout(1, TimeUnit.MINUTES).readTimeout(1, TimeUnit.MINUTES);    // socket timeout
        OkHttpClient client = builder.build();
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
                if (getApplicationContext() == null)
                    return;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (data != null) {
                                loadUi(data);
                                String address = placetitle.getText().toString();
                                GeocodingLocation locationAddress = new GeocodingLocation();
                                GeocodingLocation.getAddressFromLocation(address, getApplicationContext(), new GeocoderHandler());
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, e.getMessage());
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
            Glide.with(getApplicationContext())
                    .asBitmap()
                    .load(serverurl + "/image/" + place_id + ".jpeg")
                    .into(bgimage);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private class MyAnimationListener implements Animation.AnimationListener {

        @Override
        public void onAnimationEnd(Animation animation) {
            placeImageHolder.clearAnimation();
            if (params.gravity == Gravity.CENTER) {
                params.gravity = Gravity.TOP;
                placeImageHolder.setLayoutParams(params);
            }
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }

        @Override
        public void onAnimationStart(Animation animation) {
        }
    }

    private class DownAnimationListener implements Animation.AnimationListener {

        @Override
        public void onAnimationEnd(Animation animation) {
            placeImageHolder.clearAnimation();
            if (params.gravity == Gravity.TOP) {
                params.gravity = Gravity.CENTER;
                placeImageHolder.setLayoutParams(params);
            }
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }

        @Override
        public void onAnimationStart(Animation animation) {
        }
    }

    //method that sets the background of sliding panel
    private void setImageHeight(int h, int w) {
        RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(w, h);
        bgoverlay.setLayoutParams(p);
    }

    private class GeocoderHandler extends Handler {
        @Override
        public void handleMessage(Message message) {
            String locationAddress;
            switch (message.what) {
                case 1:
                    Bundle bundle = message.getData();
                    locationAddress = bundle.getString("address");
                    break;
                default:
                    locationAddress = null;
            }
            PlaceDetail.dest = locationAddress;
        }
    }
}
