package com.example.taek.commutingchecker.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.taek.commutingchecker.R;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Awesometic on 2016-08-22.
 */
public class CalibrationFragment extends Fragment {

    private View rootView;

    Button btnCalibrationStart;

    private Timer timer;
    private TextView timerText;
    private int timerSecond = 0;
    private final Handler timerHandler = new Handler();

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

        btnCalibrationStart = (Button) rootView.findViewById(R.id.btn_calibration_start);

        btnCalibrationStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (btnCalibrationStart.getText().toString()) {

                    case "START":
                        // Calibration start code here

                        timerStart();
                        btnCalibrationStart.setText("RESET");

                        break;

                    case "RESET":
                        // Calibration cancel code here

                        timerStop();
                        btnCalibrationStart.setText("START");

                        break;

                    case "NEXT":
                        ThresholdAdjustmentFragment thresholdAdjustmentFragment = new ThresholdAdjustmentFragment();
                        FragmentManager fragmentManager = getFragmentManager();
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.content_frame, thresholdAdjustmentFragment);
                        fragmentTransaction.commit();

                        break;

                    default:
                        break;
                }
            }
        });

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private void timerStart() {
        timerText = (TextView) rootView.findViewById(R.id.timer_text);
        timerSecond = 0;

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                timerTextUpdate();
                timerSecond++;
            }
        }, 0, 1000);
    }

    private void timerStop() {
        timer.cancel();
        timer = null;
    }

    private void timerTextUpdate() {
        Runnable updater = new Runnable() {
            @Override
            public void run() {
                timerText.setText(timerSecond + " 초");

                if (timerSecond == 30) {
                    timerStop();

                    btnCalibrationStart.setText("NEXT");
                }
            }
        };
        timerHandler.post(updater);
    }
}
