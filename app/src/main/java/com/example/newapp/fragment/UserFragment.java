package com.example.newapp.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.newapp.R;
import com.example.newapp.activity.LoginActivity;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.widget.LoginButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;


public class UserFragment extends Fragment {

    AccessTokenTracker tokenTracker = new AccessTokenTracker() {
        @Override
        protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
            if (currentAccessToken == null) {
                //user logged out
                logout();
            }
        }
    };
    private static boolean isSelected = false;
    private ImageView icon, fullView;
    private TextView username, userid, gender;
    private boolean fullScreen;
    private CardView iconholder, fullViewHolder;
    private View v;
    private LoginButton loginButton;
    private boolean small, full;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.user_fragment, null);
        icon = v.findViewById(R.id.user_icon);
        iconholder = v.findViewById(R.id.icon_holder);
        loginButton = v.findViewById(R.id.logout_button);
        username = v.findViewById(R.id.user_name);
        userid = v.findViewById(R.id.user_id);
        gender = v.findViewById(R.id.user_gender);
        fullView = v.findViewById(R.id.expanded_image);
        fullViewHolder = v.findViewById(R.id.fullViewHolder);
        initUi();
        icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fullViewHolder.setVisibility(View.VISIBLE);
                iconholder.setVisibility(View.INVISIBLE);
                loginButton.setVisibility(View.INVISIBLE);
                fullViewHolder.setRadius(20);
            }
        });

        fullView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fullViewHolder.setVisibility(View.INVISIBLE);
                iconholder.setVisibility(View.VISIBLE);
                loginButton.setVisibility(View.VISIBLE);
            }
        });
        isSelected = true;
        return v;
    }

    private void initUi() {
        if (AccessToken.getCurrentAccessToken() != null) {
            GraphRequest request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                @Override
                public void onCompleted(JSONObject object, GraphResponse response) {
                    try {
                        Glide.with(v.getContext()).asBitmap().load("https://graph.facebook.com/" + object.getString("id") + "/picture?type=large").into(icon);
                        Glide.with(v.getContext()).asBitmap().load("https://graph.facebook.com/" + object.getString("id") + "/picture?height=500").into(fullView);
                        username.setText(object.getString("name"));
                        userid.setText("Id: " + object.getString("id"));
                        //gender.setText(object.getString("user_gender"));
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

    private void logout() {
        try {
            File file = new File(getContext().getFilesDir(), "user");
            file.delete();
            Toast.makeText(getContext(), "Thank You for your visit! Hope to see you soon", Toast.LENGTH_LONG).show();
            Intent i = new Intent(this.getContext(), LoginActivity.class);
            startActivity(i);
            getActivity().finish();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }
}
