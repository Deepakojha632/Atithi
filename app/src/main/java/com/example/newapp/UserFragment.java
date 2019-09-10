package com.example.newapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;

import org.json.JSONException;
import org.json.JSONObject;

public class UserFragment extends Fragment {
    private ImageView icon;
    private TextView username, userid;
    private View v;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.user_fragment, null);
        icon = v.findViewById(R.id.user_icon);
        username = v.findViewById(R.id.user_name);
        userid = v.findViewById(R.id.user_id);
        initUi();
        return v;
    }

    private void initUi(){
        if (AccessToken.getCurrentAccessToken() != null){
            GraphRequest request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                @Override
                public void onCompleted(JSONObject object, GraphResponse response) {
                    try {
                        Glide.with(v.getContext()).asBitmap().load("http://graph.facebook.com/" + object.getString("id") + "/picture?type=large").into(icon);
                        username.setText(object.getString("name"));
                        userid.setText(object.getString("id"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
            Bundle params = new Bundle();
            params.putString("fields", "name, email, id");
            request.setParameters(params);
            request.executeAsync();
        }
    }

}
