package com.example.taek.commutingchecker.utils;

import com.example.taek.commutingchecker.services.BLEScanService;

import java.util.Map;

/**
 * Created by Taek on 2016-06-05.
 */
public class AddEssentialData {
    public static void addEssentialData(Map<String, String> essentialData){
        boolean isExisted = false;
        int index = 0;

        for(Map<String, String> map : BLEScanService.EssentialDataArray){
            if(essentialData.get("id_workplace").equals(map.get("id_workplace"))){
                isExisted = true;
                index = BLEScanService.EssentialDataArray.indexOf(map);
                break;
            }
        }

        if(isExisted == true){
            BLEScanService.EssentialDataArray.add(index, essentialData);
            BLEScanService.EssentialDataArray.remove(index + 1);
        }else{
            BLEScanService.EssentialDataArray.add(essentialData);
        }
    }
}
