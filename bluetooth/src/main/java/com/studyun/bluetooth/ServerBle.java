package com.studyun.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.pm.PackageManager;

import java.util.List;
import java.util.UUID;

public class ServerBle {

    /*default services uuid*/
    public static final UUID SERVICE_UUID = UUID.fromString("f000aa60-0451-4000-b000-000000000000");
    public static final UUID RECEIVE_UUID = UUID.fromString("f000aa61-0451-4000-b000-000000000000");
    public static final UUID SEND_UUID = UUID.fromString("f000aa63-0451-4000-b000-000000000000");
    public static final UUID DESC_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    private BluetoothGattCharacteristic receive;
    /**
     * singleton instance
     */
    private static ServerBle serverBle;

    private BluetoothGattServer server;

    private BleServerService serverService;

    private ServerInitStrategy initStrategy;

    public static ServerBle getInstance(BleServerService service){

        if(serverBle == null){
            synchronized(ServerBle.class) {
                if(serverBle == null){
                    serverBle = new ServerBle(service);
                }
            }
        }
        return serverBle;
    }

    private ServerBle(BleServerService service) {

        serverService = service;
        if (!serverService.getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_BLUETOOTH_LE)) {
            serverService.bleNotSupported();
            return;
        }

        final BluetoothManager bluetoothManager = (BluetoothManager) serverService
                .getSystemService(Context.BLUETOOTH_SERVICE);

        BluetoothAdapter mBtAdapter = bluetoothManager.getAdapter();
        if (mBtAdapter == null) {
            serverService.bleNoBtAdapter();
        }
        if (server == null){
            BluetoothManager manager = (BluetoothManager) serverService
                    .getSystemService(Context.BLUETOOTH_SERVICE);
            if (serverService == null){
                serverService = new BleServerService();
            }
            BluetoothGattServerCallback serverCallBack = new ServerCallBack(serverService);
            server = manager.openGattServer(serverService.getApplicationContext(),
                    serverCallBack);
        }
        /* 添加默认服务 */
        defaultAddServices();
    }

    /**
     * @see BluetoothGattServer#sendResponse(BluetoothDevice, int, int, int, byte[])
     */
    public void sendResponse(BluetoothDevice device, int requestId, int status, int offset, byte[] value){
        server.sendResponse(device,requestId,status,offset,value);
    }

    /**
     * 添加蓝牙服务,添加结果通过回调确定
     * @see android.bluetooth.BluetoothGattServerCallback#onServiceAdded(int, android.bluetooth.BluetoothGattService)
     */
    public boolean addService (BluetoothGattService service){
        return server.addService(service);
    }

    /**
     * 断开指定蓝牙设备的连接
     */
    public void cancelConnection (BluetoothDevice device){
        server.cancelConnection(device);
    }

    /**
     * 关闭蓝牙服务
     * @see android.bluetooth.BluetoothGattServer#close()
     */
    public void close (){
        server.close();
    }

    /**
     * 清除蓝牙服务
     * @see android.bluetooth.BluetoothGattServer#clearServices()
     */
    public void clearServices (){
        server.clearServices();
    }

    /**
     * 连接蓝牙设备,在连接失败后是否自动连接
     * @param device 指定连接设备
     * @param autoConnect 失败后,是否继续自动连接
     * @return true 连接成功
     * @see android.bluetooth.BluetoothGattServer#connect(android.bluetooth.BluetoothDevice, boolean)
     */
    public boolean connect (BluetoothDevice device, boolean autoConnect){
        return server.connect(device,autoConnect);
    }

    /**
     * 获取当前蓝牙连接设备
     * @return 设备列表
     */
    public List<BluetoothDevice> getConnectedDevices (){
        BluetoothManager manager = (BluetoothManager)serverService.
                getSystemService(Context.BLUETOOTH_SERVICE);
        return manager.getConnectedDevices(BluetoothProfile.GATT);
    }

    /**
     * 获取设备连接状态
     * @param device 设备
     * @return 连接状态
     */
    public int getConnectionState (BluetoothDevice device){
        BluetoothManager manager = (BluetoothManager)serverService.
                getSystemService(Context.BLUETOOTH_SERVICE);
        return manager.getConnectionState(device, BluetoothProfile.GATT);

    }

    /**
     * 获取指定状态的连接设备
     * @param states 状态数组
     * @return 设备列表
     */
    public List<BluetoothDevice> getDevicesMatchingConnectionStates (int[] states){
        BluetoothManager manager = (BluetoothManager)serverService.
                getSystemService(Context.BLUETOOTH_SERVICE);
        return manager.getDevicesMatchingConnectionStates(BluetoothProfile.GATT, states);
    }

    /**
     * 获取当前蓝牙服务
     * @return {@link android.bluetooth.BluetoothGattServer}
     */
    public BluetoothGattServer getServer() {
        return server;
    }

    /**
     * 设置蓝牙默认服务
     */
    private void defaultAddServices(){

        BluetoothGattService service = new BluetoothGattService(SERVICE_UUID,
                BluetoothGattService.SERVICE_TYPE_PRIMARY);

        receive = new BluetoothGattCharacteristic(RECEIVE_UUID,
                BluetoothGattCharacteristic.PROPERTY_WRITE | BluetoothGattCharacteristic.PERMISSION_READ,
                BluetoothGattCharacteristic.FORMAT_FLOAT);
        BluetoothGattDescriptor receiveDescriptor = new BluetoothGattDescriptor(
                DESC_UUID,BluetoothGattDescriptor.PERMISSION_READ
        );
        receive.addDescriptor(receiveDescriptor);
        service.addCharacteristic(receive);

        BluetoothGattCharacteristic send = new BluetoothGattCharacteristic(SEND_UUID,
                BluetoothGattCharacteristic.FORMAT_FLOAT,BluetoothGattCharacteristic.FORMAT_FLOAT);
        BluetoothGattDescriptor sendDescriptor = new BluetoothGattDescriptor(
                DESC_UUID,BluetoothGattDescriptor.PERMISSION_READ
        );
        send.addDescriptor(sendDescriptor);
        service.addCharacteristic(send);

        server.addService(service);
    }

    /**
     * 写数据到 指定key的BluetoothGattCharacteristic
     * @param values 数据
     * @param key 键值
     */
    public void sendValues(byte[] values,String key){

        if(initStrategy == null){
            if(receive == null){
                throw new NullPointerException("receive is null");
            }
            receive.setValue(values);
            for(BluetoothDevice device : server.getConnectedDevices()){
                server.notifyCharacteristicChanged(device,receive,true);
            }
        }else{
            BluetoothGattCharacteristic characteristic = initStrategy.getSendCharacteristic(key);
           if(characteristic == null){
               throw new IllegalArgumentException("it has no characteristic belong to" + key);
           }else{
               characteristic.setValue(values);
               for(BluetoothDevice device : server.getConnectedDevices()){
                   server.notifyCharacteristicChanged(device,receive,true);
               }
           }
        }
    }


    /**
     * 设置蓝牙服务端，初始化策略
     * @param initStrategy  初始化策略，给外部提供接口
     * @param isClear true 删除历史添加服务
     */
    public void setInitStrategy(ServerInitStrategy initStrategy,boolean isClear) {

        this.initStrategy = initStrategy;
        if (isClear){
            clearServices();
        }
        addService(initStrategy.getService());

    }

}
