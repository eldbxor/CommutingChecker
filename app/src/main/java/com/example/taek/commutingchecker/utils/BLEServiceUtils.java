package com.example.taek.commutingchecker.utils;

import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;

import com.example.taek.commutingchecker.services.BLEScanService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Taek on 2016-07-07.
 */
public class BLEServiceUtils {
    public static int threshold_Calibration = 1;
    private static Timer timer;
    private static int timerSecond = 0;

    public static void addDeviceInfo(DeviceInfo deviceInfo){
        boolean isExisted = false;
        int index = 0;

        for(DeviceInfo mInfo : BLEScanService.mBLEDevices){
            if(mInfo.Address.equals(deviceInfo.Address)){
                isExisted = true;
                index = BLEScanService.mBLEDevices.indexOf(mInfo);
                break;
            }
        }

        if(isExisted == true){
            BLEScanService.mBLEDevices.add(index, deviceInfo);
            BLEScanService.mBLEDevices.remove(index + 1);
        }else{
            BLEScanService.mBLEDevices.add(deviceInfo);
        }
    }

    public static void addEssentialData(Map<String, String> essentialData){
        boolean isExisted = false;
        int index = 0;

        for(Map<String, String> map : BLEScanService.EssentialDataArray){
            if(essentialData.get("id_workplace").equals(map.get("id_workplace"))){
                isExisted = true;
                index = BLEScanService.EssentialDataArray.indexOf(map);
                break;
            }
        }

        if(isExisted == true){
            BLEScanService.EssentialDataArray.add(index, essentialData);
            BLEScanService.EssentialDataArray.remove(index + 1);
        }else{
            BLEScanService.EssentialDataArray.add(essentialData);
        }
    }

    public static void addFilterList(String beaconAddress){
        boolean isExisted = false;
        int index = 0;

        if(beaconAddress.equals(""))
            return;

        for(String mac : BLEScanService.filterlist){
            if(mac.equals(beaconAddress)){
                isExisted = true;
                index = BLEScanService.filterlist.indexOf(mac);
                break;
            }
        }

        if(isExisted == true){
            BLEScanService.filterlist.add(index, beaconAddress);
            BLEScanService.filterlist.remove(index + 1);
        }else{
            BLEScanService.filterlist.add(beaconAddress);
        }
    }

