package com.example.taek.commutingchecker;

import android.bluetooth.BluetoothDevice;

/**
 * Created by Taek on 2016-04-15.
 */
public class DeviceInfo {
    public BluetoothDevice Device;
    public String Address;
    public String ScanRecord;
    public String UUID;
    public String Major;
    public String Minor;
    public int Rssi;

    public DeviceInfo(BluetoothDevice device, String address, String scanRecord, String uuid, String major, String minor, int rssi){
        Device = device;
        Address = address;
        ScanRecord = scanRecord;
        UUID = uuid;
        Major = major;
        Minor = minor;
        Rssi = rssi;
    }
}
