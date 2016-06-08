package com.example.taek.commutingchecker;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Taek on 2016-06-08.
 */
public class SendEvent {
    public synchronized static void sendEvent(DeviceInfo deviceInfo1, DeviceInfo deviceInfo2, DeviceInfo deviceInfo3, boolean comeToWork){
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
            if(comeToWork) {
                BLEScanService.coolTime = true;
            }else{
                BLEScanService.coolTime = false;
            }
        }
    }
}
