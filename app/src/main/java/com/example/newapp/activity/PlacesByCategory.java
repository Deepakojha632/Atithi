package com.example.newapp.activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newapp.R;
import com.example.newapp.adapter.PlacesByCategoryAdapter;
import com.example.newapp.callbackinterface.PlaceByCategoryCallback;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PlacesByCategory extends AppCompatActivity implements PlaceByCategoryCallback {
    private String serverurl = "http://arnab882.heliohost.org";
    private String TAG = "PlacesByCategories";
    private TextView name;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_places_by_category);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        name = findViewById(R.id.cat_name);
        initui();

    }

    private void initui() {
        String category_name = this.getIntent().getStringExtra("cat_name");
        String category_id = this.getIntent().getStringExtra("cat_id");
        if (category_name != null) {
            char[] temp = category_name.toCharArray();
            temp[0] = Character.toUpperCase(temp[0]);
            category_name = String.valueOf(temp);
        }
        name.setText(category_name);
        getPlaces(category_id);
    }

    private void getPlaces(String category_id) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(1, TimeUnit.MINUTES).writeTimeout(1, TimeUnit.MINUTES).readTimeout(1, TimeUnit.MINUTES);    // socket timeout
        OkHttpClient client = builder.build();
        Request request = new Request.Builder()
                .url(serverurl + "/getplacesbycategory.php?interest_id=" + category_id)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Failed to load places data", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                final String data = response.body().string();
                if (getApplicationContext() == null)
                    return;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (data != null)
                                showPlaces(data);
                        } catch (JSONException e) {
                            Log.e(TAG, e.getMessage());
                        }
                    }
                });
            }
        });
    }

    private void showPlaces(String data) throws JSONException {
        JSONArray jsonArray = new JSONArray(data);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        RecyclerView recyclerView = findViewById(R.id.places);
        PlacesByCategoryAdapter adapter = new PlacesByCategoryAdapter(this, jsonArray, this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

    }

    @Override
    public void showPlaceDetail(String placeid) {
        Intent intent = new Intent(this, PlaceDetail.class);
        intent.putExtra("placeid", Integer.parseInt(placeid));
        startActivity(intent);
    }
}
