package com.example.taek.commutingchecker.utils;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;

import com.example.taek.commutingchecker.services.BLEScanService;
import com.example.taek.commutingchecker.services.CalibrationService;

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
    public static int threshold_Calibration = 6;
    private static Timer timer;
    private static int timerSecond = 0;
    private static int leaveWorkCount = 0;
    private static Runnable updater;
    public BluetoothManager mBluetoothManager;
    public BluetoothAdapter mBluetoothAdapter;
    public BluetoothLeScanner mBLEScanner; // BLE 스캐너(api 21 이상)
    private Context mContext;
    private String TAG = "BLEServiceUtils";

    // 생성자
    public BLEServiceUtils(Context context) {
        mContext = context;
        Log.d(TAG, "BLEServiceUtils(): 생성자");
    }

    public void createBluetoothAdapter(Object obj){
        mBluetoothManager = (BluetoothManager)obj;
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        Log.d(TAG, "createBluetoothAdapter():  BluetoothAdapter 객체 생성");
    }

    public void enableBluetooth(){
        if(!mBluetoothAdapter.isEnabled()){
            mBluetoothAdapter.enable();
            Log.d(TAG, "enableBluetooth():  BluetoothAdapter 실행");
        }
    }

    public void addDeviceInfo(int callbackType, DeviceInfo deviceInfo) {
        boolean isExisted = false;
        int index = 0;

        switch (callbackType) {
            case Constants.CALLBACK_TYPE_BLE_SCAN_SERVICE:
                for(DeviceInfo mInfo : ((BLEScanService) mContext).mBLEDevices){
                    if(mInfo.Address.equals(deviceInfo.Address)){
                        isExisted = true;
                        index = ((BLEScanService) mContext).mBLEDevices.indexOf(mInfo);
                        break;
                    }
                }

                if(isExisted == true){
                    ((BLEScanService) mContext).mBLEDevices.add(index, deviceInfo);
                    ((BLEScanService) mContext).mBLEDevices.remove(index + 1);
                }else{
                    ((BLEScanService) mContext).mBLEDevices.add(deviceInfo);
                }
                break;

            case Constants.CALLBACK_TYPE_CALIBRATION_SERVICE:
                for(DeviceInfo mInfo : ((CalibrationService) mContext).mBLEDevices){
                    if(mInfo.Address.equals(deviceInfo.Address)){
                        isExisted = true;
                        index = ((CalibrationService) mContext).mBLEDevices.indexOf(mInfo);
                        break;
                    }
                }

                if(isExisted == true){
                    ((CalibrationService) mContext).mBLEDevices.add(index, deviceInfo);
                    ((CalibrationService) mContext).mBLEDevices.remove(index + 1);
                }else{
                    ((CalibrationService) mContext).mBLEDevices.add(deviceInfo);
                }
                break;
        }
    }

    public void addEssentialData(int callbackType, Map<String, String> essentialData) {
        boolean isExisted = false;
        int index = 0;

        switch (callbackType) {
            case Constants.CALLBACK_TYPE_BLE_SCAN_SERVICE:
                for (Map<String, String> map : ((BLEScanService) mContext).EssentialDataArray) {
                    if (essentialData.get("id_workplace").equals(map.get("id_workplace"))) {
                        isExisted = true;
                        index = ((BLEScanService) mContext).EssentialDataArray.indexOf(map);
                        break;
                    }
                }

                if (isExisted == true) {
                    ((BLEScanService) mContext).EssentialDataArray.add(index, essentialData);
                    ((BLEScanService) mContext).EssentialDataArray.remove(index + 1);
                } else {
                    ((BLEScanService) mContext).EssentialDataArray.add(essentialData);
                }
                break;

            case Constants.CALLBACK_TYPE_CALIBRATION_SERVICE:
                for (Map<String, String> map : ((CalibrationService) mContext).EssentialDataArray) {
                    if (essentialData.get("id_workplace").equals(map.get("id_workplace"))) {
                        isExisted = true;
                        index = ((CalibrationService) mContext).EssentialDataArray.indexOf(map);
                        break;
                    }
                }

                if (isExisted == true) {
                    ((CalibrationService) mContext).EssentialDataArray.add(index, essentialData);
                    ((CalibrationService) mContext).EssentialDataArray.remove(index + 1);
                } else {
                    ((CalibrationService) mContext).EssentialDataArray.add(essentialData);
                }
                break;
        }
    }

    public void addFilterList(int callbackType, String beaconAddress){
        boolean isExisted = false;
        int index = 0;

        if(beaconAddress.equals(""))
            return;

        switch (callbackType) {
            case Constants.CALLBACK_TYPE_BLE_SCAN_SERVICE:
                for(String mac : ((BLEScanService) mContext).filterlist){
                    if(mac.equals(beaconAddress)){
                        isExisted = true;
                        index = ((BLEScanService) mContext).filterlist.indexOf(mac);
                        break;
                    }
                }

                if(isExisted == true){
                    ((BLEScanService) mContext).filterlist.add(index, beaconAddress);
                    ((BLEScanService) mContext).filterlist.remove(index + 1);
                }else{
                    ((BLEScanService) mContext).filterlist.add(beaconAddress);
                }
                break;

            case Constants.CALLBACK_TYPE_CALIBRATION_SERVICE:
                for(String mac : ((CalibrationService) mContext).filterlist){
                    if(mac.equals(beaconAddress)){
                        isExisted = true;
                        index = ((CalibrationService) mContext).filterlist.indexOf(mac);
                        break;
                    }
                }

                if(isExisted == true){
                    ((CalibrationService) mContext).filterlist.add(index, beaconAddress);
                    ((CalibrationService) mContext).filterlist.remove(index + 1);
                }else{
                    ((CalibrationService) mContext).filterlist.add(beaconAddress);
                }
                break;
        }

    }

    // uuid, major, minor 나누는 메서드
    public List<String> separate(byte[] scanRecord) {
        List<String> result = new ArrayList<String>();
        String all = "";
        String uuid = "";
        int major_int;
        int minor_int;
        for (int i = 0; i <= 28; i++) {
            byte b = scanRecord[i];
            if (i > 8 && i < 28) {
                all += String.format("%02x ", b);
            } else if(i == 28) {
                all += String.format("%02x", b);
            }
            if (i > 8 && i <= 24) {
                if (i == 24) {
                    uuid += String.format("%02x", b);
                } else {
                    uuid += String.format("%02x ", b);
                }
            }
        }

        major_int = (scanRecord[25] & 0xff) * 0x100 + (scanRecord[26] & 0xff);
        minor_int = (scanRecord[27] & 0xff) * 0x100 + (scanRecord[28] & 0xff);

        result.add(all);
        result.add(uuid);
        result.add(String.valueOf(major_int));
        result.add(String.valueOf(minor_int));

        return result;
    }

    public ScanSettings setPeriod(int scanMode){
        ScanSettings settings = null;

        if(Build.VERSION.SDK_INT >= 21){
            if(Build.VERSION.SDK_INT == Build.VERSION_CODES.M){
                settings = new ScanSettings.Builder()
                        .setScanMode(scanMode)
                        .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                        .build();
            }else if(Build.VERSION.SDK_INT >= 21 && Build.VERSION.SDK_INT < 23) {
                settings = new ScanSettings.Builder()
                        .setScanMode(scanMode)
                        .build();
            }
        }

        return settings;
    }

    public void comeToWorkCheckTime(final int callbackType) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                List<DeviceInfo> mBLEDevices;
                List<Boolean> checkThreeTime = new ArrayList<Boolean>();
                DeviceInfo mDeviceInfo1 = null, mDeviceInfo2 = null, mDeviceInfo3 = null;
                int coordinateX = 0, coordinateY = 0, coordinateZ = 0, count = 0, threshold = 0;

                switch (callbackType) {
                    case Constants.CALLBACK_TYPE_BLE_SCAN_SERVICE:
                        mBLEDevices = ((BLEScanService) mContext).mBLEDevices;

                        BLEScanService.commuteCycleFlag = true;
                        BLEScanService.commuteStatusFlag = false;

                        if (((BLEScanService) mContext).mBLEDevices.size() != 3)
                            return;

                        // Rssi 제한값 검사
                        for (Map<String, String> map : ((BLEScanService) mContext).EssentialDataArray) {
                            for (int j = 0; j < 3; j++) {
                                if (map.get("beacon_address1").equals(mBLEDevices.get(j).Address))
                                    mDeviceInfo1 = mBLEDevices.get(j);
                                else if (map.get("beacon_address2").equals(mBLEDevices.get(j).Address))
                                    mDeviceInfo2 = mBLEDevices.get(j);
                                else if (map.get("beacon_address3").equals(mBLEDevices.get(j).Address)) {
                                    mDeviceInfo3 = mBLEDevices.get(j);
                                    coordinateX = Integer.valueOf(map.get("coordinateX"));
                                    coordinateY = Integer.valueOf(map.get("coordinateY"));
                                    coordinateZ = Integer.valueOf(map.get("coordinateZ"));
                                    threshold = Integer.valueOf(map.get("thresholdX"));
                                }
                            }
                        }
                        break;

                    case Constants.CALLBACK_TYPE_CALIBRATION_SERVICE:
                        mBLEDevices = ((CalibrationService) mContext).mBLEDevices;

                        if (!CalibrationService.CalibrationFlag)
                            Log.d("ComeToWork", "start comeToWorkCheckTime");

                        if (((CalibrationService) mContext).mBLEDevices.size() != 3)
                            return;

                        // Rssi 제한값 검사
                        for (Map<String, String> map : ((CalibrationService) mContext).EssentialDataArray) {
                            for (int j = 0; j < 3; j++) {
                                if (map.get("beacon_address1").equals(mBLEDevices.get(j).Address))
                                    mDeviceInfo1 = mBLEDevices.get(j);
                                else if (map.get("beacon_address2").equals(mBLEDevices.get(j).Address))
                                    mDeviceInfo2 = mBLEDevices.get(j);
                                else if (map.get("beacon_address3").equals(mBLEDevices.get(j).Address)) {
                                    mDeviceInfo3 = mBLEDevices.get(j);
                                    coordinateX = Integer.valueOf(map.get("coordinateX"));
                                    coordinateY = Integer.valueOf(map.get("coordinateY"));
                                    coordinateZ = Integer.valueOf(map.get("coordinateZ"));
                                    threshold = Integer.valueOf(map.get("thresholdX"));
                                }
                            }
                        }
                        break;
                }

                while(true) {
                    int count_for = 0;

                    switch (callbackType) {
                        case Constants.CALLBACK_TYPE_BLE_SCAN_SERVICE:
                            mBLEDevices = ((BLEScanService) mContext).mBLEDevices;
                            for(DeviceInfo deviceInfo : mBLEDevices){
                                if(deviceInfo.Address.equals(mDeviceInfo1.Address))
                                    mDeviceInfo1 = deviceInfo;
                                else if(deviceInfo.Address.equals(mDeviceInfo2.Address))
                                    mDeviceInfo2 = deviceInfo;
                                else if(deviceInfo.Address.equals(mDeviceInfo3.Address))
                                    mDeviceInfo3 = deviceInfo;
                            }

                            if (mDeviceInfo1.Rssi > (coordinateX - threshold) && mDeviceInfo1.Rssi < (coordinateX + threshold)) {
                                count_for++;
                            }
                            if (mDeviceInfo2.Rssi > (coordinateY - threshold) && mDeviceInfo2.Rssi < (coordinateY + threshold)) {
                                count_for++;
                            }
                            if (mDeviceInfo3.Rssi > (coordinateZ - threshold) && mDeviceInfo3.Rssi < (coordinateZ + threshold)) {
                                count_for++;
                            }
                            break;

                        case Constants.CALLBACK_TYPE_CALIBRATION_SERVICE:
                            if(CalibrationService.CompleteCalibraton) {
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
                                ((CalibrationService) mContext).temporaryCalibrationData.put("ThresholdX", String.valueOf(threshold_Calibration));
                                ((CalibrationService) mContext).temporaryCalibrationData.put("ThresholdY", String.valueOf(threshold_Calibration));
                                ((CalibrationService) mContext).temporaryCalibrationData.put("ThresholdZ", String.valueOf(threshold_Calibration));
                                ((CalibrationService) mContext).mSocketIO.calibration(((CalibrationService) mContext).temporaryCalibrationData);

                                return;
                            }

                            mBLEDevices = ((CalibrationService) mContext).mBLEDevices;
                            for(DeviceInfo deviceInfo : mBLEDevices){
                                if(deviceInfo.Address.equals(mDeviceInfo1.Address))
                                    mDeviceInfo1 = deviceInfo;
                                else if(deviceInfo.Address.equals(mDeviceInfo2.Address))
                                    mDeviceInfo2 = deviceInfo;
                                else if(deviceInfo.Address.equals(mDeviceInfo3.Address))
                                    mDeviceInfo3 = deviceInfo;
                            }

                            if (mDeviceInfo1.Rssi > (coordinateX - threshold_Calibration) && mDeviceInfo1.Rssi < (coordinateX + threshold_Calibration)) {
                                count_for++;
                            }
                            if (mDeviceInfo2.Rssi > (coordinateY - threshold_Calibration) && mDeviceInfo2.Rssi < (coordinateY + threshold_Calibration)) {
                                count_for++;
                            }
                            if (mDeviceInfo3.Rssi > (coordinateZ - threshold_Calibration) && mDeviceInfo3.Rssi < (coordinateZ + threshold_Calibration)) {
                                count_for++;
                            }
                            break;
                    }

                    if(count_for >= 2) {
                        if(checkThreeTime.size() < 3)
                            checkThreeTime.add(true);
                        else if(checkThreeTime.size() == 3){
                            checkThreeTime.remove(0);
                            checkThreeTime.add(true);
                        }
                    } else {
                        if (checkThreeTime.size() < 3)
                            checkThreeTime.add(false);
                        else if (checkThreeTime.size() == 3) {
                            checkThreeTime.remove(0);
                            checkThreeTime.add(false);
                        }
                    }

                    int times = 0;
                    for(Boolean bool : checkThreeTime){
                        if(bool)
                            times++;
                    }

                    if (callbackType == Constants.CALLBACK_TYPE_BLE_SCAN_SERVICE) {
                        if (times >= 2) { // 출근존을 지났을 때
                            // BLEServiceUtils.sendEvent(mDeviceInfo1, mDeviceInfo2, mDeviceInfo3, true);
                            Log.d("ComeToWork", "comeToWork's zone");
                            GenerateNotification.generateNotification(BLEScanService.ServiceContext, "출근 대기 중", "출근 대기 중입니다.", "");
                            timerStart(mDeviceInfo1, mDeviceInfo2, mDeviceInfo3);
                            break;
                        }
                    } else if (callbackType == Constants.CALLBACK_TYPE_CALIBRATION_SERVICE) {
                        if(times >= 2){ // 출근존을 지났을 때
                            try {
                                ((CalibrationService) mContext).replyToActivityMessenger.send(Message.obtain(null, Constants.HANDLE_MESSAGE_TYPE_SETTEXT_ATTENDANCE_ZONE));
                                Log.d("MessengerCommunication", "Service send 3");
                            }catch(RemoteException e){
                                Log.d("replyToActivity", e.toString());
                            }
                        }else{
                            try {
                                ((CalibrationService) mContext).replyToActivityMessenger.send(Message.obtain(null, Constants.HANDLE_MESSAGE_TYPE_SETTEXT_NOT_ATTENDANCE_ZONE));
                                Log.d("MessengerCommunication", "Service send 4");
                            }catch(RemoteException e){
                                Log.d("replyToActivity", e.toString());
                            }
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

    private static HashMap<String, Integer> currentBeacons = new HashMap<>();

    /**
     * Input the beacon's bluetooth address to the map
     * @param bluetoothAddress
     */
    public static void setCurrentBeacons(String bluetoothAddress, int rssi) {
        if (!currentBeacons.containsKey(bluetoothAddress))
            currentBeacons.put(bluetoothAddress, rssi);
    }

    private void leaveWorkTimerStart(final DeviceInfo deviceInfo1, final DeviceInfo deviceInfo2, final DeviceInfo deviceInfo3) {
        timer = new Timer();
        timerSecond = 0;
        leaveWorkCount = 0;
        ((BLEScanService) mContext).restartScan(ScanSettings.SCAN_MODE_LOW_POWER);

        Log.d(TAG, "leaveWorkTimerStart(): Timer start and clear the map which has current beacons at each second");
        Log.d("Awesometic", "leaveWorkTimerStart(): Timer start and clear the map which has current beacons at each second");

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                timerSecond++;

                leaveWorkChecker(deviceInfo1, deviceInfo2, deviceInfo3);
            }
        }, 0, 6000);
    }

    private void leaveWorkTimerStop(final DeviceInfo deviceInfo1, final DeviceInfo deviceInfo2, final DeviceInfo deviceInfo3) {
        Log.d(TAG, "leaveWorkTimerStop(): Timer Stop");
        timer.cancel();
        timer = null;

        ((BLEScanService) mContext).timerHandler.removeCallbacks(updater);
        updater = null;
        sendEvent(deviceInfo1, deviceInfo2, deviceInfo3, false);
    }

    private void leaveWorkChecker(final DeviceInfo deviceInfo1, final DeviceInfo deviceInfo2, final DeviceInfo deviceInfo3) {
        updater = new Runnable() {
            @Override
            public void run() {
                if(timerSecond <= 1){
                    return;
                    // Thread.interrupted();
                }
                Log.d(TAG, "leaveWorkChecker(): currentBeacons size(): " + currentBeacons.size());
                Log.d("Awesometic", "leaveWorkChecker(): currentBeacons size(): " + currentBeacons.size());
                if (BLEScanService.commuteStatusFlag && currentBeacons.size() != 3) {
                    if(leaveWorkCount > 2) {
                        leaveWorkTimerStop(deviceInfo1, deviceInfo2, deviceInfo3);
                        // sendEvent(deviceInfo1, deviceInfo2, deviceInfo3, false);
                        Log.d(TAG, "leaveWorkChecker(): Get off the office success");
                        Log.d("Awesometic", "leaveWorkChecker(): Get off the office success");
                    }else{
                        leaveWorkCount++;
                    }
                } else if (currentBeacons.size() == 3){
                    if(currentBeacons.get(deviceInfo1.Address) < -100 && currentBeacons.get(deviceInfo2.Address) < -100 && currentBeacons.get(deviceInfo3.Address) < -100){
                        if(leaveWorkCount > 2) {
                            leaveWorkTimerStop(deviceInfo1, deviceInfo2, deviceInfo3);
                            // sendEvent(deviceInfo1, deviceInfo2, deviceInfo3, false);
                            Log.d(TAG, "leaveWorkChecker(): Get off the office success");
                            Log.d("Awesometic", "leaveWorkChecker(): Get off the office success");
                        }else{
                            leaveWorkCount++;
                        }
                    }else{
                        leaveWorkCount = 0;
                        currentBeacons.clear();
                    }
                } else {
                    leaveWorkCount = 0;
                    currentBeacons.clear();
                }
            }
        };
        ((BLEScanService) mContext).timerHandler.post(updater);
    }

    private void timerStart(final DeviceInfo deviceInfo1, final DeviceInfo deviceInfo2, final DeviceInfo deviceInfo3) {
        timerSecond = 0;
        timer = new Timer();
        Log.d(TAG, "timerStart(): start timer(ComeToWork)");

        // 출근 대기 상태 알림
        Intent intent = new Intent("android.intent.action.STAND_BY_COME_TO_WORK_STATE");
        intent.setData(Uri.parse("standByComeToWork:"));
        ((BLEScanService) mContext).sendBroadcast(intent);

    timer.schedule(new TimerTask() {
            @Override
            public void run() {
                timerSecond++;

                timerTextUpdate(deviceInfo1, deviceInfo2, deviceInfo3);
            }
        }, 0, 3000);
    }

    private void timerStop() {
        Log.d(TAG, "timerStop(): stop timer(ComeToWork)");
        timer.cancel();
        timer = null;

        ((BLEScanService) mContext).timerHandler.removeCallbacks(updater);
        updater = null;
    }

    private void timerTextUpdate(final DeviceInfo deviceInfo1, final DeviceInfo deviceInfo2, final DeviceInfo deviceInfo3) {
        updater = new Runnable() {
            @Override
            public void run() {
                if(timerSecond <= 1){
                    return;
                    // Thread.interrupted();
                }

                //timerText.setText(timerSecond + " 초");
                if(currentBeacons.size() != 3){ // 출근 범위를 벗어났을 경우 - 출근 조건을 만족하지 못함
                    Log.d(TAG, "timerTextUpdate(): comeToWork is failed(StandByAttendance))");
                    timerStop();
                    GenerateNotification.generateNotification(((BLEScanService) mContext), "출근 실패", "출근대기 중 범위를 벗어났습니다.", "");
                    BLEScanService.commuteStatusFlag = false;
                    BLEScanService.commuteCycleFlag = false;

                    // 퇴근 상태 알림
                    Intent intent = new Intent("android.intent.action.LEAVE_WORK_STATE");
                    intent.setData(Uri.parse("leaveWork:"));
                    ((BLEScanService) mContext).sendBroadcast(intent);

                } else if (timerSecond == 10) { // 출근 조건을 만족했을 경우 ( real second == timerSecond * 3 )
                    Log.d(TAG, "timerTextUpdate(): comeToWork success");
                    timerStop();

                    sendEvent(deviceInfo1, deviceInfo2, deviceInfo3, true);
                    leaveWorkTimerStart(deviceInfo1, deviceInfo2, deviceInfo3);
                }

                currentBeacons.clear();
            }
        };
        ((BLEScanService) mContext).timerHandler.post(updater);
    }

    public void sendEvent(DeviceInfo deviceInfo1, DeviceInfo deviceInfo2, DeviceInfo deviceInfo3, final boolean isComeToWork){
        // if((!BLEScanService.commuteCycleFlag && isComeToWork) || (BLEScanService.commuteCycleFlag && !isComeToWork)) {
        if (BLEScanService.commuteCycleFlag) {
            Map<String, String> data = new HashMap<String, String>();
            data.put("BeaconDeviceAddress1", deviceInfo1.Address);
            data.put("BeaconDeviceAddress2", deviceInfo2.Address);
            data.put("BeaconDeviceAddress3", deviceInfo3.Address);
            data.put("BeaconData1", deviceInfo1.ScanRecord);
            data.put("BeaconData2", deviceInfo2.ScanRecord);
            data.put("BeaconData3", deviceInfo3.ScanRecord);
            data.put("SmartphoneAddress", BLEScanService.myMacAddress);
            //data.put("DateTime", CurrentTime.currentTime());

            ((BLEScanService) mContext).mSocketIO.sendEvent(data, isComeToWork);
            if (isComeToWork) {
                BLEScanService.commuteStatusFlag = true;
                BLEScanService.commuteCycleFlag = true;

                // 출근 상태 알림
                Intent intent = new Intent("android.intent.action.COME_TO_WORK_STATE");
                intent.setData(Uri.parse("comeToWork:"));
                ((BLEScanService) mContext).sendBroadcast(intent);
            } else {
                BLEScanService.commuteStatusFlag = false;
                BLEScanService.commuteCycleFlag = false;
                ((BLEScanService) mContext).mBLEDevices.clear();

                // 퇴근 상태 알림
                Intent intent = new Intent("android.intent.action.LEAVE_WORK_STATE");
                intent.setData(Uri.parse("leaveWork:"));
                ((BLEScanService) mContext).sendBroadcast(intent);
            }
        }
    }
}
