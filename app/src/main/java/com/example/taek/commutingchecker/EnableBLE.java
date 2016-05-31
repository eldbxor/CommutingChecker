package com.example.taek.commutingchecker;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;

/**
 * Created by Taek on 2016-04-15.
 */
public class EnableBLE {
    public BluetoothManager mBluetoothManager;
    public BluetoothAdapter mBluetoothAdapter;

    public EnableBLE(Object obj){
        mBluetoothManager = (BluetoothManager)obj;
        mBluetoothAdapter = mBluetoothManager.getAdapter();
    }

    public BluetoothAdapter enable(){
        if(!mBluetoothAdapter.isEnabled()){
            mBluetoothAdapter.enable();
        }
        return mBluetoothAdapter;
    }
}
