package com.studyun.bluetooth;


import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

public interface ServerInitStrategy {

    BluetoothGattService getService();
    BluetoothGattCharacteristic getReceiveCharacteristic(String key);
    BluetoothGattCharacteristic getSendCharacteristic(String key);
}
