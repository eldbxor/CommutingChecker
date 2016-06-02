package com.example.taek.commutingchecker;

import java.util.List;
import java.util.Map;

/**
 * Created by Taek on 2016-04-15.
 */
public class CheckTime {
    private static int count = 0;
    public static void checkTime(){
        Runnable r = new Runnable() {
            @Override
            public void run() {
                List<DeviceInfo> mBLEDevice;
                count = 0;
                for(int i = 0; i < 3; i++){
                    mBLEDevice = BLEScanService.mBLEDevices;
                    if(mBLEDevice.size() != 3)
                        return;

                    // Rssi 제한값 검사
                    for(Map<String, String> map : BLEScanService.EssentialDataArray){
                        DeviceInfo mDeviceInfo1 = null, mDeviceInfo2 = null, mDeviceInfo3 = null;
                        for(int j = 0; j < 3; j++){
                            if(map.get("beacon_address1") == mBLEDevice.get(j).Address)
                                mDeviceInfo1 = mBLEDevice.get(j);
                            else if(map.get("beacon_address2") == mBLEDevice.get(j).Address)
                                mDeviceInfo2 = mBLEDevice.get(j);
                            else if(map.get("beacon_address3") == mBLEDevice.get(j).Address)
                                mDeviceInfo3 = mBLEDevice.get(j);
                        }

                        if(mDeviceInfo1.Rssi > Integer.parseInt(map.get("coordinateX")) - 10
                                && mDeviceInfo1.Rssi < Integer.parseInt(map.get("coordinateX")) + 10
                                && mDeviceInfo2.Rssi > Integer.parseInt(map.get("coordinateY")) - 10
                                && mDeviceInfo2.Rssi < Integer.parseInt(map.get("coordinateY")) + 10
                                && mDeviceInfo3.Rssi > Integer.parseInt(map.get("coordinateZ")) - 10
                                && mDeviceInfo3.Rssi < Integer.parseInt(map.get("coordinateZ")) + 10){
                            count++;
                        }
                    }
                }

                if(count >= 2){
                    BLEScanService.sendEvent();
                }

                try {
                    Thread.sleep(500);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        Thread thread = new Thread(r);
        thread.start();
    }
}
