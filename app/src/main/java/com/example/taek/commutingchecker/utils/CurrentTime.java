package com.example.taek.commutingchecker.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Taek on 2016-05-17.
 */
public class CurrentTime {
    public static String currentTime(){
        Long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        String strNow = sdfNow.format(date);

        return strNow;
    }
}
