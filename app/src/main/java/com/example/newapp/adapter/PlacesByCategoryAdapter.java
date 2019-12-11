package com.example.newapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.newapp.R;
import com.example.newapp.callbackinterface.PlaceByCategoryCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class PlacesByCategoryAdapter extends RecyclerView.Adapter<PlacesByCategoryAdapter.ViewHolder> {
    private ArrayList<String> id = new ArrayList<>();
    private ArrayList<String> name = new ArrayList<>();
    private ArrayList<String> rating = new ArrayList<>();
    private ArrayList<String> description = new ArrayList<>();
    private Context context;
    private PlaceByCategoryCallback callback;

    private String serverurl = "http://arnab882.heliohost.org";

    public PlacesByCategoryAdapter(Context context, JSONArray jsonArray, PlaceByCategoryCallback callback) throws JSONException {
        this.context = context;
        this.callback = callback;
        if (jsonArray.length() > 0) {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                id.add(jsonObject.get("place_id").toString());
                name.add(jsonObject.get("place_name").toString());
                description.add(jsonObject.get("place_description").toString());
                rating.add(jsonObject.get("place_rating").toString());
            }
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.places_by_category_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        holder.name.setText(name.get(position));
        holder.description.setText(description.get(position));
        holder.rating.setText(rating.get(position));
        Glide.with(context).asBitmap()
                .load(serverurl + "/image/" + id.get(position) + ".jpeg")
                .into(holder.icon);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callback.showPlaceDetail(id.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return id.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView name, rating, description;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.pla_by_cat_icon);
            name = itemView.findViewById(R.id.pla_by_cat_name);
            rating = itemView.findViewById(R.id.pla_by_cat_rating);
            description = itemView.findViewById(R.id.pla_by_cat_description);
        }
    }
}
