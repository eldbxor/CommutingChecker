package com.example.taek.commutingchecker.ui;

import android.app.ActivityManager;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.example.taek.commutingchecker.R;
import com.example.taek.commutingchecker.services.BLEScanService;

import java.util.List;

/**
 * Created by Awesometic on 2016-06-09.
 * Migrating views from MainActivity to this SetupFragment
 */
public class SetupFragment extends Fragment {

    Intent intent;
    public static Button showData, request, calibration, checkRunning;
    public static Switch bleScanSwitch;

    public static SetupFragment newInstance() {
        SetupFragment fragment = new SetupFragment();
        return fragment;
    }

    public SetupFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_setup, container, false);

        bleScanSwitch = (Switch) rootView.findViewById(R.id.BLEScanSwitch);
        intent = new Intent(getActivity(), BLEScanService.class);
        request = (Button) rootView.findViewById(R.id.Request);
        showData = (Button) rootView.findViewById(R.id.Show_EssentialData);
        calibration = (Button) rootView.findViewById(R.id.Calibration);
        checkRunning = (Button) rootView.findViewById(R.id.CheckRunning);

        // if service is running switch off
        if(isRunningProcess(getActivity(), "com.example.taek.commutingchecker:remote"))
            bleScanSwitch.setChecked(true);

        bleScanSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){ // Switch on
                    boolean isRunning = isRunningProcess(getActivity(), "com.example.taek.commutingchecker:remote");
                    if(isRunning){
                        Toast.makeText(getActivity(), "service is already running", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(getActivity(), "service start", Toast.LENGTH_SHORT).show();
                        getActivity().startService(intent);
                    }
                }else{ // Switch off
                    //stopService(intent);
                    Intent intent = new Intent("android.intent.action.STOP_SERVICE");
                    intent.setData(Uri.parse("StopSelf:"));
                    getActivity().sendBroadcast(intent);
                }
            }
        });

        request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent("android.intent.action.REQUEST_DATA");
                intent.setData(Uri.parse("RequestData:"));
                getActivity().sendBroadcast(intent);
            }
        });

        showData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent("android.intent.action.SHOW_DATA");
                intent.setData(Uri.parse("ShowData:"));
                getActivity().sendBroadcast(intent);
                //GenerateNotification.generateNotification(getActivity(), "showData", "data", "");
            }
        });

        calibration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent("android.intent.action.CALIBRATION_SERVICE");
                intent.setData(Uri.parse("Calibration:"));
                getActivity().sendBroadcast(intent);
            }
        });

        checkRunning.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // if service is running switch off
                if(isRunningProcess(getActivity(), "com.example.taek.commutingchecker:remote"))
                    Toast.makeText(getActivity(), "True", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getActivity(), "False", Toast.LENGTH_SHORT).show();
            }
        });

        return rootView;
    }

    // 서비스 프로세스 실행 상태 확인
    private boolean isRunningProcess(Context context, String packageName){
        boolean isRunning = false;

        ActivityManager actMng = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        List <ActivityManager.RunningServiceInfo> list = actMng.getRunningServices(Integer.MAX_VALUE);
        for(ActivityManager.RunningServiceInfo rap : list){
            if(rap.process.equals(packageName)){
                isRunning = true;
                break;
            }
        }

        return isRunning;
    }

}
