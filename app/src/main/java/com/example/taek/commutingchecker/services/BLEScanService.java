package com.example.taek.commutingchecker.services;

import android.annotation.SuppressLint;
import android.app.Notification;
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
import android.nfc.Tag;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

import com.example.taek.commutingchecker.utils.BLEServiceUtils;
import com.example.taek.commutingchecker.utils.Constants;
import com.example.taek.commutingchecker.utils.DeviceInfo;
import com.example.taek.commutingchecker.utils.GenerateNotification;
import com.example.taek.commutingchecker.utils.RegisterReceiver;
import com.example.taek.commutingchecker.utils.SocketIO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BLEScanService extends Service {
    public static String myMacAddress; // 스마트폰 블루투스 Mac 주소
    public static Context ServiceContext;
    public static boolean scanFlag, commuteCycleFlag, commuteStatusFlag, lowPowerScanFlag, networkDisconnectedFlag, screenOnFlag, isRunningCommutingThreadFlag;
    public static int failureCount_SendEv; // sendCommutingEvent's Failure Count
    public SocketIO mSocketIO; // Jason을 이용한 서버와의 통신 클래스
    public List<DeviceInfo> mBLEDevices; // 비콘 디바이스 정보를 갖는 ArrayList
    private ScanSettings settings; // BLE 스캔 옵션 세팅
    public List<ScanFilter> filters; // BLE 스캔 필터
    public List<String> filterlist; // Api21 이하 버전용 BLE 스캔 필터
    public Handler timerHandler, leaveWorkTimerHandler, sendCommutingEventInQueueHandler;
    public BLEServiceUtils mBLEServiceUtils;
    private String TAG = "BLEScanService";
    private Thread commutingThread; // 출퇴근 등록 쓰레드
    private BroadcastReceiver StopSelfReceiver, RequestDataReceiver, ShowDataReceiver, NetworkChangeReceiver, ScreenOnReceiver, ScreenOffReceiver;
    public List<Map<String, String>> EssentialDataArray; // 서버에서 받아온 비콘 데이터
    private Notification mNotification;
    public PowerManager mPowerManager;
    public PowerManager.WakeLock mWakeLock;
    public Vibrator mVibrator;
    public SendMessageHandler mSendMessageHandler;
    public CommutingThread mCommutingThread;

    class SendMessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case 1:
                    Log.d(TAG, "SendMessageHandler - handleMessage()");
                    if (mCommutingThread == null) {
                        mCommutingThread = new CommutingThread();
                        mCommutingThread.start();
                    } else {
                        if (!(mCommutingThread.isRunning)) {
                            mCommutingThread = null;
                            mCommutingThread = new CommutingThread();
                            mCommutingThread.start();
                        }
                    }
                    break;
                case 2:
                    break;
            }
        }
    }

    class CommutingThread extends Thread implements Runnable {
        private boolean isRunning = false;
        private int failureCount = 0;

        public CommutingThread() {
            isRunning = true;
        }

        public void stopThread() {
            Log.d(TAG, "CommutingThread - stopThread()");
            isRunning = false;
        }

        @Override
        public void run() {
            super.run();

            Log.d(TAG, "CommutingThread - run()");
            isRunningCommutingThreadFlag = true;

            while (isRunning) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if(failureCount == 10) {
                    stopThread();
                    break;
                }
                // 3개의 비콘 정보가 없을 경우 continue
                if (mBLEDevices.size() != 3) {
                    Log.d(TAG, "CommutingThread - run(): failureCount++");
                    failureCount++;
                    continue;
                } else if (mBLEDevices.size() == 0) { // 비콘이 검색되지 않으면 Low power 모드로 스캔
                    restartScan(ScanSettings.SCAN_MODE_LOW_POWER);
                }

                // 0.5초 동안 3번 Rssi 체크 후 2번 이상 적합하면 sendCommutingEvent() 메서드 실행
                if (!commuteCycleFlag && !commuteStatusFlag) {
                    // Low Latency 모드로 스캔하기 위한 Screen on
                    mBLEServiceUtils.wakeScreen(ServiceContext);

                    // low Latency 스캔 모드로 변경
                    restartScan(ScanSettings.SCAN_MODE_LOW_LATENCY);

                    // 출근 알고리즘 실행
                    mBLEServiceUtils.comeToWorkCheckTime(Constants.CALLBACK_TYPE_BLE_SCAN_SERVICE);

                    stopThread();
                    break;
                }
            }
        }
    }

    // 생성자
    public BLEScanService() {
    }

    @Override
    public void onCreate(){
        Log.i(TAG, "Service onCreate");
        scanFlag = true;
        commuteCycleFlag = false;
        commuteStatusFlag = false;
        ServiceContext = this;
        screenOnFlag = true;
        isRunningCommutingThreadFlag = false;
        failureCount_SendEv = 0;

        mSendMessageHandler = new SendMessageHandler();

        mPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        mBLEServiceUtils = new BLEServiceUtils(ServiceContext);

        // 비콘 Mac 주소를 저장할 ArrayList
        filterlist = new ArrayList<String>();

        // 비콘 디바이스 정보
        mBLEDevices = new ArrayList<DeviceInfo>();

        // 비콘의 Mac 주소와 제한 값을 저장할 Map
        EssentialDataArray = new ArrayList<Map<String, String>>();

        // 소켓 생성
        this.mSocketIO = new SocketIO(ServiceContext);

        timerHandler = new Handler();
        leaveWorkTimerHandler = new Handler();
        sendCommutingEventInQueueHandler = new Handler();

        // 리시버 등록
        RegisterReceiver mRegisterReceiver = new RegisterReceiver(ServiceContext);

        StopSelfReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                stopSelf();
            }
        };

        RequestDataReceiver = mRegisterReceiver.createReceiver(Constants.BROADCAST_RECEIVER_TYPE_REQEUST_DATA);
        ShowDataReceiver = mRegisterReceiver.createReceiver(Constants.BROADCAST_RECEIVER_TYPE_SHOW_DATA);
        NetworkChangeReceiver = mRegisterReceiver.createReceiver(Constants.BROADCAST_RECEIVER_TYPE_NETWORK_CHANGE);
        ScreenOffReceiver = mRegisterReceiver.createReceiver(Constants.BROADCAST_RECEIVER_TYPE_SCREEN_OFF);
        ScreenOnReceiver = mRegisterReceiver.createReceiver(Constants.BROADCAST_RECEIVER_TYPE_SCREEN_ON);
        registerReceiver(StopSelfReceiver, mRegisterReceiver.createPackageFilter(Constants.BROADCAST_RECEIVER_TYPE_STOP_SERVICE));
        registerReceiver(RequestDataReceiver, mRegisterReceiver.createPackageFilter(Constants.BROADCAST_RECEIVER_TYPE_REQEUST_DATA));
        registerReceiver(ShowDataReceiver, mRegisterReceiver.createPackageFilter(Constants.BROADCAST_RECEIVER_TYPE_SHOW_DATA));
        registerReceiver(NetworkChangeReceiver, mRegisterReceiver.createPackageFilter(Constants.BROADCAST_RECEIVER_TYPE_NETWORK_CHANGE));
        registerReceiver(ScreenOffReceiver, mRegisterReceiver.createPackageFilter(Constants.BROADCAST_RECEIVER_TYPE_SCREEN_OFF));
        registerReceiver(ScreenOnReceiver, mRegisterReceiver.createPackageFilter(Constants.BROADCAST_RECEIVER_TYPE_SCREEN_ON));


        mBLEServiceUtils.createBluetoothAdapter(getSystemService(this.BLUETOOTH_SERVICE)); // Bluetooth Adapter 생성
        mBLEServiceUtils.enableBluetooth(); // Bluetooth 사용

        // Service wakeLock
        acquireWakeLock(PowerManager.PARTIAL_WAKE_LOCK);

        // waiting for stating bluetooth on
        try{
            do {
                Log.d("BLEScan", "BLEScanService onCreate(): waiting for stating bluetooth on");
                Thread.sleep(100);
            } while (mBLEServiceUtils.mBluetoothAdapter.getState() != mBLEServiceUtils.mBluetoothAdapter.STATE_ON);
        }catch (InterruptedException e){
            e.printStackTrace();
        }

        if (mBLEServiceUtils.mBluetoothAdapter != null) { // && mBluetoothAdapter.isEnabled()){
            if (Build.VERSION.SDK_INT >= 21) {
                mBLEServiceUtils.mBLEScanner = mBLEServiceUtils.mBluetoothAdapter.getBluetoothLeScanner();
                settings = mBLEServiceUtils.setPeriod(ScanSettings.SCAN_MODE_LOW_POWER);
                filters = new ArrayList<ScanFilter>();
            }

            myMacAddress = android.provider.Settings.Secure.getString(this.getContentResolver(), "bluetooth_address");
            Log.d("myMacAddress", myMacAddress);

            // BLEScanner 객체 확인
            if(mBLEServiceUtils.mBLEScanner == null && Build.VERSION.SDK_INT >= 21) {
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

        Log.d(TAG, "debuging");
        // To Wait for connecting
        try{
            do {
                Thread.sleep(100);
            } while (mSocketIO.connected() == false);
        }catch (InterruptedException e){
            e.printStackTrace();
        }
        networkDisconnectedFlag = false;

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
        mSocketIO.requestEssentialData(Constants.CALLBACK_TYPE_BLE_SCAN_SERVICE);
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

                mNotification = GenerateNotification.notification(ServiceContext, "CommutingChecker", "CommutingChecker", "서비스 실행 중");
                startForeground(Constants.NOTIFICATION_ID_TYPE_COMMUTING_STATE, mNotification);

                /*
                Log.d("FilterListSize", String.valueOf(filterlist.size()));
                for(int i = 0; i < filterlist.size(); i++) {
                    Log.d("FilterList", String.valueOf(i) + ": " + filterlist.get(i));
                } */

                // 스캔 시작
                scanLeDevice(true);

/*
                while(scanFlag) {
                    if (lowPowerScanFlag) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {
                        try {
                            Thread.sleep(300);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    mSocketIO.sendQueueData();

                    // 3개의 비콘 정보가 없을 경우 continue
                    if (mBLEDevices.size() != 3) {
                        continue;
                    } else if (mBLEDevices.size() == 0) { // 비콘이 검색되지 않으면 Low power 모드로 스캔
                        restartScan(ScanSettings.SCAN_MODE_LOW_POWER);
                    }

                    // 0.5초 동안 3번 Rssi 체크 후 2번 이상 적합하면 sendCommutingEvent() 메서드 실행
                    if (!commuteCycleFlag && !commuteStatusFlag) {
                        // Low Latency 모드로 스캔하기 위한 Screen on
                        mBLEServiceUtils.wakeScreen(ServiceContext);

                        // low Latency 스캔 모드로 변경
                        restartScan(ScanSettings.SCAN_MODE_LOW_LATENCY);

                        // 출근 알고리즘 실행
                        mBLEServiceUtils.comeToWorkCheckTime(Constants.CALLBACK_TYPE_BLE_SCAN_SERVICE);
                    }

                    //mSocketIO.sendCommutingEvent(new HashMap<String, String>());
                    //scanLeDevice(false);
                    //scanFlag = false;
                }
*/
            } // End of while
        };

        commutingThread = new Thread(r);
        commutingThread.start();
        // return Service.START_STICKY;
        return Service.START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "BLEScanService onBind()");
        return null; // BoundService가 아님을 안드로이드 시스템에 알림
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

    public void restartScan(int scanMode) {
        if(Build.VERSION.SDK_INT >= 21) {
            if (settings.getScanMode() == scanMode) {
                return;
            } else {
                Log.d(TAG, "restartScan(): change scanMode");
                settings = null;
                scanLeDevice(false);
                settings = mBLEServiceUtils.setPeriod(scanMode);
                scanLeDevice(true);
            }
        }
    }

    public void acquireWakeLock(int wakeLockMode) {
        if (mWakeLock != null) {
            if (mWakeLock.isHeld()) {
                mWakeLock.release();
            }
            mWakeLock = null;
        }

        mWakeLock = mPowerManager.newWakeLock(wakeLockMode, TAG);
        mWakeLock.acquire();
    }

    @Override
    public void onDestroy(){
        Log.i(TAG, "Service onDestroy");
        scanFlag = false;
        if (mWakeLock != null) {
            if (mWakeLock.isHeld()) {
                mWakeLock.release();
            }
        }
        scanLeDevice(false); // 스캔 중지
        unregisterReceiver(StopSelfReceiver);
        unregisterReceiver(RequestDataReceiver);
        unregisterReceiver(ShowDataReceiver);
        unregisterReceiver(NetworkChangeReceiver);
        unregisterReceiver(ScreenOffReceiver);
        unregisterReceiver(ScreenOnReceiver);
        stopForeground(false);

        try {
            if (mBLEServiceUtils.timer != null) {
                mBLEServiceUtils.timer.cancel();
                mBLEServiceUtils.timer.purge();
                mBLEServiceUtils.timer = null;
            }
            if (mBLEServiceUtils.leaveWorkTimer != null) {
                mBLEServiceUtils.leaveWorkTimer.cancel();
                mBLEServiceUtils.leaveWorkTimer.purge();
                mBLEServiceUtils.leaveWorkTimer = null;
            }
            leaveWorkTimerHandler.removeCallbacksAndMessages(null);
            timerHandler.removeCallbacksAndMessages(null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(mSocketIO.connected() == true)
            mSocketIO.close();
        GenerateNotification.generateNotification(this, "CommutingChecker", "CommutingChecker", "서비스가 종료되었습니다.", Constants.NOTIFICATION_ID_TYPE_COMMUTING_STATE);
    }

    // api 21 이상
    @SuppressLint("NewApi")
    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            if (!isRunningCommutingThreadFlag) {
                Log.d(TAG, "mScanCallback(): sendEmptyMesage()");
                mSendMessageHandler.sendEmptyMessage(1);
            }

//            isCallbackRunning = true;
            /*
            Log.d("ScanRecord", "Running");
            Log.d("ScanRecord", result.getScanRecord().toString());
*/
            List<String> separatedData = mBLEServiceUtils.separate(result.getScanRecord().getBytes());

            // public DeviceInfo(BluetoothDevice device, String address, String scanRecord, String uuid, String major, String minor, int rssi)
            mBLEServiceUtils.addDeviceInfo(Constants.CALLBACK_TYPE_BLE_SCAN_SERVICE, new DeviceInfo(result.getDevice(), result.getDevice().getAddress(), separatedData.get(0),
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
            mBLEServiceUtils.addDeviceInfo(Constants.CALLBACK_TYPE_BLE_SCAN_SERVICE, new DeviceInfo(device, device.getAddress(), all,
                    uuid, String.valueOf(major_int), String.valueOf(minor_int), rssi));

            mBLEServiceUtils.setCurrentBeacons(device.getAddress(), rssi);
        }
    };
}
