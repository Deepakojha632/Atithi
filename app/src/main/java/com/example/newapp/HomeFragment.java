package com.example.newapp;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.ReferenceQueue;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class HomeFragment  extends Fragment implements HomeFragmentCallBack{
    private View v;
    private Context vcontext;
    private TextView greeting, location;

    private SharedPreferences runtime;

    private HomeActivityCallback callBackInterface;

    public void getCallBackInterface(HomeActivityCallback activityCallback) {
        callBackInterface = activityCallback;
    }

    /*
    @Override
    public void onResume() {
        super.onResume();
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                location.setText(Double.toString(intent.getExtras().getDouble("location_lat")));

            }
        };
        this.getContext().registerReceiver(broadcastReceiver, new IntentFilter("location_update"));
    }*/

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.home_fragment, null);
        vcontext = v.getContext();
        //Bundle args = getArguments();
        greeting = v.findViewById(R.id.greetingtext);
        //usericon = v.findViewById(R.id.usericon);
        location = v.findViewById(R.id.locationtext);
        runtime = PreferenceManager.getDefaultSharedPreferences(this.getContext());
        initUi();
        /*if (callBackInterface != null){
            Bundle bundle = getArguments();
            try {
                String fname = bundle.getString("firstname");
                callBackInterface.callBackMethod("Welcome " + fname);;
            }catch (NullPointerException e){
                e.printStackTrace();
            }

        }*/
        return v;
    }

    //Initialising UI elements
    private void initUi(){
        greeting.setText(getGreeting(runtime.getString("first_name", "")));
        String str = "Waiting for location";
        location.setText(str);
        //usericon.setImageURI(Uri.parse("http://graph.facebook.com/" + bundle.getString("id") + "/picture?type=square"));
        //Glide.with(this).load("http://graph.facebook.com/" + runtime.getString("userid", "") + "/picture?type=large").into(usericon);
        SetLocation s = new SetLocation();
        s.execute();
    }

    private String getGreeting(String name){
        Calendar cal = Calendar.getInstance();
        int hours = cal.get(Calendar.HOUR_OF_DAY);
        String msg[] = new String[5];
        msg[0] = "Good Morning";
        msg[1] = "Good Noon";
        msg[2] = "Good Afternoon";
        msg[3] = "Good Evening";
        msg[4] = "Good Night";

        if (hours < 12){
            String s = msg[0] + " " + name;
            return s;
        }else if(hours == 12){
            String s = msg[1] + " " + name;
            return s;
        }else if(hours > 12 && hours < 17){
            String s = msg[2] + " " + name;
            return s;
        }else if(hours >= 17 && hours < 19){
            String s = msg[3] + " " + name;
            return s;
        }else {
            String s = msg[4] + " " + name;
            return s;
        }
    }

    @Override
    public void placeClicked(int placeid) {
        callBackInterface.showPlaceDetails(placeid);
    }

    private class SetLocation extends AsyncTask<String, String, String>{

        @Override
        protected String doInBackground(String... strings) {
            while (!showLocation()){
                showLocation();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            //trigger http requests
            LoadNearby l = new LoadNearby();
            l.execute();
        }
    }

    private boolean showLocation(){
        if (RuntimeData.getInstance().locationlat == 0 || RuntimeData.getInstance().locationlon == 0){
            //location not fixed
            return false;
        } else {
            //get city name
            try {
                Geocoder geocoder = new Geocoder(this.getContext(), Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocation(RuntimeData.getInstance().locationlat, RuntimeData.getInstance().locationlon, 1);
                String city = "Welcome to ";
                city += addresses.get(0).getLocality();
                location.setText(city);
            } catch (IOException e){
                e.printStackTrace();
            }
            return true;

        }
    }

    private void initRecyclerview(View v, ArrayList<String> id, ArrayList<String> title, ArrayList<String> distance, ArrayList<String> rating){
        LinearLayoutManager layoutManager = new LinearLayoutManager(v.getContext(), LinearLayoutManager.HORIZONTAL, false);
        RecyclerView recyclerView = v.findViewById(R.id.nearby);
        recyclerView.setLayoutManager(layoutManager);
        NearbyAdapter adapter;

        adapter = new NearbyAdapter(v.getContext(), id, title, distance, rating, this);

        recyclerView.setAdapter(adapter);
    }

    private class LoadNearby extends AsyncTask{
        private BufferedReader reader;

        private void fetchData(){
            try{
                URL requesturl = new URL("http://13.127.126.240/getnearby.php?lat=" + RuntimeData.getInstance().
                        locationlat + "&lon=" + RuntimeData.getInstance().locationlon);
                HttpURLConnection urlConnection = (HttpURLConnection) requesturl.openConnection();
                reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            fetchData();
            while(reader == null){
                fetchData();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            String data = "";
            ArrayList<String> id = new ArrayList<String>();
            ArrayList<String> title = new ArrayList<String>();
            ArrayList<String> rating = new ArrayList<String>();
            ArrayList<String> distance = new ArrayList<String>();
            try {
                String line = "";
                while (line != null){
                    line = reader.readLine();
                    data = data + line;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.d("resp", data);
            try {
                JSONObject jsonObject = new JSONObject(data);
                if(!jsonObject.getBoolean("error")){
                    JSONArray array = jsonObject.getJSONArray("message");
                    for (int i = 0; i < array.length(); i++){
                        JSONObject object = array.getJSONObject(i);
                        id.add(object.get("place_id").toString());
                        title.add(object.get("place_name").toString());
                        distance.add(object.get("distance").toString());
                    }
                    Log.d("JSON", array.toString());
                } else {
                    Log.e("JSON Parsing", jsonObject.get("message").toString());
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
            initRecyclerview(v, id, title, distance, rating);
        }
    }



}
