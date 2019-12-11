package com.example.newapp.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
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

import com.bumptech.glide.Glide;
import com.example.newapp.R;
import com.example.newapp.adapter.CategoryAdapter;
import com.example.newapp.adapter.NearbyAdapter;
import com.example.newapp.adapter.RecommendedAdapter;
import com.example.newapp.callbackinterface.HomeActivityCallback;
import com.example.newapp.callbackinterface.HomeFragmentCallBack;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HomeFragment extends Fragment implements HomeFragmentCallBack {
    private String serverurl = "http://arnab882.heliohost.org";

    private View v;
    private Context vcontext;
    private TextView greeting, textViewlocation, nextstoptitle, nextstopdistance, nextstoptags, nextstoprating;
    private ImageView nextstopimage;
    private String TAG = "HomeFragment";
    //private Location curlocation;
    private LocationManager locationManager;
    private LocationListener l;
    private HomeActivityCallback callBackInterface;

    private String userid, firstname, lastname;
    private JSONObject userdata;

    public void getCallBackInterface(HomeActivityCallback activityCallback) {
        callBackInterface = activityCallback;
    }

    @SuppressLint({"MissingPermission", "InflateParams"})
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.home_fragment, null);
        vcontext = v.getContext();
        greeting = v.findViewById(R.id.greetingtext);
        textViewlocation = v.findViewById(R.id.locationtext);
        nextstoptitle = v.findViewById(R.id.next_stop_title);
        nextstopimage = v.findViewById(R.id.next_stop_image);
        nextstopdistance = v.findViewById(R.id.next_stop_distance);
        nextstoptags = v.findViewById(R.id.next_stop_tags);
        nextstoprating = v.findViewById(R.id.next_stop_rating);
        Log.e(TAG, "onCreateView: ");
        initUi();
        return v;
    }

    //Initialising UI elements
    @SuppressLint("MissingPermission")
    private void initUi() {
        File file = new File(vcontext.getFilesDir(), "user");
        if (file.exists()) {
            try {
                String data = "";
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String line;
                while ((line = reader.readLine()) != null) {
                    data += line;
                }
                reader.close();
                JSONArray array = new JSONArray(data);
                userdata = array.getJSONObject(0);
                Log.d(TAG, "initUi: JSON user object: " + userdata.toString());
                userid = userdata.getString("userid");
                firstname = userdata.getString("firstname");
                lastname = userdata.getString("lastname");
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }
        Log.v(TAG, "first name: " + firstname);
        greeting.setText(getGreeting(firstname));
        String str = "Waiting for location";
        textViewlocation.setText(str);
        Log.e(TAG, "initUi: ");
        locationManager = (LocationManager) vcontext.getSystemService(Context.LOCATION_SERVICE);
        l = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                Log.e(TAG, "onLocationChanged: " + location.toString());
//                curlocation = location;
                Geocoder geocoder = new Geocoder(vcontext, Locale.getDefault());
                try {
                    List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    String city = "Welcome to ";
                    city += addresses.get(0).getLocality();
                    textViewlocation.setText(city);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                locationManager.removeUpdates(l);
                getRecommended(userid, location);
                //showRecommended(data);
                getNearby(location);
                getCategories();
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
            locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, l, null);
        }
    }

    private String getGreeting(String name) {
        Calendar cal = Calendar.getInstance();
        int hours = cal.get(Calendar.HOUR_OF_DAY);
        String[] msg = {"Good Morning", "Good Noon", "Good Afternoon", "Good Evening", "Good Night"};
        if (hours < 12) {
            return msg[0] + " " + name;
        } else if (hours == 12) {
            return msg[1] + " " + name;
        } else if (hours < 17) {
            return msg[2] + " " + name;
        } else if (hours < 19) {
            return msg[3] + " " + name;
        } else {
            return msg[4] + " " + name;
        }
    }

    @Override
    public void placeClicked(int placeid) {
        callBackInterface.showPlaceDetails(placeid);
    }

    @Override
    public void categoryClicked(String catid, String catname) {
        callBackInterface.showPlacesByCategory(catid, catname);
    }

    private void getNearby(Location location) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(serverurl + "/getnearby.php?lat=" + location.getLatitude() + "&lon=" + location.getLongitude())
                .addHeader("Accept", "application/json; q=0.5")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                try {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(v.getContext(), "There was an error getting nearby places", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (NullPointerException ne) {
                    ne.printStackTrace();
                }

            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                final String data = response.body().string();
                Log.e(TAG, "onResponse: " + data);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            showNearby(data);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    private void showNearby(String data) throws JSONException {
        JSONObject object = new JSONObject(data);
        if (!object.getBoolean("error")) {
            JSONArray jsonArray = object.getJSONArray("message");
            LinearLayoutManager layoutManager = new LinearLayoutManager(v.getContext(), LinearLayoutManager.HORIZONTAL, false);
            RecyclerView recyclerView = v.findViewById(R.id.nearby);
            NearbyAdapter adapter = new NearbyAdapter(v.getContext(), jsonArray, this);
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(layoutManager);
        }
    }

    private void getRecommended(String userid, Location location) {
        OkHttpClient client = new OkHttpClient();
        final String[] data = new String[1];
        final Request request = new Request.Builder()
                .url(serverurl + "/getrecommendation.php?userid=" + userid + "&lat=" + location.getLatitude() + "&lon=" + location.getLongitude())
                .addHeader("Accept", "application/json; q=0.5")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.e(TAG, "onFailure: e.printStackTrace()");
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                Log.e(TAG, "onResponse: calling showrecommended");
                try {
                    data[0] = response.body().string();
                    System.out.println(data[0]);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                showRecommended(data[0]);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
        });
        //return data[0];
    }

    private void showRecommended(String data) throws JSONException {
        Log.e(TAG, "showRecommended: " + data);
        try {
            JSONObject jsonObject = new JSONObject(data);
            if (!jsonObject.getBoolean("error")) {
                final JSONObject nextstop = new JSONObject(jsonObject.get("nextstop").toString());
                JSONArray jsonArray = nextstop.getJSONArray("tags");
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
                String placetitle = nextstop.get("place_name").toString();
                String distance = nextstop.get("distance").toString() + " KM";
                String rating = nextstop.get("place_rating").toString();
                nextstoprating.setText(rating);
                nextstoptitle.setText(placetitle);
                nextstopdistance.setText(distance);
                nextstoptags.setText(tags);
                Glide.with(vcontext).asBitmap().load(serverurl + "/image/" + nextstop.get("place_id").toString() + ".jpeg")
                        .into(nextstopimage);
                nextstopimage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            callBackInterface.showPlaceDetails(Integer.parseInt(nextstop.get("place_id").toString()));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
                Log.e(TAG, "showRecommended: Nextstop card set");
                JSONArray recommendations = jsonObject.getJSONArray("recomendation");
                LinearLayoutManager layoutManager = new LinearLayoutManager(v.getContext(), LinearLayoutManager.HORIZONTAL, false);
                RecyclerView recyclerView = v.findViewById(R.id.recommended);
                RecommendedAdapter adapter = new RecommendedAdapter(v.getContext(), recommendations, this);
                recyclerView.setAdapter(adapter);
                recyclerView.setLayoutManager(layoutManager);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getCategories() {
        OkHttpClient client = new OkHttpClient();
        final Request request = new Request.Builder()
                .url(serverurl + "/getcategories.php")
                .addHeader("Accept", "application/json; q=0.5")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                final String data = response.body().string();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            showCategories(data);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    private void showCategories(String data) throws JSONException {
        JSONArray jsonArray = new JSONArray(data);
        LinearLayoutManager layoutManager = new LinearLayoutManager(vcontext, LinearLayoutManager.HORIZONTAL, false);
        RecyclerView recyclerView = v.findViewById(R.id.categories);
        CategoryAdapter adapter = new CategoryAdapter(jsonArray, this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }
}
