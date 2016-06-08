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
                    int count_for = 0;

                    if(mDeviceInfo1.Rssi > (coordinateX - 3) && mDeviceInfo1.Rssi < (coordinateX + 3)){
                        count_for++;
                    }
                    if(mDeviceInfo2.Rssi > (coordinateY - 3) && mDeviceInfo2.Rssi < (coordinateY + 3)){
                        count_for++;
                    }
                    if(mDeviceInfo3.Rssi > (coordinateZ - 3) && mDeviceInfo3.Rssi < (coordinateZ + 3)){
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
                    SendEvent.sendEvent(mDeviceInfo1, mDeviceInfo2, mDeviceInfo3);
                    /*
                    if(!BLEScanService.coolTime) {
                        sendEvent(mDeviceInfo1, mDeviceInfo2, mDeviceInfo3);

                        Map<String, String> data = new HashMap<String, String>();
                        data.put("BeaconDeviceAddress1", mDeviceInfo1.Address);
                        data.put("BeaconDeviceAddress2", mDeviceInfo2.Address);
                        data.put("BeaconDeviceAddress3", mDeviceInfo3.Address);
                        data.put("BeaconData1", mDeviceInfo1.ScanRecord);
                        data.put("BeaconData2", mDeviceInfo2.ScanRecord);
                        data.put("BeaconData3", mDeviceInfo3.ScanRecord);
                        data.put("SmartphoneAddress", BLEScanService.myMacAddress);
                        data.put("DateTime", CurrentTime.currentTime());

                        BLEScanService.mSocketIO.sendEvent(data);
                        BLEScanService.coolTime = true;

                        try {
                            Thread.sleep(3000);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        BLEScanService.coolTime = false;
                    } */
                }
            }
        };

        Thread thread = new Thread(r);
        thread.start();
    }

    private synchronized static void sendEvent(DeviceInfo deviceInfo1, DeviceInfo deviceInfo2, DeviceInfo deviceInfo3){
        if(!BLEScanService.coolTime) {
            Map<String, String> data = new HashMap<String, String>();
            data.put("BeaconDeviceAddress1", deviceInfo1.Address);
            data.put("BeaconDeviceAddress2", deviceInfo2.Address);
            data.put("BeaconDeviceAddress3", deviceInfo3.Address);
            data.put("BeaconData1", deviceInfo1.ScanRecord);
            data.put("BeaconData2", deviceInfo2.ScanRecord);
            data.put("BeaconData3", deviceInfo3.ScanRecord);
            data.put("SmartphoneAddress", BLEScanService.myMacAddress);
            data.put("DateTime", CurrentTime.currentTime());

            BLEScanService.mSocketIO.sendEvent(data);
            BLEScanService.coolTime = true;

            try {
                Thread.sleep(3000);
            } catch (Exception e) {
                e.printStackTrace();
            }

            BLEScanService.coolTime = false;
        }
    }
}
