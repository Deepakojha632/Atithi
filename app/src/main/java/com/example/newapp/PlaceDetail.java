package com.example.newapp;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Locale;

public class PlaceDetail extends AppCompatActivity {

    private RelativeLayout panelbg;
    private ImageView bgoverlay;
    private TextView placetitle, placerating, placedistance, placedetails;

    private int place_id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_detail2);

        //getting values passed to this intent
        place_id = getIntent().getIntExtra("placeid", 0);
        //Toast.makeText(this, place_id+"", Toast.LENGTH_SHORT).show();
        //initialising ui elements
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


        //handle sliding event
        slidingUpPanelLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                bgoverlay.setAlpha(slideOffset);
                TextView slidemsg = findViewById(R.id.place_scroll_msg);
                if(slideOffset == 0){
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

        fetchdata f = new fetchdata();
        f.execute();
    }

    //method that sets the background of sliding panel
    private void setImageHeight(int h, int w) {
        RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(w, h);
        bgoverlay.setLayoutParams(p);
    }

    class fetchdata extends AsyncTask{
        private BufferedReader reader;

        //method that fetches data and populates reader object
        void fetchData(){
            try {
                URL url = new URL("http://13.127.126.240/getplacedetail.php?place_id=" + place_id +
                        "&lat=" + RuntimeData.getInstance().locationlat + "&lon=" + RuntimeData.getInstance().locationlon);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            fetchData();
            while (reader == null){
                fetchData();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            try{
                String s = "";
                String line = reader.readLine();
                while (line != null){
                    s = s + line;
                    line = reader.readLine();
                }
                JSONObject jsonObject = new JSONObject(s);
                if (!jsonObject.getBoolean("error")){
                    JSONArray  arr = jsonObject.getJSONArray("message");
                    jsonObject = arr.getJSONObject(0);
                    initUi(jsonObject);
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }


        }
    }

    private void initUi(JSONObject jsonObject) {
        placetitle = findViewById(R.id.place_title);
        placerating = findViewById(R.id.place_rating);
        placedistance = findViewById(R.id.place_distance);
        placedetails = findViewById(R.id.place_details);
        ImageView bgimage = findViewById(R.id.place_image);
        try {
            placetitle.setText(jsonObject.get("place_name").toString());
            placedistance.setText(jsonObject.get("distance").toString() + " KM");
            placedetails.setText(jsonObject.get("place_description").toString());
            Glide.with(this).asBitmap().load("http://13.127.126.240/image/" + place_id + ".jpeg").into(bgimage);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


}
