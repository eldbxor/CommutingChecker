package com.example.taek.commutingchecker.utils;

import android.os.Message;
import android.os.RemoteException;
import android.util.Log;

import com.example.taek.commutingchecker.services.BLEScanService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Taek on 2016-06-02.
 */
public class Calibration {
    private static ArrayList<Integer> rssi1, rssi2, rssi3;
    private static DeviceInfo mDeviceInfo1, mDeviceInfo2, mDeviceInfo3;
    private static List<DeviceInfo> mBLEDevices_Calibration;
    private static int sumOfRssi1, sumOfRssi2, sumOfRssi3;
    private static int count_error = 0;

    public static void calibration(){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {

                while(true) {
                    try{
                        Thread.sleep(500);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    if (BLEScanService.mBLEDevices.size() < 3) {
                        if (count_error == 10) {
                            GenerateNotification.generateNotification(BLEScanService.ServiceContext, "Calibration Error", "비콘 데이터가 없습니다 잠시후 재시도 해주십시오.", "");
                            BLEScanService.CalibrationFlag = false;
                            return;
                        }else{
                            count_error++;
                            continue;
                        }
                    }else{
                        break;
                    }
                }
                rssi1 = new ArrayList<Integer>();
                rssi2 = new ArrayList<Integer>();
                rssi3 = new ArrayList<Integer>();

                sumOfRssi1 = 0;
                sumOfRssi2 = 0;
                sumOfRssi3 = 0;
                rssi1.clear(); rssi2.clear(); rssi3.clear();
                mBLEDevices_Calibration = BLEScanService.mBLEDevices;

                mDeviceInfo1 = mBLEDevices_Calibration.get(0);
                mDeviceInfo2 = mBLEDevices_Calibration.get(1);
                mDeviceInfo3 = mBLEDevices_Calibration.get(2);

                // GenerateNotification.generateNotification(BLEScanService.ServiceContext, "Calibration", "Calibration start" ,"");

                // 30 times for 30 seconds
                for(int i = 0; i < 60; i++){
                    try{
                        Thread.sleep(500);
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                    if(BLEScanService.calibrationResetFlag == true)
                        return;
                    // Log.d("calibrationResetFlag", String.valueOf(BLEScanService.calibrationResetFlag));

                    mBLEDevices_Calibration = null;
                    mBLEDevices_Calibration = BLEScanService.mBLEDevices;
                    for(DeviceInfo deviceInfo : mBLEDevices_Calibration){
                        if(deviceInfo.Address.equals(mDeviceInfo1.Address)) {
                            rssi1.add(deviceInfo.Rssi);
                            sumOfRssi1 += deviceInfo.Rssi;
                        }
                        else if(deviceInfo.Address.equals(mDeviceInfo2.Address)) {
                            rssi2.add(deviceInfo.Rssi);
                            sumOfRssi2 += deviceInfo.Rssi;
                        }
                        else if(deviceInfo.Address.equals(mDeviceInfo3.Address)) {
                            rssi3.add(deviceInfo.Rssi);
                            sumOfRssi3 += deviceInfo.Rssi;
                        }
                    }

                    try {
                        Log.d("MessengerCommunication", "Service send 2");
                        BLEScanService.replyToActivityMessenger.send(Message.obtain(null, Constants.HANDLE_MESSAGE_TYPE_ADD_TIMESECOND));
                    }catch(RemoteException e){
                        Log.d("replyToActivity", e.toString());
                    }
                }

                sumOfRssi1 = sumOfRssi1 / rssi1.size();
                sumOfRssi2 = sumOfRssi2 / rssi2.size();
                sumOfRssi3 = sumOfRssi3 / rssi3.size();

                Log.d("Rssi 값 배열", rssi1.toString() + ", " + rssi2.toString() + ", " + rssi3.toString());
                Log.d("Rssi 값 평균", String.valueOf(sumOfRssi1) + ", " + String.valueOf(sumOfRssi2) + ", " + String.valueOf(sumOfRssi3));

                /*
                while(count_error < 30){
                    try{
                        Thread.sleep(1000);
                        count_error++;
                        BLEScanService.replyToActivityMessenger.send(Message.obtain(null, 2));
                    }catch (Exception e){
                        Log.d("test", e.toString());
                    }
                } */


                try {
                    BLEScanService.replyToActivityMessenger.send(Message.obtain(null, Constants.HANDLE_MESSAGE_TYPE_SETTEXT_NEXT));
                    Log.d("MessengerCommunication", "Service send 1");
                }catch(RemoteException e){
                    Log.d("replyToActivity", e.toString());
                }


                Map<String, String> data = new HashMap<String, String>();
                data.put("BeaconDeviceAddress1", mDeviceInfo1.Address);
                data.put("BeaconDeviceAddress2", mDeviceInfo2.Address);
                data.put("BeaconDeviceAddress3", mDeviceInfo3.Address);
                data.put("BeaconData1", mDeviceInfo1.ScanRecord);
                data.put("BeaconData2", mDeviceInfo2.ScanRecord);
                data.put("BeaconData3", mDeviceInfo3.ScanRecord);
                data.put("SmartphoneAddress", BLEScanService.myMacAddress);
                //data.put("DateTime", CurrentTime.currentTime());
                data.put("CoordinateX", String.valueOf(sumOfRssi1));
                data.put("CoordinateY", String.valueOf(sumOfRssi2));
                data.put("CoordinateZ", String.valueOf(sumOfRssi3));
                BLEScanService.temporaryCalibrationData = data;
              // BLEScanService.mSocketIO.calibration(data);
            }
        });

        thread.start();
    }
}
