package com.example.taek.commutingchecker;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
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
import android.nfc.Tag;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.socket.emitter.Emitter;

public class BLEScanService extends Service {
    BluetoothAdapter mBluetoothAdapter; // Bluetooth 어뎁터
    BluetoothLeScanner mBLEScanner; // BLE 스캐너(api 21 이상)
    public static SocketIO mSocketIO; // Jason을 이용한 서버와의 통신 클래스
    public static List<DeviceInfo> mBLEDevices; // 비콘 디바이스 정보를 갖는 ArrayList
    private ScanSettings settings; // BLE 스캔 옵션 세팅
    private List<ScanFilter> filters; // BLE 스캔 필터
    public static String myMacAddress; // 스마트폰 블루투스 Mac 주소
    private static String sendTime; // 서버에 데이터를 보낸 시간
    String TAG = MainActivity.ServiceTAG;
    Thread t; // 출퇴근 등록 쓰레드
    boolean flag; // 출퇴근등록 쓰레드 실행 플래그
    // Notification ID
    public static final int NOTIFICATION_ID = 1;
    BroadcastReceiver mReceiver;
    private List<String> filterlist;
    public static List<Map<String, String>> EssentialDataArray;
    public static Context ServiceContext;

    public BLEScanService() {
    }

    @Override
    public void onCreate(){
        Log.i(TAG, "Service onCreate");
        flag = true;
        ServiceContext = this;
        // 임의의 비콘 Mac 주소
        filterlist = new ArrayList<String>();
        filterlist.add("00:1A:7D:DA:71:07");
        filterlist.add("00:1A:7D:DA:71:03");
        filterlist.add("B8:27:EB:E1:47:EB");

        // 비콘 디바이스 정보
        mBLEDevices = new ArrayList<DeviceInfo>();

        // 비콘의 Mac 주소와 제한 값을 저장할 Map
        EssentialDataArray = new ArrayList<Map<String, String>>();

        // 소켓 생성
        mSocketIO = new SocketIO();

        // 리시버 등록
        IntentFilter pkgFilter = new IntentFilter();
        pkgFilter.addAction("android.intent.action.STOP_SERVICE");
        pkgFilter.addDataScheme("sample");

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                stopSelf();
            }
        };
        registerReceiver(mReceiver, pkgFilter);

        EnableBLE mEnableBLE = new EnableBLE(getSystemService(this.BLUETOOTH_SERVICE)); // BLE 활성화 클래스 생성
        mBluetoothAdapter = mEnableBLE.enable(); // BLE 활성화

        if(mBluetoothAdapter != null){ // && mBluetoothAdapter.isEnabled()){
            if(Build.VERSION.SDK_INT >= 21){
                mBLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
                settings = new ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                        .build();
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
            if(mBLEScanner == null){
                Toast.makeText(this, "Can not find BLE Scanner", Toast.LENGTH_SHORT).show();
                return;
            }

        }

        // 소켓 연결
        mSocketIO.connect();

        // 서버에 Rssi 제한 값 요청 후 데이터 받기
        mSocketIO.requestEssentialData();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        // 스캔 시작
        scanLeDevice(true);

        Runnable r = new Runnable() {
            @Override
            public void run() {
                // Waiting for connecting
                try{
                    Thread.sleep(5000);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
                while(flag){
                    try{
                        Thread.sleep(500);
                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }

                    DeviceInfo deviceInfo1 = null;
                    DeviceInfo deviceInfo2 = null;
                    DeviceInfo deviceInfo3 = null;

                    for(DeviceInfo deviceInfo : mBLEDevices){
                        if(Integer.valueOf(deviceInfo.Major) == 1)
                            deviceInfo1 = deviceInfo;
                        else if(Integer.valueOf(deviceInfo.Major) == 2)
                            deviceInfo2 = deviceInfo;
                        else if(Integer.valueOf(deviceInfo.Major) == 3)
                            deviceInfo3 = deviceInfo;
                    }

                    // 3개의 비콘 정보가 없을 경우 continue
                    /*
                    if(deviceInfo1 == null || deviceInfo2 == null || deviceInfo3 == null)
                        continue; */
                    if(mBLEDevices.size() != 3)
                        continue;

                    // 0.5초 동안 3번 Rssi 체크 후 2번 이상 적합하면 sendEvent() 메서드 실행
                    CheckTime.checkTime();

                    //mSocketIO.sendEvent(new HashMap<String, String>());
                    //scanLeDevice(false);
                    //flag = false;
                }
            }
        };

        t = new Thread(r);
        t.start();
        return Service.START_STICKY;
    }

    public static void sendEvent(){
        sendTime = CurrentTime.currentTime();

        Map<String, String> data = new HashMap<String, String>();
        data.put("BeaconDeviceAddress1", mBLEDevices.get(0).Address);
        data.put("BeaconDeviceAddress2", mBLEDevices.get(1).Address);
        data.put("BeaconDeviceAddress3", mBLEDevices.get(2).Address);
        data.put("BeaconData1", mBLEDevices.get(0).ScanRecord);
        data.put("BeaconData2", mBLEDevices.get(1).ScanRecord);
        data.put("BeaconData3", mBLEDevices.get(2).ScanRecord);
        data.put("SmartphoneAddress", myMacAddress);
        data.put("Datetime", sendTime);
        mSocketIO.sendEvent(data);
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
        flag = false;
        scanLeDevice(false); // 스캔 중지
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
            if (i == 28) {
                all += String.format("%02x", b);
            } else {
                all += String.format("%02x ", b);
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
            // 비콘 Mac 주소 필터링
            boolean filtering = false;
            for(String deviceMacAddres : filterlist){
                if(device.getAddress().equals(deviceMacAddres)){
                    filtering = true;
                }
            }
            if(!filtering) return;

            List<String> separatedData = separate(scanRecord);

            AddDeviceInfo.addDeviceInfo(new DeviceInfo(device, device.getAddress(), separatedData.get(0),
                    separatedData.get(1), separatedData.get(2), separatedData.get(3), rssi));
        }
    };
}
