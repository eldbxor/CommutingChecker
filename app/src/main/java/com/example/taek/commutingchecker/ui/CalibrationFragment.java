package com.example.taek.commutingchecker.ui;

import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.taek.commutingchecker.R;

/**
 * Created by Awesometic on 2016-08-22.
 */
public class CalibrationFragment extends Fragment {
    
    private View rootView;

    public static CalibrationFragment newInstance() {
        CalibrationFragment fragment = new CalibrationFragment();
        return fragment;
    }

    public CalibrationFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_calibration, container, false);

        Button calibration = (Button) rootView.findViewById(R.id.btn_calibration_start);
        calibration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent("android.intent.action.CALIBRATION_SERVICE");
                intent.setData(Uri.parse("Calibration:"));
                getActivity().sendBroadcast(intent);
            }
        });

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
}
