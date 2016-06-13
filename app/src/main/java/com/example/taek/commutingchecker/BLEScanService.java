package com.example.taek.commutingchecker;

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
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BLEScanService extends Service {
    BluetoothAdapter mBluetoothAdapter; // Bluetooth 어뎁터
    BluetoothLeScanner mBLEScanner; // BLE 스캐너(api 21 이상)
    public static SocketIO mSocketIO; // Jason을 이용한 서버와의 통신 클래스
    public static List<DeviceInfo> mBLEDevices; // 비콘 디바이스 정보를 갖는 ArrayList
    private ScanSettings settings; // BLE 스캔 옵션 세팅
    public static List<ScanFilter> filters; // BLE 스캔 필터
    public static String myMacAddress; // 스마트폰 블루투스 Mac 주소
    // private static String sendTime; // 서버에 데이터를 보낸 시간
    String TAG = MainActivity.ServiceTAG;
    Thread t; // 출퇴근 등록 쓰레드
    public static boolean ScanFlag, CalibrationFlag; // 출퇴근등록 쓰레드 실행 플래그, Rssi 측정 플래그
    // Notification ID
    public static final int NOTIFICATION_ID = 1;
    BroadcastReceiver StopSelfReceiver, CalibrationReceiver;
    public static List<String> filterlist;
    public static List<Map<String, String>> EssentialDataArray;
    public static Context ServiceContext;
    public static boolean coolTime, isCallbackRunning;
    public static CheckCallback checkCallbackThread;
    /**
     * Trigger a callback for every Bluetooth advertisement found that matches the filter criteria.
     * If no filter is active, all advertisement packets are reported.
     */
    public static final int CALLBACK_TYPE_ALL_MATCHES = 1;

    /**
     * A result callback is only triggered for the first advertisement packet received that matches
     * the filter criteria.
     */
    public static final int CALLBACK_TYPE_FIRST_MATCH = 2;

    /**
     * Receive a callback when advertisements are no longer received from a device that has been
     * previously reported by a first match callback.
     */
    public static final int CALLBACK_TYPE_MATCH_LOST = 4;

    public BLEScanService() {
    }

    @Override
    public void onCreate(){
        Log.i(TAG, "Service onCreate");
        ScanFlag = true;
        CalibrationFlag = false;
        coolTime = false;
        ServiceContext = this;
        isCallbackRunning = false;
        // 비콘 Mac 주소를 저장할 ArrayList
        filterlist = new ArrayList<String>();

        // 비콘 디바이스 정보
        mBLEDevices = new ArrayList<DeviceInfo>();

        // 비콘의 Mac 주소와 제한 값을 저장할 Map
        EssentialDataArray = new ArrayList<Map<String, String>>();

        // 소켓 생성
        mSocketIO = new SocketIO();

        // 리시버 등록
        IntentFilter StopSelfPkgFilter = new IntentFilter();
        IntentFilter CalibrationPkgFilter = new IntentFilter();
        StopSelfPkgFilter.addAction("android.intent.action.STOP_SERVICE");
        StopSelfPkgFilter.addDataScheme("StopSelf");
        CalibrationPkgFilter.addAction("android.intent.action.CALIBRATION_SERVICE");
        CalibrationPkgFilter.addDataScheme("Calibration");

        StopSelfReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                stopSelf();
            }
        };

        CalibrationReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                CalibrationFlag = true;
            }
        };

        registerReceiver(StopSelfReceiver, StopSelfPkgFilter);
        registerReceiver(CalibrationReceiver, CalibrationPkgFilter);

        EnableBLE mEnableBLE = new EnableBLE(getSystemService(this.BLUETOOTH_SERVICE)); // BLE 활성화 클래스 생성
        mBluetoothAdapter = mEnableBLE.enable(); // BLE 활성화

        if(mBluetoothAdapter != null){ // && mBluetoothAdapter.isEnabled()){
            if(Build.VERSION.SDK_INT >= 21){
                mBLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
                if(Build.VERSION.SDK_INT == Build.VERSION_CODES.M){
                    settings = new ScanSettings.Builder()
                            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                            .setCallbackType(CALLBACK_TYPE_ALL_MATCHES)
                            .build();
                }else if(Build.VERSION.SDK_INT >= 21 && Build.VERSION.SDK_INT < 23) {
                    settings = new ScanSettings.Builder()
                            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                            .build();
                }
                filters = new ArrayList<ScanFilter>();

                // 스캔 필터 리스트 추가
                for(String deviceMacAddress : filterlist){
                    ScanFilter filter = new ScanFilter.Builder().setDeviceAddress(deviceMacAddress).build();
                    filters.add(filter);
                }
            }

            myMacAddress = android.provider.Settings.Secure.getString(this.getContentResolver(), "bluetooth_address");
            Log.d("myMacAddress", myMacAddress);

            // BLEScanner 객체 확인
            if(mBLEScanner == null && Build.VERSION.SDK_INT >= 21){
                Toast.makeText(this, "Can not find BLE Scanner", Toast.LENGTH_SHORT).show();
                return;
            }

        }

        // 소켓 연결
        mSocketIO.connect();

        // 서버에 Rssi 제한 값 요청 후 데이터 받기
        //mSocketIO.requestEssentialData();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        Runnable r = new Runnable() {
            @Override
            public void run() {
                // To Wait for connecting
                try{
                    Thread.sleep(1000);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }

                // 서버에 Rssi 제한 값 요청 후 데이터 받기
                mSocketIO.requestEssentialData();

                try{
                    Thread.sleep(500);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }

                Log.d("FilterListSize", String.valueOf(filterlist.size()));
                for(int i = 0; i < filterlist.size(); i++) {
                    Log.d("FilterList", String.valueOf(i) + ": " + filterlist.get(i));
                }

                // 스캔 시작
                scanLeDevice(true);

                try{
                    Thread.sleep(5000);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }

                while(ScanFlag) {
                    if (CalibrationFlag) {  // if you receive Calibration_Broadcast
                        Calibration.calibration();
                        CalibrationFlag = false;
                        mSocketIO.requestEssentialData();
                    }
                    else {
                        try {
                            Thread.sleep(300);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        // 3개의 비콘 정보가 없을 경우 continue
                        if (mBLEDevices.size() != 3)
                            continue;

                        // 0.5초 동안 3번 Rssi 체크 후 2번 이상 적합하면 sendEvent() 메서드 실행
                        if(!coolTime)
                            CheckTime.checkTime();

                        //mSocketIO.sendEvent(new HashMap<String, String>());
                        //scanLeDevice(false);
                        //ScanFlag = false;
                    }
                } // End of while
            }
        };

        t = new Thread(r);
        t.start();
        return Service.START_STICKY;
    }

    public static void test(){
        mSocketIO.test();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");
        return null; // 바운드 서비스가 아니라는 것을 안드로이드 시스템에 알려주기 위함
    }

    private void scanLeDevice(final boolean enable){
        if(enable){
            if(Build.VERSION.SDK_INT < 21){
                // 롤리팝 이전버전
                mBluetoothAdapter.startLeScan(mLeScanCallback);
            }else{
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
            isCallbackRunning = true;
            Log.d("ScanRecord", "Running");
            Log.d("ScanRecord", result.getScanRecord().toString());

            List<String> separatedData = separate(result.getScanRecord().getBytes());

            AddDeviceInfo.addDeviceInfo(new DeviceInfo(result.getDevice(), result.getDevice().getAddress(), separatedData.get(0),
                    separatedData.get(1), separatedData.get(2), separatedData.get(3), result.getRssi()));
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

            isCallbackRunning = true;
            Log.d("ScanRecord", "Running");
            Log.d("filterList's size", String.valueOf(filterlist.size()));
            Log.d("Essential Data's size", String.valueOf(EssentialDataArray.size()));
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

            Log.d("AllOfScanRecord", all + ", " + uuid + ", " + String.valueOf(major_int) + ", " + String.valueOf(minor_int));

            AddDeviceInfo.addDeviceInfo(new DeviceInfo(device, device.getAddress(), all,
                    uuid, String.valueOf(major_int), String.valueOf(minor_int), rssi));
        }
    };
}
