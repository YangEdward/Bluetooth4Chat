package com.studyun.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;

import java.util.UUID;

/**
 * Created by Edward on 2015/5/3.
 */
public class ServiceBroadcast {

    /** Intent for broadcast */
    public static final String BLE_NOT_SUPPORTED = "com.studyun.bluetooth.not_supported";
    public static final String BLE_NO_BT_ADAPTER = "com.studyun.bluetooth.no_bt_adapter";
    public static final String BLE_STATUS_ABNORMAL = "com.studyun.bluetooth.status_abnormal";
    /**
     * @see BleService#bleRequestFailed
     */
    public static final String BLE_REQUEST_FAILED = "com.studyun.bluetooth.request_failed";
    /**
     * @see BleService#bleDeviceFound
     */
    public static final String BLE_DEVICE_FOUND = "com.studyun.bluetooth.device_found";
    /**
     * @see BleService#bleGattConnected
     */
    public static final String BLE_GATT_CONNECTED = "com.studyun.bluetooth.gatt_connected";
    /**
     * @see BleService#bleGattDisConnected
     */
    public static final String BLE_GATT_DISCONNECTED = "com.studyun.bluetooth.gatt_disconnected";
    /**
     * @see BleService#bleServiceDiscovered
     */
    public static final String BLE_SERVICE_DISCOVERED = "com.studyun.bluetooth.service_discovered";
    /**
     * @see BleService#bleCharacteristicRead
     */
    public static final String BLE_CHARACTERISTIC_READ = "com.studyun.bluetooth.characteristic_read";
    /**
     * @see BleService#bleCharacteristicNotification
     */
    public static final String BLE_CHARACTERISTIC_NOTIFICATION = "com.studyun.bluetooth.characteristic_notification";
    /**
     * @see BleService#bleCharacteristicIndication
     */
    public static final String BLE_CHARACTERISTIC_INDICATION = "com.studyun.bluetooth.characteristic_indication";
    /**
     * @see BleService#bleCharacteristicWrite
     */
    public static final String BLE_CHARACTERISTIC_WRITE = "com.studyun.bluetooth.characteristic_write";
    /**
     * @see BleService#bleDescriptorRead(String, UUID)
     */
    public static final String BLE_DESCRIPTOR_READ = "com.studyun.bluetooth.descriptor_read";
    /**
     * @see BleService#bleDescriptorRead(String, UUID)
     */
    public static final String BLE_DESCRIPTOR_WRITE = "com.studyun.bluetooth.descriptor_write";
    /**
     * @see BleService#bleCharacteristicChanged
     */
    public static final String BLE_CHARACTERISTIC_CHANGED = "com.studyun.bluetooth.characteristic_changed";

    /**
     * @see BleService#
     */
    public final static String ACTION_GATT_RSSI = "ACTION_GATT_RSSI";

    /**
     * @see BleServerService#descriptorRead
     */
    public static final String BLE_SERVER_DESCRIPTOR_READ = "com.studyun.bluetooth.server_descriptor_read";
    /**
     * @see BleServerService#descriptorWrite(BluetoothDevice, int, BluetoothGattDescriptor, boolean, boolean, int, byte[])
     */
    public static final String BLE_SERVER_DESCRIPTOR_WRITE = "com.studyun.bluetooth.server_descriptor_write";

    /**
     * @see BleServerService#addServiceSuccess(BluetoothGattService)
     */
    public static final String BLE_ADD_SERVICE_SUCCESS = "com.studyun.bluetooth.server_add_service_success";
    /**
     * @see BleServerService#addServiceFailed(BluetoothGattService)
     */
    public static final String BLE_ADD_SERVICE_FAILED = "com.studyun.bluetooth.server_add_service_failed";

    /**
     * @see BleServerService#characteristicRead(BluetoothDevice, int, int, BluetoothGattCharacteristic)
     */
    public static final String BLE_SERVER_CHARACTERISTIC_READ = "com.studyun.bluetooth.server_characteristic_read";
    /**
     * @see BleServerService#characteristicWrite(BluetoothDevice, int, BluetoothGattCharacteristic, boolean, boolean, int, byte[])
     */
    public static final String BLE_SERVER_CHARACTERISTIC_WRITE = "com.studyun.bluetooth.server_characteristic_write";
    /**
     * @see BleServerService#executeWrite(BluetoothDevice, int, boolean)
     */
    public static final String BLE_SERVER_EXECUTE_WRITE = "com.studyun.bluetooth.server_execute_write";


    /** Intent extras */
    public static final String EXTRA_DEVICE = "device";
    public static final String EXTRA_RSSI = "rssi";
    public static final String EXTRA_SCAN_RECORD = "scan_record";
    public static final String EXTRA_SOURCE = "source";
    public static final String EXTRA_ADDR = "address";
    public static final String EXTRA_CONNECTED = "connected";
    public static final String EXTRA_UUID = "uuid";
    public static final String EXTRA_VALUE = "value";
    public static final String EXTRA_REQUEST = "request";
    public static final String EXTRA_REASON = "reason";

    /** Source of device entries in the device list */
    public static final int DEVICE_SOURCE_SCAN = 0;
    public static final int DEVICE_SOURCE_BONDED = 1;
    public static final int DEVICE_SOURCE_CONNECTED = 2;

    /** for server intent extras*/
    public static final String EXTRA_REQUEST_ID = "request_id";
    public static final String EXTRA_OFFSET = "offset";
    public static final String EXTRA_SERVICE = "service";
    public static final String EXTRA_PREPARED_WRITE = "prepared_write";
    public static final String EXTRA_RESPONSE_NEEDED = "response_needed";
    public static final String EXTRA_EXECUTE = "execute";

    public static final UUID DESC_CCC = UUID
            .fromString("00002902-0000-1000-8000-00805f9b34fb");

    public static final UUID SERVICE_UUID = UUID.fromString("f000aa60-0451-4000-b000-000000000000");
    public static final UUID TX_UUID = UUID.fromString("f000aa61-0451-4000-b000-000000000000");
    public static final UUID RX_UUID = UUID.fromString("f000aa63-0451-4000-b000-000000000000");

    public static String BLE_SHIELD_TX = "713d0003-503e-4c75-ba94-3148f18d941e";
    public static String BLE_SHIELD_RX = "713d0002-503e-4c75-ba94-3148f18d941e";
    public static String BLE_SHIELD_SERVICE = "713d0000-503e-4c75-ba94-3148f18d941e";
}
