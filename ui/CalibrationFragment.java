package com.example.taek.commutingchecker.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.taek.commutingchecker.R;
import com.example.taek.commutingchecker.services.BLEScanService;
import com.example.taek.commutingchecker.utils.Constants;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Awesometic on 2016-08-22.
 */
public class CalibrationFragment extends Fragment {

    private View rootView;

    public static Button btnCalibrationStart;

    private Timer timer;
    private TextView timerText;
    public static int timerSecond = 0;
    private final Handler timerHandler = new Handler();
    public static ProgressBar progressBar;

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

        progressBar = (ProgressBar) rootView.findViewById(R.id.calibrationProgressBar);

        btnCalibrationStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (btnCalibrationStart.getText().toString()) {

                    case "START":
                        // Calibration start code here

                        MainActivity.connectMessenger();

                        //timerStart();
                        // btnCalibrationStart.setText("RESET");

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

                        try {
                            MainActivity.messenger.send(Message.obtain(null, Constants.HANDLE_MESSAGE_TYPE_CEHCK_THRESHOLD));
                            Log.d("MessengerCommunication", "Activity send 3");
                        }catch (RemoteException e){
                            Log.d("Calibration Reset", e.toString());
                        }
                        timerSecond = 0;
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

    public static void timerStart() {
        try{
            Message msg = Message.obtain(null, Constants.HANDLE_MESSAGE_TYPE_CALIBRATION);
            msg.replyTo = MainActivity.incomingMessenger;
            MainActivity.messenger.send(msg);
            Log.d("MessengerCommunication", "Activity send 1");
        }catch (RemoteException e){
            Log.d("ServiceConnection", e.toString());
        }
    }

    private void timerStop() {
        timerSecond = 0;
        progressBar.setProgress(timerSecond);
        try {
            MainActivity.messenger.send(Message.obtain(null, Constants.HANDLE_MESSAGE_TYPE_CALIBRATION_RESET));
            Log.d("MessengerCommunication", "Activity send 2");
        }catch (RemoteException e){
            Log.d("Calibration Reset", e.toString());
        }
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
