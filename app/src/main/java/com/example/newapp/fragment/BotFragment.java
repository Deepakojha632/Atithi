package com.example.newapp.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.newapp.R;

public class BotFragment extends Fragment {

    private TextView t;
    private View v;
    private static boolean isSelected = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.bot_fragment, null);
        initUi();
        isSelected = true;
        return v;
    }

    private void initUi() {
        t = v.findViewById(R.id.maintext);
        if (t != null) {
            t.setText("Ask Us");
        } else {
            Toast.makeText(v.getContext(), "Textview is null", Toast.LENGTH_SHORT).show();
        }
    }
}
