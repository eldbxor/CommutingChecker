package com.example.taek.commutingchecker;

/**
 * Created by Taek on 2016-04-15.
 */
public class AddDeviceInfo {
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
}
