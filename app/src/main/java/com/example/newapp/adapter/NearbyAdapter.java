package com.example.newapp.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.newapp.R;
import com.example.newapp.callbackinterface.HomeFragmentCallBack;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class NearbyAdapter extends RecyclerView.Adapter<NearbyAdapter.ViewHolder>{

    private static final String TAG = "NearbyAdapter";
    private HomeFragmentCallBack homeFragmentCallBack;

    String serverurl = "http://arnab882.heliohost.org";

    //vars
    private ArrayList<String> id = new ArrayList<String>();
    private ArrayList<String> title = new ArrayList<String>();
    private ArrayList<String> rating = new ArrayList<String>();
    private ArrayList<String> distance = new ArrayList<String>();
    private JSONArray arr;
    private Context mcontext;

    public NearbyAdapter(Context mcontext, JSONArray jsonArray, HomeFragmentCallBack h) throws JSONException {
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            this.id.add(jsonObject.get("place_id").toString());
            this.title.add(jsonObject.get("place_name").toString());
            this.distance.add(jsonObject.get("distance").toString());
            this.rating.add(jsonObject.get("place_rating").toString());
        }
        this.mcontext = mcontext;
        this.homeFragmentCallBack = h;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder called");
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.nearby_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        Log.d(TAG, "onBindViewHolder called");
        String iconurl = serverurl + "/image/" + id.get(position) + ".jpeg";
        Glide.with(mcontext)
                .asBitmap()
                .load(iconurl)
                .into(holder.icon);
        holder.title.setText(title.get(position));
        String str = distance.get(position) + " KM";
        holder.distance.setText(str);
        holder.rating.setText(rating.get(position));
        holder.icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "clicked on image: " + position);
                homeFragmentCallBack.placeClicked(Integer.parseInt(id.get(position)));
            }
        });
    }

    @Override
    public int getItemCount() {
        return id.size();
    }

    //viewholder class
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView title, rating, distance;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.nearbycardimage);
            title = itemView.findViewById(R.id.nearbycardtitle);
            rating = itemView.findViewById(R.id.nearbycardrating);
            distance = itemView.findViewById(R.id.nearbycarddistance);
        }
    }
}
