package com.example.taek.commutingchecker;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    Button startService, stopService;
    Intent intent;
    public static String ServiceTAG;
    Button showData, request, calibration, calibrationTest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startService = (Button)findViewById(R.id.Start_Service);
        stopService = (Button)findViewById(R.id.Stop_Service);
        intent = new Intent(this, BLEScanService.class);
        ServiceTAG = getResources().getString(R.string.scan_service);
        request = (Button)findViewById(R.id.Request);
        showData = (Button)findViewById(R.id.Show_EssentialData);
        calibration = (Button)findViewById(R.id.Calibration);
        calibrationTest = (Button)findViewById(R.id.test);

        // BLE 관련 Permission 주기
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            // Android M Permission check
            if(this.checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("This app needs location access");
                builder.setMessage("Please grant location access so this app can detect beacons.");
                builder.setPositiveButton("Ok", null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @TargetApi(Build.VERSION_CODES.M)
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        requestPermissions(new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                    }
                });
                builder.show();
            }
        }

        startService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isRunning = isRunningProcess(MainActivity.this, "com.example.taek.commutingchecker:remote");
                if(isRunning){
                    Toast.makeText(MainActivity.this, "service is already running", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(MainActivity.this, "service start", Toast.LENGTH_SHORT).show();
                    startService(intent);
                }
            }
        });

        stopService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //stopService(intent);
                Intent intent = new Intent("android.intent.action.STOP_SERVICE");
                intent.setData(Uri.parse("StopSelf:"));
                sendBroadcast(intent);
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
                    Toast.makeText(MainActivity.this, BLEScanService.EssentialDataArray.get(0).get("id_workplace").toString() + ", "
                            + BLEScanService.EssentialDataArray.get(0).get("coordinateX").toString() + ", "
                            + BLEScanService.EssentialDataArray.get(0).get("coordinateY").toString() + ", "
                            + BLEScanService.EssentialDataArray.get(0).get("coordinateZ").toString() + ", "
                            + BLEScanService.EssentialDataArray.get(0).get("beacon_address1").toString() + ", "
                            + BLEScanService.EssentialDataArray.get(0).get("beacon_address2").toString() + ", "
                            + BLEScanService.EssentialDataArray.get(0).get("beacon_address3").toString() + ", "
                            + BLEScanService.EssentialDataArray.size(), Toast.LENGTH_LONG).show();
                else{
                    Toast.makeText(MainActivity.this, "no data", Toast.LENGTH_SHORT).show();
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
    }

    @Override
    protected void onResume(){
        super.onResume();
        // BLE를 지원하지 않는 디바이스 경우 강제 종료
        if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)){
            Toast.makeText(this, "BLE를 지원하지 않는 디바이스입니다.", Toast.LENGTH_SHORT).show();
            finish();
        }else{
            //Toast.makeText(this, "BLE를 지원하는 디바이스입니다.", Toast.LENGTH_SHORT).show();
        }

        // 서비스 실행
        //startService(intent);

        Log.d("connect in mainactivity", "true");
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

    @Override
    public void onRequestPermissionsResult(int reqeustCode, String permission[], int[] grantResults){
        switch (reqeustCode){
            case PERMISSION_REQUEST_COARSE_LOCATION:{
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Log.d("permission", "coarse location permission granted");
                }else{
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, " +
                            "this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton("Ok", null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {

                        }
                    });
                    builder.show();
                }
                return;
            }
        }
    }
}
