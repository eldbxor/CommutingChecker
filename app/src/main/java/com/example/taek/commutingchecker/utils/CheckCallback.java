package com.example.taek.commutingchecker.utils;

import android.util.Log;

import com.example.taek.commutingchecker.services.BLEScanService;

/**
 * Created by Taek on 2016-06-08.
 */
public class CheckCallback {
    private Thread thread;
    boolean flag;
    boolean standBy;
    int count;
    public CheckCallback(final DeviceInfo deviceInfo1, final DeviceInfo deviceInfo2, final DeviceInfo deviceInfo3, final boolean standByAttendance){
        flag = true;
        standBy = standByAttendance;
        // count = 0;

        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(flag){
                    Log.d("CheckCallback", "Running");
                    BLEScanService.isCallbackRunning = false;

                    try{
                        Thread.sleep(3000);
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                    if(!BLEScanService.isCallbackRunning){ // callback method isn't running
                        Log.d("isCallbackRunning)", "false");
                        if(standBy){ // 출근 대기 중 - 출근 실패
                            BLEScanService.standByFlag = false;
                        }else {
                            BLEServiceUtils.sendEvent(deviceInfo1, deviceInfo2, deviceInfo3, false);
                            /*
                            count++;
                            if(count >= 10) {
                                BLEServiceUtils.sendEvent(deviceInfo1, deviceInfo2, deviceInfo3, false);
                            } */
                        }
                        //flag = false;
                        break;
                    }else{
                        if(standBy){ // 출근 대기 중 - 출근 범위 내에 있음
                            // BLEScanService.standByFlag = true;
                            /*
                            BLEServiceUtils.sendEvent(deviceInfo1, deviceInfo2, deviceInfo3, true);
                            standBy = false;
                            continue; */
                        }
                    }
                }
            }
        });

        thread.start();
    }
}
