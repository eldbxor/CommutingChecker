package com.example.taek.commutingchecker;

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
import android.widget.Toast;

import java.util.List;

/**
 * Created by Awesometic on 2016-06-09.
 * Migrating views from MainActivity to this SetupFragment
 */
public class SetupFragment extends Fragment {

    Button startService, stopService;
    Intent intent;
    Button showData, request, calibration, calibrationTest;

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

        startService = (Button) rootView.findViewById(R.id.Start_Service);
        stopService = (Button) rootView.findViewById(R.id.Stop_Service);
        intent = new Intent(getActivity(), BLEScanService.class);
        request = (Button) rootView.findViewById(R.id.Request);
        showData = (Button) rootView.findViewById(R.id.Show_EssentialData);
        calibration = (Button) rootView.findViewById(R.id.Calibration);
        calibrationTest = (Button) rootView.findViewById(R.id.test);

        startService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isRunning = isRunningProcess(getActivity(), "com.example.taek.commutingchecker:remote");
                if(isRunning){
                    Toast.makeText(getActivity(), "service is already running", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getActivity(), "service start", Toast.LENGTH_SHORT).show();
                    getActivity().startService(intent);
                }
            }
        });

        stopService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //stopService(intent);
                Intent intent = new Intent("android.intent.action.STOP_SERVICE");
                intent.setData(Uri.parse("StopSelf:"));
                getActivity().sendBroadcast(intent);
            }
        });

        request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BLEScanService.mSocketIO.requestEssentialData();
            }
        });

        showData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(BLEScanService.EssentialDataArray.size() > 0)
                    Toast.makeText(getActivity(), BLEScanService.EssentialDataArray.get(0).get("id_workplace").toString() + ", "
                            + BLEScanService.EssentialDataArray.get(0).get("coordinateX").toString() + ", "
                            + BLEScanService.EssentialDataArray.get(0).get("coordinateY").toString() + ", "
                            + BLEScanService.EssentialDataArray.get(0).get("coordinateZ").toString() + ", "
                            + BLEScanService.EssentialDataArray.get(0).get("beacon_address1").toString() + ", "
                            + BLEScanService.EssentialDataArray.get(0).get("beacon_address2").toString() + ", "
                            + BLEScanService.EssentialDataArray.get(0).get("beacon_address3").toString() + ", "
                            + BLEScanService.EssentialDataArray.size(), Toast.LENGTH_LONG).show();
                else{
                    Toast.makeText(getActivity(), "no data", Toast.LENGTH_SHORT).show();
                }
            }
        });

        calibration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BLEScanService.CalibrationFlag = true;
            }
        });

        calibrationTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BLEScanService.test();
            }
        });

        return rootView;
    }

    // 서비스 프로세스 실행 상태 확인
    private boolean isRunningProcess(Context context, String packageName){
        boolean isRunning = false;
        ActivityManager actMng = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        //List<ActivityManager.RunningAppProcessInfo> list = actMng.getRunningAppProcesses();
        List<ActivityManager.RunningServiceInfo> list = actMng.getRunningServices(Integer.MAX_VALUE);
        for(ActivityManager.RunningServiceInfo rap : list){
            if(rap.service.getPackageName().equals(packageName)){
                isRunning = true;
                break;
            }
        }

        return isRunning;
    }

}
