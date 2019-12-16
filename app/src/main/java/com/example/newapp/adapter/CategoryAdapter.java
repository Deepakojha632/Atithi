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

import com.example.newapp.R;
import com.example.newapp.activity.Home;
import com.example.newapp.callbackinterface.HomeFragmentCallBack;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {
    private ArrayList<String> id = new ArrayList<>();
    private ArrayList<String> name = new ArrayList<>();
    private HomeFragmentCallBack callBack;
    private String TAG = "Category adapter";

    public CategoryAdapter(JSONArray jsonArray, HomeFragmentCallBack callBack) throws JSONException {
        this.callBack = callBack;
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            id.add(jsonObject.get("cat_id").toString());
            name.add(jsonObject.get("cat_name").toString());
            Log.e(TAG, "CategoryAdapter: Constructor finished");
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_card, parent, false);
        return new ViewHolder(view);
    }

    public static int getResourseId(Context context, String pVariableName, String pResourcename, String pPackageName) throws RuntimeException {
        try {
            return context.getResources().getIdentifier(pVariableName, pResourcename, pPackageName);
        } catch (Exception e) {
            throw new RuntimeException("Error getting Resource ID.", e);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        char[] temp = name.get(position).toCharArray();
        temp[0] = Character.toUpperCase(temp[0]);
        String catname = String.valueOf(temp);
        holder.name.setText(catname);
        int imageId = getResourseId(holder.getContext(), catname.toLowerCase(), "drawable", Home.PACKAGE_NAME);
        holder.icon.setImageResource(imageId);
        Log.i(TAG, "Category: " + catname);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callBack.categoryClicked(id.get(position), name.get(position));
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
        TextView name;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.caticon);
            name = itemView.findViewById(R.id.catname);
        }

        public Context getContext() {
            return itemView.getContext();
        }
    }
}
