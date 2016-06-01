package com.example.taek.commutingchecker;

/**
 * Created by Taek on 2016-04-15.
 */
public class CheckTime {
    private static int count = 0;
    public static Runnable checkTime(final DeviceInfo deviceInfo1, final DeviceInfo deviceInfo2, final DeviceInfo deviceInfo3){
        Runnable r = new Runnable() {
            @Override
            public void run() {
                count = 0;

                for(int i = 0; i < 3; i++){
                    for(DeviceInfo deviceInfo : BLEScanService.mBLEDevices){
                        if(deviceInfo1.Address.equals(deviceInfo.Address)){
                            if(deviceInfo.Rssi > Integer.valueOf(BLEScanService.EssentialDataArray.get(0).get("Rssi1")) - 10 &
                                    deviceInfo.Rssi < Integer.valueOf(BLEScanService.EssentialDataArray.get(0).get("Rssi1")) + 10)
                                count++;
                        }else if(deviceInfo2.Address.equals(deviceInfo.Address)){
                            if(deviceInfo.Rssi > Integer.valueOf(BLEScanService.EssentialDataArray.get(0).get("Rssi2")) - 10 &
                                    deviceInfo.Rssi < Integer.valueOf(BLEScanService.EssentialDataArray.get(0).get("Rssi2")) + 10)
                                count++;
                        }else if(deviceInfo3.Address.equals(deviceInfo.Address)){
                            if(deviceInfo.Rssi > Integer.valueOf(BLEScanService.EssentialDataArray.get(0).get("Rssi3")) - 10 &
                                    deviceInfo.Rssi < Integer.valueOf(BLEScanService.EssentialDataArray.get(0).get("Rssi3")) + 10)
                                count++;
                        }
                    }

                    if(count >= 2){
                        BLEScanService.sendEvent();
                    }
                }

                try {
                    Thread.sleep(500);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        return r;
    }
}
