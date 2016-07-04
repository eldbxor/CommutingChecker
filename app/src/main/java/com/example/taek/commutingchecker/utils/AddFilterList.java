package com.example.taek.commutingchecker.utils;

import com.example.taek.commutingchecker.services.BLEScanService;

/**
 * Created by Taek on 2016-06-08.
 */
public class AddFilterList {
    public static void addFilterList(String beaconAddress){
        boolean isExisted = false;
        int index = 0;

        if(beaconAddress.equals(""))
            return;

        for(String mac : BLEScanService.filterlist){
            if(mac.equals(beaconAddress)){
                isExisted = true;
                index = BLEScanService.filterlist.indexOf(mac);
                break;
            }
        }

        if(isExisted == true){
            BLEScanService.filterlist.add(index, beaconAddress);
            BLEScanService.filterlist.remove(index + 1);
        }else{
            BLEScanService.filterlist.add(beaconAddress);
        }
    }
}
