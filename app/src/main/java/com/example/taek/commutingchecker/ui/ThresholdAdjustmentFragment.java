package com.example.taek.commutingchecker.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.taek.commutingchecker.R;

/**
 * Created by Awesometic on 2016-08-23.
 */
public class ThresholdAdjustmentFragment extends Fragment {

    private View rootView;

    Button btnCalibrationEnd;

    public static ThresholdAdjustmentFragment newInstance() {
        ThresholdAdjustmentFragment fragment = new ThresholdAdjustmentFragment();
        return fragment;
    }

    public ThresholdAdjustmentFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_threshold_adjustment, container, false);

        btnCalibrationEnd = (Button) rootView.findViewById(R.id.btn_calibration_end);

        btnCalibrationEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Send final calibration data to server, notify user, and then go back to setup fragment

                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.content_frame, new SetupFragment());
                fragmentTransaction.commit();
            }
        });

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
}