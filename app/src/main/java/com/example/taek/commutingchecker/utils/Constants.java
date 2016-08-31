package com.example.taek.commutingchecker.utils;

/**
 * Created by Taek on 2016-04-15.
 */
public class Constants {
    public static final String SERVER_URL = "http://14.63.219.156:2070"; // Cloud server
//    public static final String SERVER_URL = "http://192.168.0.25:2070"; // Awesometic: for developing using my desktop
    public static final String AES256_KEY_SALT = "Awesometic";

    /**
     * Trigger a callback for every Bluetooth advertisement found that matches the filter criteria.
     * If no filter is active, all advertisement packets are reported.
     */
    public static final int CALLBACK_TYPE_ALL_MATCHES = 1;

    /**
     * A result callback is only triggered for the first advertisement packet received that matches
     * the filter criteria.
     */
    public static final int CALLBACK_TYPE_FIRST_MATCH = 2;

    /**
     * Receive a callback when advertisements are no longer received from a device that has been
     * previously reported by a first match callback.
     */
    public static final int CALLBACK_TYPE_MATCH_LOST = 4;

    // 서비스에서 받는 Message
    public static final int HANDLE_MESSAGE_TYPE_CALIBRATION = 1;
    public static final int HANDLE_MESSAGE_TYPE_CALIBRATION_RESET = 2;
    public static final int HANDLE_MESSAGE_TYPE_CEHCK_THRESHOLD = 3;
    public static final int HANDLE_MESSAGE_TYPE_COMPLETE_CALIBRATION = 4;
    public static final int HANDLE_MESSAGE_TYPE_SEEKBAR_VALUE_CHANGED = 5;

    // 액티비티에서 받는 Message
    public static final int HANDLE_MESSAGE_TYPE_SETTEXT_NEXT = 1;
    public static final int HANDLE_MESSAGE_TYPE_ADD_TIMESECOND = 2;
    public static final int HANDLE_MESSAGE_TYPE_SETTEXT_ATTENDANCE_ZONE = 3;
    public static final int HANDLE_MESSAGE_TYPE_SETTEXT_NOT_ATTENDANCE_ZONE = 4;
    public static final int HANDLE_MESSAGE_TYPE_REGISTER_CALIBRATION = 5;
}