    public static void checkTime(){
        Runnable r = new Runnable() {
            @Override
            public void run() {
                List<DeviceInfo> mBLEDevice;
                List<Boolean> checkThreeTime = new ArrayList<Boolean>();
                DeviceInfo mDeviceInfo1 = null, mDeviceInfo2 = null, mDeviceInfo3 = null;
                int coordinateX = 0, coordinateY = 0, coordinateZ = 0, count = 0, threshold = 0;
                mBLEDevice = BLEScanService.mBLEDevices;
                BLEScanService.coolTime = true;

                if(BLEScanService.mBLEDevices.size() != 3)
                    return;

                // Rssi 제한값 검사
                for(Map<String, String> map : BLEScanService.EssentialDataArray){
                    for(int j = 0; j < 3; j++){
                        if(map.get("beacon_address1").equals(mBLEDevice.get(j).Address))
                            mDeviceInfo1 = mBLEDevice.get(j);
                        else if(map.get("beacon_address2").equals(mBLEDevice.get(j).Address))
                            mDeviceInfo2 = mBLEDevice.get(j);
                        else if(map.get("beacon_address3").equals(mBLEDevice.get(j).Address)) {
                            mDeviceInfo3 = mBLEDevice.get(j);
                            coordinateX = Integer.valueOf(map.get("coordinateX"));
                            coordinateY = Integer.valueOf(map.get("coordinateY"));
                            coordinateZ = Integer.valueOf(map.get("coordinateZ"));
                            threshold = Integer.valueOf(map.get("thresholdX"));
                        }
                    }
                }

                while(true){
                    if(BLEScanService.CompleteCalibraton) {
                        /* 서버에 전송하는 내용
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
                        BLEScanService.temporaryCalibrationData.put("ThresholdX", String.valueOf(threshold_Calibration));
                        BLEScanService.temporaryCalibrationData.put("ThresholdY", String.valueOf(threshold_Calibration));
                        BLEScanService.temporaryCalibrationData.put("ThresholdZ", String.valueOf(threshold_Calibration));
                         */
                        BLEScanService.temporaryCalibrationData.put("ThresholdX", String.valueOf(threshold_Calibration));
                        BLEScanService.temporaryCalibrationData.put("ThresholdY", String.valueOf(threshold_Calibration));
                        BLEScanService.temporaryCalibrationData.put("ThresholdZ", String.valueOf(threshold_Calibration));
                        BLEScanService.mSocketIO.calibration(BLEScanService.temporaryCalibrationData);

                        return;
                    }

                    mBLEDevice = BLEScanService.mBLEDevices;
                    for(DeviceInfo deviceInfo : mBLEDevice){
                        if(deviceInfo.Address.equals(mDeviceInfo1.Address))
                            mDeviceInfo1 = deviceInfo;
                        else if(deviceInfo.Address.equals(mDeviceInfo2.Address))
                            mDeviceInfo2 = deviceInfo;
                        else if(deviceInfo.Address.equals(mDeviceInfo3.Address))
                            mDeviceInfo3 = deviceInfo;
                    }
                    int count_for = 0;

                    if(BLEScanService.CalibrationFlag){ // Calibration 중일 때
                        if (mDeviceInfo1.Rssi > (coordinateX - threshold_Calibration) && mDeviceInfo1.Rssi < (coordinateX + threshold_Calibration)) {
                            count_for++;
                        }
                        if (mDeviceInfo2.Rssi > (coordinateY - threshold_Calibration) && mDeviceInfo2.Rssi < (coordinateY + threshold_Calibration)) {
                            count_for++;
                        }
                        if (mDeviceInfo3.Rssi > (coordinateZ - threshold_Calibration) && mDeviceInfo3.Rssi < (coordinateZ + threshold_Calibration)) {
                            count_for++;
                        }
                    }else {
                        if (mDeviceInfo1.Rssi > (coordinateX - threshold) && mDeviceInfo1.Rssi < (coordinateX + threshold)) {
                            count_for++;
                        }
                        if (mDeviceInfo2.Rssi > (coordinateY - threshold) && mDeviceInfo2.Rssi < (coordinateY + threshold)) {
                            count_for++;
                        }
                        if (mDeviceInfo3.Rssi > (coordinateZ - threshold) && mDeviceInfo3.Rssi < (coordinateZ + threshold)) {
                            count_for++;
                        }
                    }

                    if(count_for >= 2){
                        if(checkThreeTime.size() < 3)
                            checkThreeTime.add(true);
                        else if(checkThreeTime.size() == 3){
                            checkThreeTime.remove(0);
                            checkThreeTime.add(true);
                        }
                    }else{
                        if(checkThreeTime.size() < 3)
                            checkThreeTime.add(false);
                        else if(checkThreeTime.size() == 3){
                            checkThreeTime.remove(0);
                            checkThreeTime.add(false);
                        }
                    }

                    int times = 0;
                    for(Boolean bool : checkThreeTime){
                        if(bool)
                            times++;
                    }

                    if(BLEScanService.CalibrationFlag){ // Calibration 중일 때
                        if(times >= 2){
                            try {
                                BLEScanService.replyToActivityMessenger.send(Message.obtain(null, Constants.HANDLE_MESSAGE_TYPE_SETTEXT_ATTENDANCE_ZONE));
                                Log.d("MessengerCommunication", "Service send 3");
                            }catch(RemoteException e){
                                Log.d("replyToActivity", e.toString());
                            }
                        }else{
                            try {
                                BLEScanService.replyToActivityMessenger.send(Message.obtain(null, Constants.HANDLE_MESSAGE_TYPE_SETTEXT_NOT_ATTENDANCE_ZONE));
                                Log.d("MessengerCommunication", "Service send 4");
                            }catch(RemoteException e){
                                Log.d("replyToActivity", e.toString());
                            }
                        }
                    }else {
                        if (times >= 2) {
                            // BLEServiceUtils.sendEvent(mDeviceInfo1, mDeviceInfo2, mDeviceInfo3, true);
                            GenerateNotification.generateNotification(BLEScanService.ServiceContext, "출근대기 중", "", "");
                            timerStart(mDeviceInfo1, mDeviceInfo2, mDeviceInfo3);
                            break;
                        }
                    }

                    try {
                        Thread.sleep(300);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        Thread thread = new Thread(r);
        thread.start();
    }

    private static void timerStart(final DeviceInfo deviceInfo1, final DeviceInfo deviceInfo2, final DeviceInfo deviceInfo3) {
        timerSecond = 0;

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                timerTextUpdate(deviceInfo1, deviceInfo2, deviceInfo3);
                timerSecond++;
            }
        }, 0, 1000);
    }

    private static void timerStop() {
        timer.cancel();
        timer = null;
    }

    private static void timerTextUpdate(final DeviceInfo deviceInfo1, final DeviceInfo deviceInfo2, final DeviceInfo deviceInfo3) {
        Runnable updater = new Runnable() {
            @Override
            public void run() {
                //timerText.setText(timerSecond + " 초");

                if (timerSecond == 30) {
                    timerStop();

                    BLEScanService.checkCallbackThread_standByAttendance = new CheckCallback(deviceInfo1, deviceInfo2, deviceInfo3, true);
                    //btnCalibrationStart.setText("NEXT");
                }
            }
        };
        BLEScanService.timerHandler.post(updater);
    }

    public synchronized static void sendEvent(DeviceInfo deviceInfo1, DeviceInfo deviceInfo2, DeviceInfo deviceInfo3, final boolean isComeToWork){
        //if((!BLEScanService.coolTime && comeToWork) || (BLEScanService.coolTime && !comeToWork)) {
        if((isComeToWork || (BLEScanService.coolTime && !isComeToWork))){
            Map<String, String> data = new HashMap<String, String>();
            data.put("BeaconDeviceAddress1", deviceInfo1.Address);
            data.put("BeaconDeviceAddress2", deviceInfo2.Address);
            data.put("BeaconDeviceAddress3", deviceInfo3.Address);
            data.put("BeaconData1", deviceInfo1.ScanRecord);
            data.put("BeaconData2", deviceInfo2.ScanRecord);
            data.put("BeaconData3", deviceInfo3.ScanRecord);
            data.put("SmartphoneAddress", BLEScanService.myMacAddress);
            //data.put("DateTime", CurrentTime.currentTime());

            BLEScanService.mSocketIO.sendEvent(data, isComeToWork);
            if (isComeToWork) {
                BLEScanService.coolTime = true;
                BLEScanService.checkCallbackThread = new CheckCallback(deviceInfo1, deviceInfo2, deviceInfo3, false);
            } else {
                BLEScanService.coolTime = false;
                BLEScanService.mBLEDevices.clear();
            }
        }
    }
}
