package com.studyun.bluetooth;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothProfile;
import android.util.Log;

import java.util.UUID;


class ClientCallBack extends BluetoothGattCallback {

    private final static String TAG = ClientCallBack.class.getSimpleName();
    private BleClientService mService;

    public ClientCallBack(BleClientService mService) {
        this.mService = mService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                        int newState) {
        String address = gatt.getDevice().getAddress();
        Log.d(TAG, "onConnectionStateChange " + address + " status "
                + status + " newState " + newState);
        if (status != BluetoothGatt.GATT_SUCCESS) {
            mService.disconnect(address);
            mService.bleGattDisConnected(address);
            return;
        }

        if (newState == BluetoothProfile.STATE_CONNECTED) {
            mService.bleGattConnected(gatt.getDevice());
            mService.addBleRequest(new BleRequest(
                    BleRequest.RequestType.DISCOVER_SERVICE, address));
        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            mService.bleGattDisConnected(address);
            mService.disconnect(address);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        String address = gatt.getDevice().getAddress();
        Log.d(TAG, "onServicesDiscovered " + address + " status " + status);
        if (status != BluetoothGatt.GATT_SUCCESS) {
            mService.requestProcessed(address,
                    BleRequest.RequestType.DISCOVER_SERVICE, false);
            return;
        }
        mService.bleServiceDiscovered(address);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCharacteristicRead(BluetoothGatt gatt,
                                     BluetoothGattCharacteristic characteristic, int status) {
        String address = gatt.getDevice().getAddress();
        Log.d(TAG, "onCharacteristicRead " + address + " status " + status);
        if (status != BluetoothGatt.GATT_SUCCESS) {
            mService.requestProcessed(address,
                    BleRequest.RequestType.READ_CHARACTERISTIC, false);
            return;
        }
        // Log.d(TAG, "data " + characteristic.getStringValue(0));
        mService.bleCharacteristicRead(address,characteristic.getUuid(),characteristic.getValue());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt,
                                      BluetoothGattCharacteristic characteristic, int status) {
        String address = gatt.getDevice().getAddress();
        Log.d(TAG, "onCharacteristicWrite " + address + " status " + status);
        if (status != BluetoothGatt.GATT_SUCCESS) {
            mService.requestProcessed(address,
                    BleRequest.RequestType.WRITE_CHARACTERISTIC, false);
            return;
        }
        mService.bleCharacteristicWrite(address,characteristic.getUuid());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt,
                                        BluetoothGattCharacteristic characteristic) {
        String address = gatt.getDevice().getAddress();
        Log.d(TAG, "onCharacteristicChanged " + address);
        mService.bleCharacteristicChanged(address, characteristic.getUuid(),
                characteristic.getValue());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor,
                                 int status) {
        String address = gatt.getDevice().getAddress();
        Log.d(TAG, "onDescriptorRead " + address + " status " + status);
        if (status != BluetoothGatt.GATT_SUCCESS) {
            mService.requestProcessed(address,
                    BleRequest.RequestType.READ_DESCRIPTOR, false);
            return;
        }
        mService.bleDescriptorRead(address,descriptor.getUuid());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDescriptorWrite(BluetoothGatt gatt,
                                  BluetoothGattDescriptor descriptor, int status) {
        String address = gatt.getDevice().getAddress();
        Log.d(TAG, "onDescriptorWrite " + address + " status " + status);
        BleRequest request = mService.getCurrentRequest();
        UUID uuid = descriptor.getCharacteristic().getUuid();
        if (request.type == BleRequest.RequestType.CHARACTERISTIC_NOTIFICATION
                || request.type == BleRequest.RequestType.CHARACTERISTIC_INDICATION
                || request.type == BleRequest.RequestType.CHARACTERISTIC_STOP_NOTIFICATION) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                mService.requestProcessed(address,
                        BleRequest.RequestType.CHARACTERISTIC_NOTIFICATION, false);
                return;
            }
            if (request.type == BleRequest.RequestType.CHARACTERISTIC_NOTIFICATION) {
                mService.bleCharacteristicNotification(address,uuid, true);
            } else if (request.type == BleRequest.RequestType.CHARACTERISTIC_INDICATION) {
                mService.bleCharacteristicIndication(address, uuid);
            } else {
                mService.bleCharacteristicNotification(address, uuid, false);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
        Log.w(TAG, "onReliableWriteCompleted received: " + status);
        String address = gatt.getDevice().getAddress();
        if (status != BluetoothGatt.GATT_SUCCESS) {
            mService.requestProcessed(address,
                    BleRequest.RequestType.RELIABLE_WRITE_COMPLETED, false);
            return;
        }
        mService.bleReliableWriteCompleted(address);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
        Log.w(TAG, "onReadRemoteRssi received: " + status);
        if (status != BluetoothGatt.GATT_SUCCESS) {
            mService.requestProcessed(gatt.getDevice().getAddress(),
                    BleRequest.RequestType.READ_RSSI, false);
            return;
        }
        mService.bleReadRemoteRssi(rssi);
    }
}
