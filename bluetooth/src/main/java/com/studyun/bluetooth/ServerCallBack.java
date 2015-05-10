package com.studyun.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;


class ServerCallBack extends BluetoothGattServerCallback{

    private BleServerService serverService;

    public ServerCallBack(BleServerService service) {
        super();
        this.serverService = service;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
        super.onConnectionStateChange(device, status, newState);
        if(newState == BluetoothProfile.STATE_DISCONNECTED){
            serverService.bleDisconnected(device);
        }else if(newState == BluetoothProfile.STATE_CONNECTED){
            serverService.bleConnected(device);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onServiceAdded(int status, BluetoothGattService service) {
        super.onServiceAdded(status, service);
        if(status == BluetoothGatt.GATT_SUCCESS){
            serverService.addServiceSuccess(service);
        }else if(status == BluetoothGatt.GATT_FAILURE){
            serverService.addServiceFailed(service);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
        super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
        serverService.characteristicRead(device, requestId, offset, characteristic);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
        super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value);
        serverService.characteristicWrite(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDescriptorReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattDescriptor descriptor) {
        super.onDescriptorReadRequest(device, requestId, offset, descriptor);
        serverService.descriptorRead(device, requestId, offset, descriptor);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDescriptorWriteRequest(BluetoothDevice device, int requestId, BluetoothGattDescriptor descriptor, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
        super.onDescriptorWriteRequest(device, requestId, descriptor, preparedWrite, responseNeeded, offset, value);
        serverService.descriptorWrite(device, requestId, descriptor, preparedWrite, responseNeeded, offset, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onExecuteWrite(BluetoothDevice device, int requestId, boolean execute) {
        super.onExecuteWrite(device, requestId, execute);
        serverService.executeWrite(device, requestId, execute);
    }

}
