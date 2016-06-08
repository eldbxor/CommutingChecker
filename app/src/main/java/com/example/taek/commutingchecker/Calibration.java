package com.example.taek.commutingchecker;

import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Taek on 2016-06-02.
 */
public class Calibration {
    private static ArrayList<Integer> rssi1, rssi2, rssi3;
    private static DeviceInfo mDeviceInfo1, mDeviceInfo2, mDeviceInfo3;
    private static int sumOfRssi1, sumOfRssi2, sumOfRssi3;

    public static void calibration(){
        if(BLEScanService.mBLEDevices.size() < 3){
            GenerateNotification.generateNotification(BLEScanService.ServiceContext, "Calibration Error", "비콘 데이터가 없습니다 잠시후 재시도 해주십시오.", "");
            BLEScanService.CalibrationFlag = false;
            return;
        }

        sumOfRssi1 = 0;
        sumOfRssi2 = 0;
        sumOfRssi3 = 0;

        rssi1 = new ArrayList<Integer>();
        rssi2 = new ArrayList<Integer>();
        rssi3 = new ArrayList<Integer>();
        mDeviceInfo1 = BLEScanService.mBLEDevices.get(0);
        mDeviceInfo2 = BLEScanService.mBLEDevices.get(1);
        mDeviceInfo3 = BLEScanService.mBLEDevices.get(2);

        GenerateNotification.generateNotification(BLEScanService.ServiceContext, "Calibration", "Calibration start" ,"");
        // 30 times for 30 seconds
        for(int i = 0; i < 60; i++){
            try{
                Thread.sleep(500);
            }catch (Exception e){
                e.printStackTrace();
            }

            for(DeviceInfo deviceInfo : BLEScanService.mBLEDevices){
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
        }

        sumOfRssi1 = sumOfRssi1 / rssi1.size();
        sumOfRssi2 = sumOfRssi2 / rssi2.size();
        sumOfRssi3 = sumOfRssi3 / rssi3.size();

        Log.d("Rssi 값 배열", rssi1.toString() + ", " + rssi2.toString() + ", " + rssi3.toString());
        Log.d("Rssi 값 평균", String.valueOf(sumOfRssi1) + ", " + String.valueOf(sumOfRssi2) + ", " + String.valueOf(sumOfRssi3));

        /*
        GenerateNotification.generateNotification(BLEScanService.ServiceContext, "Rssi 값", "Rssi 평균값이 계산되었습니다.",
                "rssi1: " + String.valueOf(sumOfRssi1) + ", " + "rssi2: " + String.valueOf(sumOfRssi2) + ", " + "rssi3: " + String.valueOf(sumOfRssi3)); */

        Map<String, String> data = new HashMap<String, String>();
        data.put("BeaconDeviceAddress1", mDeviceInfo1.Address);
        data.put("BeaconDeviceAddress2", mDeviceInfo2.Address);
        data.put("BeaconDeviceAddress3", mDeviceInfo3.Address);
        data.put("BeaconData1", mDeviceInfo1.ScanRecord);
        data.put("BeaconData2", mDeviceInfo2.ScanRecord);
        data.put("BeaconData3", mDeviceInfo3.ScanRecord);
        data.put("SmartphoneAddress", BLEScanService.myMacAddress);
        data.put("DateTime", CurrentTime.currentTime());
        data.put("CoordinateX", String.valueOf(sumOfRssi1));
        data.put("CoordinateY", String.valueOf(sumOfRssi2));
        data.put("CoordinateZ", String.valueOf(sumOfRssi3));
        BLEScanService.mSocketIO.calibration(data);
    }
}
