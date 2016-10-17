package com.example.taek.commutingchecker.utils;

/**
 * Created by Taek on 2016-04-15.
 */
public class Constants {
    public static final String SERVER_URL = "http://14.63.219.156:2070"; // Cloud server
//    public static final String SERVER_URL = "http://192.168.0.25:2070"; // Awesometic: for developing using my desktop
    public static final String AES256_KEY_SALT = "Awesometic";

    // Handler를 생성하는 컴포넌트
    public static final int HANDLER_TYPE_ACTIVITY = 1;
    public static final int HANDLER_TYPE_SERVICE = 2;

    // BLEScanService와 CalibrationService, MainActivity를 구분(메서드 호출 시)
    public static final int CALLBACK_TYPE_MAIN_ACTIVITY = 1;
    public static final int CALLBACK_TYPE_BLE_SCAN_SERVICE = 2;
    public static final int CALLBACK_TYPE_CALIBRATION_SERVICE = 3;

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

    // BroadCastReceiver Type
    public static final int BROADCAST_RECEIVER_TYPE_REQEUST_DATA = 1;
    public static final int BROADCAST_RECEIVER_TYPE_SHOW_DATA = 2;
    public static final int BROADCAST_RECEIVER_TYPE_NETWORK_CHANGE = 3;
    public static final int BROADCAST_RECEIVER_TYPE_STOP_SERVICE = 4;
    public static final int BROADCAST_RECEIVER_TYPE_COME_TO_WORK_STATE = 5;
    public static final int BROADCAST_RECEIVER_TYPE_LEAVE_WORK_STATE = 6;
    public static final int BROADCAST_RECEIVER_TYPE_STAND_BY_COME_TO_WORK_STATE = 7;
    public static final int BROADCAST_RECEIVER_TYPE_SCREEN_OFF = 8;

    // NetworkStatus Type
    public static final int NETWORK_TYPE_WIFI = 1;
    public static final int NETWORK_TYPE_MOBILE = 2;
    public static final int NETWORK_TYPE_NOT_CONNECTED = 0;

    // Notification ID
    public static final int NOTIFICATION_ID = 1;
}
