package com.studyun.bluetooth4chat;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.studyun.bluetooth.BleClientService;
import com.studyun.bluetooth.BleServerService;
import com.studyun.bluetooth.ClientBle;
import com.studyun.bluetooth.ServiceBroadcast;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 */
public class BLEChatActivity extends Activity implements View.OnClickListener{

    private static final String TAG = BLEChatActivity.class.getSimpleName();
    private List<Message> messageList;
    private EditText messageText;
    private MessageAdapter messageAdapter;
    private ClientBle mBle;
    private BleClientService service;
    private BleServerService serverService;
    private List<BluetoothGattCharacteristic> writes = new ArrayList<>();
    private List<BluetoothGattCharacteristic> reads = new ArrayList<>();
    private boolean isConnect;
    private String connectDeviceAddress;
    public static boolean isServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blechat);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        messageText = (EditText)findViewById(R.id.message);

        Button send = (Button)findViewById(R.id.send);
        send.setOnClickListener(this);

        ListView listView = (ListView)findViewById(R.id.list);
        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(this,messageList);
        listView.setAdapter(messageAdapter);

    }

    /**
     * {@inheritDoc}
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.blue_chat_menu, menu);
        // Set up ShareActionProvider's default share intent
        //MenuItem shareItem = menu.findItem(R.id.action_share);
        /*ShareActionProvider mShareActionProvider = (ShareActionProvider)
                shareItem.getActionProvider();
        mShareActionProvider.setShareIntent(getDefaultIntent());*/
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * menu 处理，进入蓝牙搜索，蓝牙设置服务端或客户端，分享.
     * {@inheritDoc}
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.setting:
                return true;
            case R.id.scan:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /** Defines a default (dummy) share intent to initialize the action provider.
     * However, as soon as the actual content to be used in the intent
     * is known or changes, you must update the share intent by again calling
     * mShareActionProvider.setShareIntent()
     */
    private Intent getDefaultIntent() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/*");
        return intent;
    }

    /**
     * 发送消息
     * @param v send button
     */
    @Override
    public void onClick(View v) {
        String content = messageText.getText().toString();

        if(content.isEmpty()){
           return;
        }
        writeData(content.getBytes());
        Message message = new Message(mBle.getMyDeviceName(),DateUtils.now(),
                content,Message.SEND);

        messageList.add(message);
        messageAdapter.notifyDataSetChanged();
    }

    private void writeData(byte[] data){
        data = new byte[]{(byte) 0xAE,(byte) 0xAA,(byte)0x01,(byte)0x50,(byte)0x58,(byte)0x56};
        if(isConnect && writes.size() != 0){
            for(BluetoothGattCharacteristic characteristic : writes){
            //BluetoothGattCharacteristic characteristic = writes.get(1);
                    characteristic.setValue(data);
                mBle.requestWriteCharacteristic(connectDeviceAddress,characteristic,"");
            }
        }
    }
    /**
     * 蓝牙服务监听，Client模式监听
     */
    private final BroadcastReceiver mBleClientReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();
            String action = intent.getAction();
            BluetoothDevice device;
            String address;
            UUID uuid;
            byte [] values;
            switch (action) {
                case ServiceBroadcast.BLE_DEVICE_FOUND:
                    mBle.stopScan();
                    device = extras.getParcelable(ServiceBroadcast.EXTRA_DEVICE);
                    mBle.requestConnect(device.getAddress());
                    break;
                case ServiceBroadcast.BLE_GATT_CONNECTED:
                    device = extras.getParcelable(ServiceBroadcast.EXTRA_DEVICE);
                    connectDeviceAddress = device.getAddress();
                    Log.e(TAG,"device name = " + device.getName());
                    isConnect = true;
                    updateConnectState(R.string.connected);
                    break;
                case ServiceBroadcast.BLE_GATT_DISCONNECTED:
                    address = extras.getString(ServiceBroadcast.EXTRA_ADDR);
                    isConnect = false;
                    updateConnectState(R.string.disconnected);
                    break;
                case ServiceBroadcast.BLE_SERVICE_DISCOVERED:
                    address = extras.getString(ServiceBroadcast.EXTRA_ADDR);
                    showServices(mBle.getServices(address));
                    break;
                /*read characteristic values,invoked by client*/
                case ServiceBroadcast.BLE_CHARACTERISTIC_READ:
                    address = extras.getString(ServiceBroadcast.EXTRA_ADDR);
                    uuid = (UUID)extras.getSerializable(ServiceBroadcast.EXTRA_UUID);
                    values = extras.getByteArray(ServiceBroadcast.EXTRA_VALUE);
                    break;
                /*when characteristic changed,remote device invoked*/
                case ServiceBroadcast.BLE_CHARACTERISTIC_CHANGED:
                    address = extras.getString(ServiceBroadcast.EXTRA_ADDR);
                    uuid = (UUID)extras.getSerializable(ServiceBroadcast.EXTRA_UUID);
                    values = extras.getByteArray(ServiceBroadcast.EXTRA_VALUE);
                    //updateValues(values);
                    break;
                case ServiceBroadcast.BLE_CHARACTERISTIC_NOTIFICATION:
                    break;
                case ServiceBroadcast.BLE_CHARACTERISTIC_INDICATION:
                    break;
                case ServiceBroadcast.BLE_CHARACTERISTIC_WRITE:
                    address = extras.getString(ServiceBroadcast.EXTRA_ADDR);
                    uuid = (UUID)extras.getSerializable(ServiceBroadcast.EXTRA_UUID);
                    Log.e(TAG,"Write Success");
                    //Write Success
                    break;
                case ServiceBroadcast.BLE_DESCRIPTOR_READ:
                    break;
                case ServiceBroadcast.BLE_DESCRIPTOR_WRITE:
                    break;
            }
        }

    };

    private void showServices(List<BluetoothGattService> services){
        for(BluetoothGattService service : services){
            Log.e(TAG,"service Type = " + service.getType());
            Log.e(TAG,"service UUID = " + service.getUuid().toString());
            for(BluetoothGattCharacteristic characteristic : service.getCharacteristics()){
                if((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE) != 0){
                    writes.add(characteristic);
                }
                if((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_READ) != 0){
                    reads.add(characteristic);
                    mBle.requestCharacteristicNotification(connectDeviceAddress,characteristic);
                }
                Log.e(TAG,"characteristic Permissions = " + characteristic.getPermissions());
                Log.e(TAG,"characteristic Properties = " + characteristic.getProperties());
                Log.e(TAG,"characteristic WriteType = " + characteristic.getWriteType());
                Log.e(TAG,"characteristic Value = " + String.valueOf(characteristic.getValue()));
                Log.e(TAG,"characteristic UUID = " + characteristic.getUuid().toString());
                for(BluetoothGattDescriptor descriptor : characteristic.getDescriptors()){
                    Log.e(TAG,"descriptor Permissions = " + descriptor.getPermissions());
//                    Log.e(TAG,"descriptor Value = " + descriptor.getValue().toString());
                    Log.e(TAG,"descriptor UUID = " + descriptor.getUuid().toString());
                }
            }
        }
    }
    /**
     * 蓝牙服务监听，Server模式监听
     */
    private final BroadcastReceiver mBleServerReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            Bundle extras = intent.getExtras();
            String action = intent.getAction();
            String address;
            UUID uuid;
            byte [] values;
            switch (action){
                case ServiceBroadcast.BLE_DEVICE_FOUND:
                    mBle.stopScan();
                    BluetoothDevice device = extras.getParcelable(ServiceBroadcast.EXTRA_DEVICE);
                    mBle.requestConnect(device.getAddress());
                    break;
                case ServiceBroadcast.BLE_GATT_CONNECTED:
                    address  = extras.getString(ServiceBroadcast.EXTRA_ADDR);
                    updateConnectState(R.string.connected);
                    break;
                case ServiceBroadcast.BLE_GATT_DISCONNECTED:
                    address  = extras.getString(ServiceBroadcast.EXTRA_ADDR);
                    updateConnectState(R.string.disconnected);
                    break;
                case ServiceBroadcast.BLE_SERVICE_DISCOVERED:
                    address  = extras.getString(ServiceBroadcast.EXTRA_ADDR);
                    mBle.getServices(address);
                    break;
                /*read characteristic values,invoked by client*/
                case ServiceBroadcast.BLE_CHARACTERISTIC_READ:
                    address  = extras.getString(ServiceBroadcast.EXTRA_ADDR);
                    uuid = (UUID)extras.getSerializable(ServiceBroadcast.EXTRA_UUID);
                    values = extras.getByteArray(ServiceBroadcast.EXTRA_VALUE);
                    break;
                /*when characteristic changed,remote device invoked*/
                case ServiceBroadcast.BLE_CHARACTERISTIC_CHANGED:
                    address  = extras.getString(ServiceBroadcast.EXTRA_ADDR);
                    uuid = (UUID)extras.getSerializable(ServiceBroadcast.EXTRA_UUID);
                    values = extras.getByteArray(ServiceBroadcast.EXTRA_VALUE);
                    //updateValues(values);
                    break;
                case ServiceBroadcast.BLE_CHARACTERISTIC_NOTIFICATION:
                    break;
                case ServiceBroadcast.BLE_CHARACTERISTIC_INDICATION:
                    break;
                case ServiceBroadcast.BLE_CHARACTERISTIC_WRITE:
                    address  = extras.getString(ServiceBroadcast.EXTRA_ADDR);
                    uuid = (UUID)extras.getSerializable(ServiceBroadcast.EXTRA_UUID);
                    //Write Success
                    break;
                case ServiceBroadcast.BLE_DESCRIPTOR_READ:
                    break;
                case ServiceBroadcast.BLE_DESCRIPTOR_WRITE:
                    break;
            }
        }
    };

    /**
     * 更新连接状态
     */
    private void updateConnectState(int state){
        Toast.makeText(this,state,Toast.LENGTH_LONG).show();
    }

    private final static int REQUEST_ENABLE_BT = 1;
    /**
     * 蓝牙服务绑定和解绑处理
     */
    private final ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder iBinder) {
            if(isServer){
                serverService = ((BleServerService.LocalBinder)iBinder).getService();
            }else{
                service = ((BleClientService.LocalBinder)iBinder).getService();
                mBle = service.getClientBle();
            }
            if(mBle != null && !mBle.adapterEnabled()){
                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableIntent,REQUEST_ENABLE_BT);
            }
            if(mBle != null){
                mBle.startScan(true);
            }
            Log.e(TAG,"onServiceConnected");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            if(isServer){
                serverService = null;
            }else{
                service = null;
            }
        }

    };

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent;
        if(isServer){
            registerReceiver(mBleServerReceiver, BleServerService.getIntentFilter());
            intent = new Intent(this,BleServerService.class);
        }else{
            registerReceiver(mBleClientReceiver, BleClientService.getIntentFilter());
            intent = new Intent(this,BleClientService.class);
        }
        bindService(intent,connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(isServer){
            unregisterReceiver(mBleServerReceiver);
        }else{
            unregisterReceiver(mBleClientReceiver);
        }
        unbindService(connection);
    }
}
