package com.example.taek.commutingchecker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Taek on 2016-04-15.
 */
public class CheckTime {
    public static void checkTime(){
        Runnable r = new Runnable() {
            @Override
            public void run() {
                List<DeviceInfo> mBLEDevice;
                DeviceInfo mDeviceInfo1 = null, mDeviceInfo2 = null, mDeviceInfo3 = null;
                int coordinateX = 0, coordinateY = 0, coordinateZ = 0, count = 0;
                mBLEDevice = BLEScanService.mBLEDevices;

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
                        }
                    }
                }

                for(int i = 0; i < 3; i++){
                    mBLEDevice = BLEScanService.mBLEDevices;
                    for(DeviceInfo deviceInfo : mBLEDevice){ // update deviceInfo1, 2, 3
                        if(deviceInfo.Address.equals(mDeviceInfo1.Address))
                            mDeviceInfo1 = deviceInfo;
                        else if(deviceInfo.Address.equals(mDeviceInfo2.Address))
                            mDeviceInfo2 = deviceInfo;
                        else if(deviceInfo.Address.equals(mDeviceInfo3.Address))
                            mDeviceInfo3 = deviceInfo;
                    }
                    int count_for = 0;

                    if(mDeviceInfo1.Rssi > (coordinateX - 5) && mDeviceInfo1.Rssi < (coordinateX + 5)){
                        count_for++;
                    }
                    if(mDeviceInfo2.Rssi > (coordinateY - 5) && mDeviceInfo2.Rssi < (coordinateY + 5)){
                        count_for++;
                    }
                    if(mDeviceInfo3.Rssi > (coordinateZ - 5) && mDeviceInfo3.Rssi < (coordinateZ + 5)){
                        count_for++;
                    }

                    if(count_for >= 2)
                        count++;

                    if(count >= 2)
                        break;

                    try {
                        Thread.sleep(300);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                if(count >= 2){
                    SendEvent.sendEvent(mDeviceInfo1, mDeviceInfo2, mDeviceInfo3, true);
                    //BLEScanService.checkCallbackThread = new CheckCallback(mDeviceInfo1, mDeviceInfo2, mDeviceInfo3);
                }
            }
        };

        Thread thread = new Thread(r);
        thread.start();
    }
}
