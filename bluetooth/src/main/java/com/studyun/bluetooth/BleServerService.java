package com.studyun.bluetooth;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;

import java.util.UUID;

/**
 * 蓝牙服务器端 服务类,通过蓝牙回调接口发出通知到应用程序.
 */
public class BleServerService extends Service {

    private final IBinder mBinder = new LocalBinder();
    private ServerBle mBle;

    /**
     * {@inheritDoc}
     * 并初始化蓝牙服务端外观类
     */
    @Override
    public void onCreate() {
        super.onCreate();
        mBle = ServerBle.getInstance(this);
    }

    /**
     * {@inheritDoc}
     * @param intent
     * @return
     */
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /**
     * {@inheritDoc}
     */
    public class LocalBinder extends Binder {
        public BleServerService getService() {
            return BleServerService.this;
        }
    }

    /**
     * {@inheritDoc}
     * @param intent
     * @return
     */
    @Override
    public boolean onUnbind(Intent intent) {
        mBle.clearServices();
        mBle.close();
        return super.onUnbind(intent);
    }

    /**
     * 设备不支持蓝牙时发出通知
     */
    void bleNotSupported() {
        Intent intent = new Intent(ServiceBroadcast.BLE_NOT_SUPPORTED);
        sendBroadcast(intent);
    }

    /**
     * 设备无蓝牙适配器时发出通知
     */
    void bleNoBtAdapter() {
        Intent intent = new Intent(ServiceBroadcast.BLE_NO_BT_ADAPTER);
        sendBroadcast(intent);
    }

    /**
     * 指定设备断开连接时发出通知
     * @param device 蓝牙设备
     */
    protected void bleDisconnected(BluetoothDevice device) {
        Intent intent = new Intent(ServiceBroadcast.BLE_GATT_DISCONNECTED);
        intent.putExtra(ServiceBroadcast.EXTRA_DEVICE, device);
        sendBroadcast(intent);
    }

    /**
     * 设备连接成功时发出通知
     * @param device 已连接设备
     */
    void bleConnected(BluetoothDevice device) {
        Intent intent = new Intent(ServiceBroadcast.BLE_GATT_CONNECTED);
        intent.putExtra(ServiceBroadcast.EXTRA_DEVICE, device);
        sendBroadcast(intent);
    }

    /**
     * 服务端descriptor请求阅读时发出通知，需要通过sendResponse相应.
     * @param device 请求设备
     * @param requestId 请求Id
     * @param offset  请求偏移量
     * @param descriptor 请求读取的descriptor
     * @see com.studyun.bluetooth.ServerBle#sendResponse(android.bluetooth.BluetoothDevice, int, int, int, byte[])
     */
    void descriptorRead(BluetoothDevice device, int requestId, int offset,
                   BluetoothGattDescriptor descriptor){
        UUID characteristic = descriptor.getCharacteristic().getUuid();
        Intent intent = new Intent(ServiceBroadcast.BLE_SERVER_DESCRIPTOR_READ);
        intent.putExtra(ServiceBroadcast.EXTRA_DEVICE, device);
        intent.putExtra(ServiceBroadcast.EXTRA_REQUEST_ID, requestId);
        intent.putExtra(ServiceBroadcast.EXTRA_OFFSET, offset);
        intent.putExtra(ServiceBroadcast.EXTRA_UUID,characteristic);
        sendBroadcast(intent);
    }

    /**
     * 服务端descriptor请求阅读时发出通知，需要通过sendResponse相应.
     * @see com.studyun.bluetooth.ServerBle#sendResponse(android.bluetooth.BluetoothDevice, int, int, int, byte[])
     * @param device 请求设备
     * @param requestId 请求Id
     * @param descriptor 请求读取的descriptor
     * @param preparedWrite 是否准备写入
     * @param responseNeeded 是否需要返回响应
     * @param offset 请求偏移量
     * @param value 写入的值
     */
    void descriptorWrite(BluetoothDevice device, int requestId,
        BluetoothGattDescriptor descriptor, boolean preparedWrite, boolean responseNeeded,
                                         int offset, byte[] value) {
        UUID characteristic = descriptor.getCharacteristic().getUuid();
        Intent intent = new Intent(ServiceBroadcast.BLE_SERVER_DESCRIPTOR_WRITE);
        intent.putExtra(ServiceBroadcast.EXTRA_DEVICE, device);
        intent.putExtra(ServiceBroadcast.EXTRA_REQUEST_ID, requestId);
        intent.putExtra(ServiceBroadcast.EXTRA_UUID, characteristic);
        intent.putExtra(ServiceBroadcast.EXTRA_PREPARED_WRITE, preparedWrite);
        intent.putExtra(ServiceBroadcast.EXTRA_RESPONSE_NEEDED, responseNeeded);
        intent.putExtra(ServiceBroadcast.EXTRA_OFFSET, offset);
        intent.putExtra(ServiceBroadcast.EXTRA_VALUE, value);
        sendBroadcast(intent);
    }

