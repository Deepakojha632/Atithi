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

public class RecommendedAdapter extends RecyclerView.Adapter<RecommendedAdapter.ViewHolder> {
    private String serverurl = "http://arnab882.heliohost.org";

    private String TAG = "Recommended adapter";
    private Context context;
    private HomeFragmentCallBack callBack;
    private ArrayList<String> id = new ArrayList<>();
    private ArrayList<String> name = new ArrayList<>();
    private ArrayList<String> distance = new ArrayList<>();
    private ArrayList<String> rating = new ArrayList<>();

    public RecommendedAdapter(Context context, JSONArray jsonArray, HomeFragmentCallBack callBack) throws JSONException {
        this.context = context;
        this.callBack = callBack;
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            this.id.add(jsonObject.get("place_id").toString());
            this.name.add(jsonObject.get("place_name").toString());
            this.distance.add(jsonObject.get("distance").toString());
            this.rating.add(jsonObject.get("place_rating").toString());
        }
        Log.e(TAG, "RecommendedAdapter: Constructor finished");
    }

    @NonNull
    @Override
    public RecommendedAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recommended_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecommendedAdapter.ViewHolder holder, final int position) {
        holder.name.setText(name.get(position));
        String dist = distance.get(position) + " KM";
        holder.distance.setText(dist);
        holder.rating.setText(rating.get(position));
        Glide.with(context)
                .asBitmap()
                .load(serverurl + "/image/" + id.get(position) + ".jpeg")
                .into(holder.icon);
        holder.icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callBack.placeClicked(Integer.parseInt(id.get(position)));
            }
        });

    }

    @Override
    public int getItemCount() {
        Log.e(TAG, "getItemCount: Count = " + id.size());
        return id.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView name, distance, rating;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.recommendedcardimage);
            name = itemView.findViewById(R.id.recommendedcardtitle);
            distance = itemView.findViewById(R.id.recommendedcarddistance);
            rating = itemView.findViewById(R.id.recommendedcardrating);
        }
    }
}