package com.example.taek.commutingchecker.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.taek.commutingchecker.R;
import com.example.taek.commutingchecker.utils.Constants;

/**
 * Created by Awesometic on 2016-08-23.
 */
public class ThresholdAdjustmentFragment extends Fragment {

    private View rootView;

    Button btnCalibrationEnd;
    TextView tvThreshold;
    public static TextView tvCheckThreshold;
    SeekBar sbThreshold;

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
        tvThreshold = (TextView) rootView.findViewById(R.id.threshold);
        sbThreshold = (SeekBar) rootView.findViewById(R.id.seekBar);
        tvCheckThreshold = (TextView) rootView.findViewById(R.id.tvCheckThreshold);

        btnCalibrationEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Send final calibration data to server, notify user, and then go back to setup fragment

                try{
                    MainActivity.messenger.send(Message.obtain(null, Constants.HANDLE_MESSAGE_TYPE_COMPLETE_CALIBRATION));
                    Log.d("MessengerCommunication", "Activity send 4");
                }catch (RemoteException e){
                    Log.d("ServiceConnection", e.toString());
                }

                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.content_frame, new SetupFragment());
                fragmentTransaction.commit();
            }
        });

        sbThreshold.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvThreshold.setText(String.valueOf(progress + 1));

                try{
                    MainActivity.messenger.send(Message.obtain(null, Constants.HANDLE_MESSAGE_TYPE_SEEKBAR_VALUE_CHANGED, progress + 6, 0));
                    Log.d("MessengerCommunication", "Activity send 5");
                }catch (RemoteException e){
                    Log.d("ServiceConnection", e.toString());
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
}