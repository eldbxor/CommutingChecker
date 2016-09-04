package com.example.taek.commutingchecker.services;

import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.widget.Toast;

import com.example.taek.commutingchecker.utils.BLEServiceUtils;
import com.example.taek.commutingchecker.utils.Calibration;
//import com.example.taek.commutingchecker.utils.CheckCallback;
import com.example.taek.commutingchecker.utils.Constants;
import com.example.taek.commutingchecker.utils.DeviceInfo;
import com.example.taek.commutingchecker.utils.EnableBLE;
import com.example.taek.commutingchecker.utils.GenerateNotification;
import com.example.taek.commutingchecker.ui.SetupFragment;
import com.example.taek.commutingchecker.utils.SocketIO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BLEScanService extends Service {
    BluetoothAdapter mBluetoothAdapter; // Bluetooth 어뎁터
    BluetoothLeScanner mBLEScanner; // BLE 스캐너(api 21 이상)
    public static SocketIO mSocketIO; // Jason을 이용한 서버와의 통신 클래스
    public static List<DeviceInfo> mBLEDevices; // 비콘 디바이스 정보를 갖는 ArrayList
    private ScanSettings settings; // BLE 스캔 옵션 세팅
    public static List<ScanFilter> filters; // BLE 스캔 필터
    public static List<String> filterlist; // Api21 이하 버전용 BLE 스캔 필터
    public static String myMacAddress; // 스마트폰 블루투스 Mac 주소
    String TAG = "BLEScanService";
    Thread t; // 출퇴근 등록 쓰레드
    // Notification ID
    public static final int NOTIFICATION_ID = 1;
    BroadcastReceiver StopSelfReceiver, RequestDataReceiver, ShowDataReceiver, CalibrationReceiver;
    public static List<Map<String, String>> EssentialDataArray;
    public static Context ServiceContext;
    public static boolean ScanFlag, CalibrationFlag, CompleteCalibraton; // 출퇴근등록 쓰레드 실행 플래그, Rssi 측정 플래그
    public static boolean commuteCycle, calibrationResetFlag, commuteStatus;
//    public static boolean isCallbackRunning;
//    public static CheckCallback checkCallbackThread, checkCallbackThread_standByAttendance;
    public static int failureCount_SendEv; // sendEvent's Failure Count
    public static int failureCount_Cali; // Calibration's Failure Count
    public static Map<String, String> temporaryCalibrationData;
    public static Messenger replyToActivityMessenger;
    public static Handler timerHandler;

    public BLEScanService() {
    }

    class IncomingHandler extends Handler{
        @Override
        public void handleMessage(Message msg){
            switch (msg.what){
                case Constants.HANDLE_MESSAGE_TYPE_CALIBRATION:
                    Log.d("MessengerCommunication", "Service receive 1");
                    calibrationResetFlag = false;
                    CalibrationFlag = true;
                    CompleteCalibraton = false;
                    replyToActivityMessenger = msg.replyTo;

                    try{
                        do {
                            Thread.sleep(100);
                        } while (mSocketIO.connected() == false);
                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }
                    scanLeDevice(true);
                    Calibration.calibration();
                    break;
                case Constants.HANDLE_MESSAGE_TYPE_CALIBRATION_RESET:
                    Log.d("MessengerCommunication", "Service receive 2");
                    calibrationResetFlag = true;
                    Log.d("ServHandler_ResetFlag", String.valueOf(calibrationResetFlag));
                    break;
                case Constants.HANDLE_MESSAGE_TYPE_CEHCK_THRESHOLD:
                    Log.d("MessengerCommunication", "Service receive 3");
                    BLEServiceUtils.comeToWorkCheckTime();
                    break;
                case Constants.HANDLE_MESSAGE_TYPE_COMPLETE_CALIBRATION:
                    Log.d("MessengerCommunication", "Service receive 4");
                    CompleteCalibraton = true;
                    break;
                case Constants.HANDLE_MESSAGE_TYPE_SEEKBAR_VALUE_CHANGED:
                    Log.d("MessengerCommunication", "Service receive 5");
                    BLEServiceUtils.threshold_Calibration = msg.arg1;
                    break;
            }
        }
    }

    // Target we publish for clients to send messages to IncomingHandler.
    final Messenger incomingMessenger = new Messenger(new IncomingHandler());

    @Override
    public void onCreate(){
        Log.i(TAG, "Service onCreate");
        ScanFlag = true;
        CalibrationFlag = false;
        commuteCycle = false;
        ServiceContext = this;
//        isCallbackRunning = false;
        calibrationResetFlag = false;
        failureCount_SendEv = 0;
        failureCount_Cali = 0;

        // 비콘 Mac 주소를 저장할 ArrayList
        filterlist = new ArrayList<String>();

        // 비콘 디바이스 정보
        mBLEDevices = new ArrayList<DeviceInfo>();

        // 비콘의 Mac 주소와 제한 값을 저장할 Map
        EssentialDataArray = new ArrayList<Map<String, String>>();

        // 소켓 생성
        this.mSocketIO = new SocketIO();

        timerHandler = new Handler();

        // 리시버 등록
        IntentFilter StopSelfPkgFilter = new IntentFilter();
        IntentFilter RequestDataPkgFilter = new IntentFilter();
        IntentFilter ShowDataPkgFilter = new IntentFilter();
        IntentFilter CalibrationPkgFilter = new IntentFilter();
        StopSelfPkgFilter.addAction("android.intent.action.STOP_SERVICE");
        StopSelfPkgFilter.addDataScheme("StopSelf");
        RequestDataPkgFilter.addAction("android.intent.action.REQUEST_DATA");
        RequestDataPkgFilter.addDataScheme("RequestData");
        ShowDataPkgFilter.addAction("android.intent.action.SHOW_DATA");
        ShowDataPkgFilter.addDataScheme("ShowData");
        CalibrationPkgFilter.addAction("android.intent.action.CALIBRATION_SERVICE");
        CalibrationPkgFilter.addDataScheme("Calibration");

        StopSelfReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                stopSelf();
            }
        };

        RequestDataReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                try{
                    // Getting a public key from server
                    mSocketIO.getServersRsaPublicKey(myMacAddress);

                    BLEScanService.mSocketIO.requestEssentialData(SocketIO.SERVICE_CALLBACK);
                }catch (Exception e){
                    e.printStackTrace();
                    Toast.makeText(BLEScanService.ServiceContext, "서비스 실행상태가 아닙니다.", Toast.LENGTH_LONG).show();
                }
            }
        };

        ShowDataReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                try{
                    if(BLEScanService.EssentialDataArray.size() > 0) {
                        GenerateNotification.generateNotification(BLEScanService.ServiceContext, "ShowData", "Data_Info",
                                "사무실 총 수: " + + BLEScanService.EssentialDataArray.size() + ", 좌표 값: "
                                        //+ "사무실 번호: " + BLEScanService.EssentialDataArray.get(0).get("id_workplace").toString() + ","
                                        //+ BLEScanService.EssentialDataArray.get(0).get("beacon_address1").toString() + ": "
                                        + BLEScanService.EssentialDataArray.get(0).get("coordinateX").toString() + ","
                                       // + BLEScanService.EssentialDataArray.get(0).get("beacon_address2").toString() + ": "
                                        + BLEScanService.EssentialDataArray.get(0).get("coordinateY").toString() + ", "
                                        //+ BLEScanService.EssentialDataArray.get(0).get("beacon_address3").toString() + ": "
                                        + BLEScanService.EssentialDataArray.get(0).get("coordinateZ").toString());
                        Log.d("ShowData", BLEScanService.EssentialDataArray.get(0).get("coordinateX").toString() + ", " +
                                BLEScanService.EssentialDataArray.get(0).get("coordinateY").toString() + ", " +
                                BLEScanService.EssentialDataArray.get(0).get("coordinateZ").toString());
                    }else{
                        GenerateNotification.generateNotification(BLEScanService.ServiceContext, "ShowData", "No Data",
                                "");
                        //Toast.makeText(BLEScanService.ServiceContext, "no data", Toast.LENGTH_SHORT).show();
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    GenerateNotification.generateNotification(BLEScanService.ServiceContext, "ShowData", "ShowData failed", "");
                    //Toast.makeText(BLEScanService.ServiceContext, "서비스 실행상태가 아닙니다.", Toast.LENGTH_LONG).show();
                }
            }
        };

        CalibrationReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                CalibrationFlag = true;
            }
        };

        registerReceiver(StopSelfReceiver, StopSelfPkgFilter);
        registerReceiver(RequestDataReceiver, RequestDataPkgFilter);
        registerReceiver(ShowDataReceiver, ShowDataPkgFilter);
        registerReceiver(CalibrationReceiver, CalibrationPkgFilter);

        EnableBLE mEnableBLE = new EnableBLE(getSystemService(this.BLUETOOTH_SERVICE)); // BLE 활성화 클래스 생성
        mBluetoothAdapter = mEnableBLE.enable(); // BLE 활성화

        // waiting for stating bluetooth on
        try{
            do {
                Log.d("BLEScan", "BLEScanService onCreate(): waiting for stating bluetooth on");
                Thread.sleep(100);
            } while (mBluetoothAdapter.getState() != mBluetoothAdapter.STATE_ON);
        }catch (InterruptedException e){
            e.printStackTrace();
        }

        if(mBluetoothAdapter != null){ // && mBluetoothAdapter.isEnabled()){
            if(Build.VERSION.SDK_INT >= 21){
                mBLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
                if(Build.VERSION.SDK_INT == Build.VERSION_CODES.M){
                    settings = new ScanSettings.Builder()
                            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                            .setCallbackType(Constants.CALLBACK_TYPE_ALL_MATCHES)
                            .build();
                }else if(Build.VERSION.SDK_INT >= 21 && Build.VERSION.SDK_INT < 23) {
                    settings = new ScanSettings.Builder()
                            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                            .build();
                }
                filters = new ArrayList<ScanFilter>();
            }

            myMacAddress = android.provider.Settings.Secure.getString(this.getContentResolver(), "bluetooth_address");
            Log.d("myMacAddress", myMacAddress);

            // BLEScanner 객체 확인
            if(mBLEScanner == null && Build.VERSION.SDK_INT >= 21){
                Log.d("BLEScan", "BLEScanService onCreate(): mBLEScanner is null");
                Toast.makeText(this, "Can not find BLE Scanner", Toast.LENGTH_SHORT).show();
                return;
            }

        }

        // 소켓 연결
        /*
        if(!mSocketIO.mSocket.connected()) {
            Log.d("Service's SocketIO", "not connected");
            mSocketIO.connect();
        }else
            Log.d("Service's SocketIO", "already connected");
        */
        this.mSocketIO.connect();

        // To Wait for connecting
        try{
            do {
                Thread.sleep(100);
            } while (mSocketIO.connected() == false);
        }catch (InterruptedException e){
            e.printStackTrace();
        }

        // Getting a public key from server
        mSocketIO.getServersRsaPublicKey();

        try{
            do {
                Thread.sleep(100);
            } while(!mSocketIO.isServersPublicKeyInitialized());
        }catch (InterruptedException e){
            e.printStackTrace();
        }

        // 서버에 Rssi 제한 값 요청 후 데이터 받기
        mSocketIO.requestEssentialData(SocketIO.SERVICE_CALLBACK);
        try{
            do {
                Thread.sleep(100);
            }while(EssentialDataArray.size() == 0);
        }catch (InterruptedException e){
            e.printStackTrace();
        }

        Log.d("EssentialData's size", EssentialDataArray.size() + "");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        Runnable r = new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Service onStartCommand");


                /*
                Log.d("FilterListSize", String.valueOf(filterlist.size()));
                for(int i = 0; i < filterlist.size(); i++) {
                    Log.d("FilterList", String.valueOf(i) + ": " + filterlist.get(i));
                } */

                // 스캔 시작
                scanLeDevice(true);

                while(ScanFlag) {
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    // 3개의 비콘 정보가 없을 경우 continue
                    if (mBLEDevices.size() != 3)
                        continue;

                    // 0.5초 동안 3번 Rssi 체크 후 2번 이상 적합하면 sendEvent() 메서드 실행
                    if (!commuteCycle && !commuteStatus)
                        BLEServiceUtils.comeToWorkCheckTime();

                    //mSocketIO.sendEvent(new HashMap<String, String>());
                    //scanLeDevice(false);
                    //ScanFlag = false;
                }
            } // End of while
        };

        t = new Thread(r);
        t.start();
        // return Service.START_STICKY;
        return Service.START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "BLEScanService onBind()");
        return incomingMessenger.getBinder();
    }

    private void scanLeDevice(final boolean enable){
        if(enable){
            if(Build.VERSION.SDK_INT < 21){
                // 롤리팝 이전버전
                mBluetoothAdapter.startLeScan(mLeScanCallback);
            }else{
                for(Map<String, String> map : EssentialDataArray){
                    filters.add(new ScanFilter.Builder().setDeviceAddress(map.get("beacon_address1")).build());
                    filters.add(new ScanFilter.Builder().setDeviceAddress(map.get("beacon_address2")).build());
                    filters.add(new ScanFilter.Builder().setDeviceAddress(map.get("beacon_address3")).build());
                }
                mBLEScanner.startScan(filters, settings, mScanCallback);
            }
        }else{
            if(Build.VERSION.SDK_INT < 21){
                // 롤리팝 이전버전
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
            }else{
                mBLEScanner.stopScan(mScanCallback);
            }
        }
    }

    @Override
    public void onDestroy(){
        Log.i(TAG, "Service onDestroy");
        ScanFlag = false;
        scanLeDevice(false); // 스캔 중지
        unregisterReceiver(StopSelfReceiver);
        unregisterReceiver(RequestDataReceiver);
        unregisterReceiver(ShowDataReceiver);
        unregisterReceiver(CalibrationReceiver);
        if(mSocketIO.connected() == true)
            mSocketIO.close();
        GenerateNotification.generateNotification(this, "서비스 종료", "서비스가 종료되었습니다.", "");
    }

    // uuid, major, minor 나누는 메서드
    private List<String> separate(byte[] scanRecord) {
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

    // api 21 이상
    @SuppressLint("NewApi")
    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
//            isCallbackRunning = true;
            /*
            Log.d("ScanRecord", "Running");
            Log.d("ScanRecord", result.getScanRecord().toString());
*/
            List<String> separatedData = separate(result.getScanRecord().getBytes());

            // public DeviceInfo(BluetoothDevice device, String address, String scanRecord, String uuid, String major, String minor, int rssi)
            BLEServiceUtils.addDeviceInfo(new DeviceInfo(result.getDevice(), result.getDevice().getAddress(), separatedData.get(0),
                    separatedData.get(1), separatedData.get(2), separatedData.get(3), result.getRssi()));

            BLEServiceUtils.setCurrentBeacons(result.getDevice().getAddress());
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }
    };

    // api 21 미만
    BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            /*
            Log.d("ScanRecord", "Running");
            Log.d("filterList's size", String.valueOf(filterlist.size()));
            Log.d("Essential Data's size", String.valueOf(EssentialDataArray.size()));
*/
            // 비콘 Mac 주소 필터링
            boolean filtering = false;

            for(String deviceMacAddress : filterlist){
                if((String.valueOf(deviceMacAddress)).equals(device.getAddress())){
                    filtering = true;
                    break;
                }
                /*
                if(device.getAddress().equals(deviceMacAddress)){
                    filtering = true;
                    break;
                }*/
            }
            if(!filtering) return;

//            isCallbackRunning = true;

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
/*
            Log.d("AllOfScanRecord", all + ", " + uuid + ", " + String.valueOf(major_int) + ", " + String.valueOf(minor_int));
*/
            BLEServiceUtils.addDeviceInfo(new DeviceInfo(device, device.getAddress(), all,
                    uuid, String.valueOf(major_int), String.valueOf(minor_int), rssi));

            BLEServiceUtils.setCurrentBeacons(device.getAddress());
        }
    };
}
