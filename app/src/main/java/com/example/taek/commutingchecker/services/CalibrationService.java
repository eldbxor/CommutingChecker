package com.example.taek.commutingchecker.services;

import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Messenger;
import android.util.Log;
import android.widget.Toast;

import com.example.taek.commutingchecker.utils.BLEServiceUtils;
import com.example.taek.commutingchecker.utils.Calibration;
import com.example.taek.commutingchecker.utils.Constants;
import com.example.taek.commutingchecker.utils.DeviceInfo;
import com.example.taek.commutingchecker.utils.GenerateNotification;
import com.example.taek.commutingchecker.utils.IncomingHandler;
import com.example.taek.commutingchecker.utils.RegisterReceiver;
import com.example.taek.commutingchecker.utils.SocketIO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CalibrationService extends Service {
    public static String myMacAddress; // 스마트폰 블루투스 Mac 주소
    public static Context ServiceContext;
    public static boolean CalibrationFlag, CompleteCalibraton, calibrationResetFlag;
    public static int failureCount_Cali; // Calibration's Failure Count
    public static Messenger replyToActivityMessenger; // Activity에 응답하기 위한 Messenger
    public Map<String, String> temporaryCalibrationData; // 서버에 Calibration 데이터를 보내기 위한 임시 저장소
    public SocketIO mSocketIO; // Jason을 이용한 서버와의 통신 클래스
    public List<DeviceInfo> mBLEDevices; // 비콘 디바이스 정보를 갖는 ArrayList
    private ScanSettings settings; // BLE 스캔 옵션 세팅
    public List<ScanFilter> filters; // BLE 스캔 필터
    public List<String> filterlist; // Api21 이하 버전용 BLE 스캔 필터
    public BLEServiceUtils mBLEServiceUtils;
    private String TAG = "CalibrationService";
    public List<Map<String, String>> EssentialDataArray; // 서버에서 받아온 비콘 데이터
    public Calibration mCalibration;

    // Target we publish for clients to send messages to IncomingHandler.
    private Messenger incomingMessenger = new Messenger(new IncomingHandler(Constants.HANDLER_TYPE_SERVICE, CalibrationService.this));

    public CalibrationService() {
    }

    @Override
    public void onCreate(){
        Log.i(TAG, "CalibrationService onCreate");
        CalibrationFlag = false;
        ServiceContext = this;
//        isCallbackRunning = false;
        calibrationResetFlag = false;
        failureCount_Cali = 0;

        mCalibration = new Calibration(ServiceContext);

        mBLEServiceUtils = new BLEServiceUtils(ServiceContext);

        // 비콘 Mac 주소를 저장할 ArrayList
        filterlist = new ArrayList<String>();

        // 비콘 디바이스 정보
        mBLEDevices = new ArrayList<DeviceInfo>();

        // 비콘의 Mac 주소와 제한 값을 저장할 Map
        EssentialDataArray = new ArrayList<Map<String, String>>();

        // 소켓 생성
        this.mSocketIO = new SocketIO(ServiceContext);

        mBLEServiceUtils.createBluetoothAdapter(getSystemService(this.BLUETOOTH_SERVICE)); // Bluetooth Adapter 생성
        mBLEServiceUtils.enableBluetooth(); // Bluetooth 사용

        // waiting for stating bluetooth on
        try{
            do {
                Log.d(TAG, "onCreate(): waiting for stating bluetooth on");
                Thread.sleep(100);
            } while (mBLEServiceUtils.mBluetoothAdapter.getState() != mBLEServiceUtils.mBluetoothAdapter.STATE_ON);
        }catch (InterruptedException e){
            e.printStackTrace();
        }

        if(mBLEServiceUtils.mBluetoothAdapter != null){ // && mBluetoothAdapter.isEnabled()){
            if(Build.VERSION.SDK_INT >= 21){
                mBLEServiceUtils.mBLEScanner = mBLEServiceUtils.mBluetoothAdapter.getBluetoothLeScanner();
                settings = mBLEServiceUtils.setPeriod(ScanSettings.SCAN_MODE_LOW_LATENCY);
                filters = new ArrayList<ScanFilter>();
            }

            myMacAddress = android.provider.Settings.Secure.getString(this.getContentResolver(), "bluetooth_address");
            Log.d(TAG, "onCreate(): myMacAddress: " + myMacAddress);

            // BLEScanner 객체 확인
            if(mBLEServiceUtils.mBLEScanner == null && Build.VERSION.SDK_INT >= 21) {
                Log.d(TAG, "onCreate(): mBLEScanner is null");
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
        Log.d(TAG, "onCreate(): waiting for connecting a socket");

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
        Log.d(TAG, "onCreate(): Getting a public key from server");

        try{
            do {
                Thread.sleep(100);
            } while(!mSocketIO.isServersPublicKeyInitialized());
        }catch (InterruptedException e){
            e.printStackTrace();
        }

        // 서버에 Rssi 제한 값 요청 후 데이터 받기
        mSocketIO.requestEssentialData(Constants.CALLBACK_TYPE_CALIBRATION_SERVICE);
        Log.d(TAG, "onCreate(): requestEssentialData");
        try{
            do {
                Thread.sleep(100);
            }while(EssentialDataArray.size() == 0);
        }catch (InterruptedException e){
            e.printStackTrace();
        }

        Log.d(TAG, "onCreate(): EssentialData's size: " + EssentialDataArray.size());
    }

    public void scanLeDevice(final boolean enable){
        if(enable){
            if(Build.VERSION.SDK_INT < 21){
                // 롤리팝 이전버전
                mBLEServiceUtils.mBluetoothAdapter.startLeScan(mLeScanCallback);
            }else{
                for(Map<String, String> map : EssentialDataArray){
                    filters.add(new ScanFilter.Builder().setDeviceAddress(map.get("beacon_address1")).build());
                    filters.add(new ScanFilter.Builder().setDeviceAddress(map.get("beacon_address2")).build());
                    filters.add(new ScanFilter.Builder().setDeviceAddress(map.get("beacon_address3")).build());
                }
                mBLEServiceUtils.mBLEScanner.startScan(filters, settings, mScanCallback);
            }
        }else{
            if(Build.VERSION.SDK_INT < 21){
                // 롤리팝 이전버전
                mBLEServiceUtils.mBluetoothAdapter.stopLeScan(mLeScanCallback);
            }else{
                mBLEServiceUtils.mBLEScanner.stopScan(mScanCallback);
                filters.clear();
            }
        }
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
            List<String> separatedData = mBLEServiceUtils.separate(result.getScanRecord().getBytes());

            // public DeviceInfo(BluetoothDevice device, String address, String scanRecord, String uuid, String major, String minor, int rssi)
            mBLEServiceUtils.addDeviceInfo(Constants.CALLBACK_TYPE_CALIBRATION_SERVICE, new DeviceInfo(result.getDevice(), result.getDevice().getAddress(), separatedData.get(0),
                    separatedData.get(1), separatedData.get(2), separatedData.get(3), result.getRssi()));

            mBLEServiceUtils.setCurrentBeacons(result.getDevice().getAddress(), result.getRssi());
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
            mBLEServiceUtils.addDeviceInfo(Constants.CALLBACK_TYPE_CALIBRATION_SERVICE, new DeviceInfo(device, device.getAddress(), all,
                    uuid, String.valueOf(major_int), String.valueOf(minor_int), rssi));

            mBLEServiceUtils.setCurrentBeacons(device.getAddress(), rssi);
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "BLEScanService onBind()");
        return incomingMessenger.getBinder();
    }

    @Override
    public void onDestroy(){
        Log.i(TAG, "Service onDestroy");
        scanLeDevice(false); // 스캔 중지
        if(mSocketIO.connected() == true)
            mSocketIO.close();
        GenerateNotification.generateNotification(this, "CommutingChecker", "서비스 종료", "서비스가 종료되었습니다.");
    }
}