    /**
     * 返回与蓝牙服务端相关联的通知Action
     * @return Action组合
     */
    public static IntentFilter getIntentFilter() {

        IntentFilter intentFilter = new IntentFilter();
        /*本地设备是否支持蓝牙*/
        intentFilter.addAction(ServiceBroadcast.BLE_NOT_SUPPORTED);
        intentFilter.addAction(ServiceBroadcast.BLE_NO_BT_ADAPTER);
        /*远程设备连接蓝牙状态*/
        intentFilter.addAction(ServiceBroadcast.BLE_GATT_CONNECTED);
        intentFilter.addAction(ServiceBroadcast.BLE_GATT_DISCONNECTED);
        /*远程设备Descriptor读写请求*/
        intentFilter.addAction(ServiceBroadcast.BLE_SERVER_DESCRIPTOR_READ);
        intentFilter.addAction(ServiceBroadcast.BLE_SERVER_DESCRIPTOR_WRITE);
        /*本地设备添加服务是否成功*/
        intentFilter.addAction(ServiceBroadcast.BLE_ADD_SERVICE_SUCCESS);
        intentFilter.addAction(ServiceBroadcast.BLE_ADD_SERVICE_FAILED);
        /*远程设备Characteristic读写请求*/
        intentFilter.addAction(ServiceBroadcast.BLE_SERVER_CHARACTERISTIC_READ);
        intentFilter.addAction(ServiceBroadcast.BLE_SERVER_CHARACTERISTIC_WRITE);
        /*远程设备请求执行写*/
        intentFilter.addAction(ServiceBroadcast.BLE_SERVER_EXECUTE_WRITE);
        return intentFilter;
    }

    /**
     * 蓝牙服务端添加蓝牙服务（BluetoothGattService）成功后发出通知
     * @param service 被添加的服务
     */
    void addServiceSuccess(BluetoothGattService service) {
        Intent intent = new Intent(ServiceBroadcast.BLE_ADD_SERVICE_SUCCESS);
        intent.putExtra(ServiceBroadcast.EXTRA_SERVICE, service.getUuid());
        sendBroadcast(intent);
    }

    /**
     * 蓝牙服务端添加蓝牙服务（BluetoothGattService）失败后发出通知
     * @param service 被添加的服务
     */
    void addServiceFailed(BluetoothGattService service) {
        Intent intent = new Intent(ServiceBroadcast.BLE_ADD_SERVICE_FAILED);
        intent.putExtra(ServiceBroadcast.EXTRA_SERVICE, service.getUuid());
        sendBroadcast(intent);
    }

    /**
     * 远程设备请求读取characteristic数值
     * @param device 远程设备
     * @param requestId 操作ID
     * @param offset  操作值偏移量
     * @param characteristic  要读取的characteristic
     * @see android.bluetooth.BluetoothGattServerCallback#onCharacteristicReadRequest(android.bluetooth.BluetoothDevice, int, int, android.bluetooth.BluetoothGattCharacteristic)
     */
    void characteristicRead(BluetoothDevice device, int requestId, int offset,
                                   BluetoothGattCharacteristic characteristic) {
        UUID uuid = characteristic.getUuid();
        Intent intent = new Intent(ServiceBroadcast.BLE_SERVER_CHARACTERISTIC_READ);
        intent.putExtra(ServiceBroadcast.EXTRA_DEVICE, device);
        intent.putExtra(ServiceBroadcast.EXTRA_REQUEST_ID, requestId);
        intent.putExtra(ServiceBroadcast.EXTRA_UUID, uuid);
        intent.putExtra(ServiceBroadcast.EXTRA_OFFSET, offset);
        sendBroadcast(intent);
    }

    /**
     * 远程设备请求写characteristic数值
     * @param device 远程客户端蓝牙设备
     * @param requestId 请求操作Id
     * @param characteristic 准备写的characteristic
     * @param preparedWrite 写操作是否放入队列准备执行
     * @param responseNeeded  远程设备是否需要反馈响应
     * @param offset value偏移量
     * @param value  写的内容
     */
    void characteristicWrite(BluetoothDevice device, int requestId,
                                    BluetoothGattCharacteristic characteristic,
                                    boolean preparedWrite, boolean responseNeeded,
                                    int offset, byte[] value) {
        UUID uuid = characteristic.getUuid();
        Intent intent = new Intent(ServiceBroadcast.BLE_SERVER_CHARACTERISTIC_WRITE);
        intent.putExtra(ServiceBroadcast.EXTRA_DEVICE, device);
        intent.putExtra(ServiceBroadcast.EXTRA_REQUEST_ID, requestId);
        intent.putExtra(ServiceBroadcast.EXTRA_UUID, uuid);
        intent.putExtra(ServiceBroadcast.EXTRA_PREPARED_WRITE, preparedWrite);
        intent.putExtra(ServiceBroadcast.EXTRA_RESPONSE_NEEDED, responseNeeded);
        intent.putExtra(ServiceBroadcast.EXTRA_OFFSET, offset);
        intent.putExtra(ServiceBroadcast.EXTRA_VALUE, value);
        sendBroadcast(intent);
    }

    /**
     * 远程设备请求写时发出通知
     * @param device 远程设备
     * @param requestId 请求Id
     * @param execute 操作是执行还是取消
     * @see android.bluetooth.BluetoothGattServerCallback#onExecuteWrite(BluetoothDevice, int, boolean)
     */
    void executeWrite(BluetoothDevice device, int requestId, boolean execute) {
        Intent intent = new Intent(ServiceBroadcast.BLE_SERVER_EXECUTE_WRITE);
        intent.putExtra(ServiceBroadcast.EXTRA_DEVICE, device);
        intent.putExtra(ServiceBroadcast.EXTRA_REQUEST_ID, requestId);
        intent.putExtra(ServiceBroadcast.EXTRA_EXECUTE, execute);
        sendBroadcast(intent);
    }

}
