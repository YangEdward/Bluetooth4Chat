/**
 * This XPG software is supplied to you by Xtreme Programming Group, Inc.
 * ("XPG") in consideration of your agreement to the following terms, and your
 * use, installation, modification or redistribution of this XPG software
 * constitutes acceptance of these terms.� If you do not agree with these terms,
 * please do not use, install, modify or redistribute this XPG software.
 * 
 * In consideration of your agreement to abide by the following terms, and
 * subject to these terms, XPG grants you a non-exclusive license, under XPG's
 * copyrights in this original XPG software (the "XPG Software"), to use and
 * redistribute the XPG Software, in source and/or binary forms; provided that
 * if you redistribute the XPG Software, with or without modifications, you must
 * retain this notice and the following text and disclaimers in all such
 * redistributions of the XPG Software. Neither the name, trademarks, service
 * marks or logos of XPG Inc. may be used to endorse or promote products derived
 * from the XPG Software without specific prior written permission from XPG.�
 * Except as expressly stated in this notice, no other rights or licenses,
 * express or implied, are granted by XPG herein, including but not limited to
 * any patent rights that may be infringed by your derivative works or by other
 * works in which the XPG Software may be incorporated.
 * 
 * The XPG Software is provided by XPG on an "AS IS" basis.� XPG MAKES NO
 * WARRANTIES, EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION THE IMPLIED
 * WARRANTIES OF NON-INFRINGEMENT, MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE, REGARDING THE XPG SOFTWARE OR ITS USE AND OPERATION ALONE OR IN
 * COMBINATION WITH YOUR PRODUCTS.
 * 
 * IN NO EVENT SHALL XPG BE LIABLE FOR ANY SPECIAL, INDIRECT, INCIDENTAL OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) ARISING IN ANY WAY OUT OF THE USE, REPRODUCTION, MODIFICATION
 * AND/OR DISTRIBUTION OF THE XPG SOFTWARE, HOWEVER CAUSED AND WHETHER UNDER
 * THEORY OF CONTRACT, TORT (INCLUDING NEGLIGENCE), STRICT LIABILITY OR
 * OTHERWISE, EVEN IF XPG HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * ABOUT XPG: Established since June 2005, YangEdward Programming Group, Inc. (XPG)
 * is a digital solutions company based in the United States and China. XPG
 * integrates cutting-edge hardware designs, mobile applications, and cloud
 * computing technologies to bring innovative products to the marketplace. XPG's
 * partners and customers include global leading corporations in semiconductor,
 * home appliances, health/wellness electronics, toys and games, and automotive
 * industries. Visit www.studyun.com for more information.
 * 
 * Copyright (C) 2013 YangEdward Programming Group, Inc. All Rights Reserved.
 */

package com.studyun.bluetooth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.util.Log;

/**
 * android BLE main class,all action will do through this class.
 * all of phone just have one bluetooth device,so this class is a singleton mode.
 * you can use it to start or stop scanning other devices around it,connect other device,
 * and read, write informations.
 */
public class ClientBle{

	protected static final String TAG = ClientBle.class.getSimpleName();

	/**
	 * singleton instance
	 */
	private static ClientBle androidBle;
	/* use service to send broadcast*/
	private BleClientService mService;

	private final static int DEFAULT_SCAN_TIME = 1000;
	/*bluetooth adapter*/

	private BluetoothAdapter mBtAdapter;

	/**/
	private Map<String, BluetoothGatt> mBluetoothGatts;

	/**
	 * scan call back,use service to update applications
	 */
	private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
		@Override
		public void onLeScan(final BluetoothDevice device, int rssi,
				byte[] scanRecord) {
			mService.bleDeviceFound(device, rssi, scanRecord,
					ServiceBroadcast.DEVICE_SOURCE_SCAN);
		}
	};

	private BluetoothGattCallback mGattCallback;

	public static ClientBle getInstance(BleClientService service){

		if(androidBle == null){
			synchronized(ClientBle.class) {
				if(androidBle == null){
					androidBle = new ClientBle(service);
				}
			}
		}
		return androidBle;
	}

    /**
     * 获取本机蓝牙名称
     * @return 蓝牙名称
     */
    public String getMyDeviceName(){
        return mBtAdapter.getName();
    }

    /**
     * 设置本机蓝牙名称
     * @param name 名称
     */
    public boolean setDeviceName(String name){
        return mBtAdapter.setName(name);
    }

	private ClientBle(BleClientService service) {

		mService = service;
		if (!mService.getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_BLUETOOTH_LE)) {
			mService.bleNotSupported();
			return;
		}

		final BluetoothManager bluetoothManager = (BluetoothManager) mService
				.getSystemService(Context.BLUETOOTH_SERVICE);

		mBtAdapter = bluetoothManager.getAdapter();
		if (mBtAdapter == null) {
			mService.bleNoBtAdapter();
		}
		mBluetoothGatts = new HashMap<>();
		mGattCallback = new ClientCallBack(service);
	}

	/**
	 * @param cancelAuto 蓝牙扫描是比较耗电的，通常请设置扫描后自动关闭,默认扫描时间为10秒
	 */
	public void startScan(boolean cancelAuto) {
		if(mBtAdapter == null){
			return;
		}
		mBtAdapter.startLeScan(mLeScanCallback);
		if(cancelAuto){
			Handler handler = new Handler();
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					stopScan();
				}
			}, DEFAULT_SCAN_TIME);
		}
	}

	/**
	 * stop bluetooth scan
	 */
	public void stopScan() {
		mBtAdapter.stopLeScan(mLeScanCallback);
	}

	/**
	 * @return true if bluetooth enable
	 */
	public boolean adapterEnabled() {
		return mBtAdapter != null && mBtAdapter.isEnabled();
	}

	/**
	 * \request to connect remote device,this request will be put in the request queue
	 * @param address remote device
	 * @return true if request success
	 */
	public boolean requestConnect(String address) {
		BluetoothGatt gatt = mBluetoothGatts.get(address);
		if (gatt != null && gatt.getServices().size() == 0) {
			return false;
		}

		mService.addBleRequest(new BleRequest(BleRequest.RequestType.CONNECT_GATT, address));
		return true;
	}

	/**
	 * connect device
	 * @param address device address
	 * @return true if success
	 */
	boolean connect(String address) {
		BluetoothDevice device = mBtAdapter.getRemoteDevice(address);
		BluetoothGatt gatt = device.connectGatt(mService, false, mGattCallback);
		if (gatt == null) {
			mBluetoothGatts.remove(address);
			return false;
		} else {
			mBluetoothGatts.put(address, gatt);
			return true;
		}
	}

	/**
	 * disconnect device
	 * @param address address of device which want to disconnect
	 */
	public void disconnect(String address) {
		if (mBluetoothGatts.containsKey(address)) {
			BluetoothGatt gatt = mBluetoothGatts.remove(address);
			if (gatt != null) {
				gatt.disconnect();
				gatt.close();
			}
		}
	}

	/**
	 * when this device is BLE client,it can use this method to get BLE service from remote device
	 * @param address device address
	 * @return many BLE services
	 */
	public List<BluetoothGattService> getServices(String address) {
		BluetoothGatt gatt = mBluetoothGatts.get(address);
		if (gatt == null) {
			return null;
		}

		List<BluetoothGattService> list = new ArrayList<>();
		list.addAll(gatt.getServices());
		return list;
	}

	/**
	 * when this device is BLE client,it can use this method to get BLE service from remote device
	 * @param address device address
	 * @param uuid service uuid
	 * @return many BLE services
	 */
	public BluetoothGattService getService(String address, UUID uuid) {
		BluetoothGatt gatt = mBluetoothGatts.get(address);
		if (gatt == null) {
			return null;
		}

		BluetoothGattService service = gatt.getService(uuid);
		if (service == null) {
			return null;
		} else {
			return service;
		}
	}

	/**
	 * request to read characteristic,this request will be put in the request queue
	 * @param address remote device address
	 * @param characteristic  which is to be wrote
	 * @return true if request success
	 */
	public boolean requestReadCharacteristic(String address,
											 BluetoothGattCharacteristic characteristic) {
		BluetoothGatt gatt = mBluetoothGatts.get(address);
		if (gatt == null || characteristic == null) {
			return false;
		}

		mService.addBleRequest(new BleRequest(BleRequest.RequestType.READ_CHARACTERISTIC,
				gatt.getDevice().getAddress(), characteristic));
		return true;
	}

	/**
	 * read characteristic
	 * @param address remote device address
	 * @param characteristic  which is to be wrote
	 * @return true if read success
	 */
	boolean readCharacteristic(String address,
			BluetoothGattCharacteristic characteristic) {
		BluetoothGatt gatt = mBluetoothGatts.get(address);

		return gatt != null && gatt.readCharacteristic(characteristic);
	}

	/**
	 * request to discover services,this request will be put in the request queue
	 * @param address remote device address
	 * @return true if request success
	 */
	public boolean requestDiscoverServices (String address){
		BluetoothGatt gatt = mBluetoothGatts.get(address);
		if (gatt == null) {
			return false;
		}
		mService.addBleRequest(new BleRequest(
				BleRequest.RequestType.DISCOVER_SERVICE, address));
		return true;
	}

	/**
	 * discover services
	 * @param address remote device address
	 * @return true if discover success
	 */
	boolean discoverServices(String address) {
		BluetoothGatt gatt = mBluetoothGatts.get(address);
		if (gatt == null) {
			return false;
		}

		boolean ret = gatt.discoverServices();
		if (!ret) {
			disconnect(address);
		}
		return ret;
	}

	/**
	 * invoke write characteristic for remote device,this request will be put in the queue
	 * @param address  remote device address
	 * @param characteristic
	 *            Get characteristic from {@link BluetoothGattService}
	 * @param remark
	 *            For debug purpose.
	 * @return true if request success
	 */
	public boolean requestWriteCharacteristic(String address,
											  BluetoothGattCharacteristic characteristic, String remark) {
		BluetoothGatt gatt = mBluetoothGatts.get(address);
		if (gatt == null || characteristic == null) {
			return false;
		}

		mService.addBleRequest(new BleRequest(BleRequest.RequestType.WRITE_CHARACTERISTIC,
				gatt.getDevice().getAddress(), characteristic, remark));
		return true;
	}

	/**
	 * write characteristic for remote device
	 * @param address remote device address
	 * @param characteristic characteristic which is to be wrote
	 * @return true if write success
	 */
	boolean writeCharacteristic(String address,
								BluetoothGattCharacteristic characteristic) {
		BluetoothGatt gatt = mBluetoothGatts.get(address);
		return gatt != null && gatt
				.writeCharacteristic(characteristic);
	}

	/**
	 * invoke read descriptor for remote device,this request will be put in the queue
	 * @param address  remote device address
	 * @param descriptor Get descriptor from {@link BluetoothGattService}
	 * @return true if request success
	 */
	public boolean requestReadDescriptor(String address,
											  BluetoothGattDescriptor descriptor) {
		BluetoothGatt gatt = mBluetoothGatts.get(address);
		if (gatt == null || descriptor == null) {
			return false;
		}

		mService.addBleRequest(new BleRequest(BleRequest.RequestType.READ_DESCRIPTOR,
				gatt.getDevice().getAddress(), descriptor));
		return true;
	}

	/**
	 * read descriptor for remote device
	 * @param address remote device address
	 * @param descriptor descriptor which is to be read
	 * @return true if read success
	 */
	boolean readDescriptor(String address,
								BluetoothGattDescriptor descriptor) {
		BluetoothGatt gatt = mBluetoothGatts.get(address);
		return gatt != null && gatt
				.readDescriptor(descriptor);
	}

	/**
	 * invoke write descriptor for remote device,this request will be put in the queue
	 * @param address  remote device address
	 * @param descriptor Get descriptor from {@link BluetoothGattService}
	 * @return true if request success
	 */
	public boolean requestWriteDescriptor(String address,
										 BluetoothGattDescriptor descriptor) {
		BluetoothGatt gatt = mBluetoothGatts.get(address);
		if (gatt == null || descriptor == null) {
			return false;
		}

		mService.addBleRequest(new BleRequest(BleRequest.RequestType.WRITE_DESCRIPTOR,
				gatt.getDevice().getAddress(), descriptor));
		return true;
	}

	/**
	 * write descriptor for remote device
	 * @param address remote device address
	 * @param descriptor descriptor which is to be wrote
	 * @return true if write success
	 */
	boolean writeDescriptor(String address,
						   BluetoothGattDescriptor descriptor) {
		BluetoothGatt gatt = mBluetoothGatts.get(address);
		return gatt != null && gatt
				.readDescriptor(descriptor);
	}

	/**
	 * request notification characteristic,this request will be put in the queue.
	 * @param address remote device address
	 * @param characteristic which to be notified
	 * @return true if request success
	 */
	public boolean requestCharacteristicNotification(String address,
													 BluetoothGattCharacteristic characteristic) {
		BluetoothGatt gatt = mBluetoothGatts.get(address);
		if (gatt == null || characteristic == null) {
			return false;
		}

		mService.addBleRequest(new BleRequest(
				BleRequest.RequestType.CHARACTERISTIC_NOTIFICATION, gatt.getDevice()
				.getAddress(), characteristic));
		return true;
	}

	/**
	 * request indication characteristic,this request will be put in the queue.
	 * @param address remote device address
	 * @param characteristic which to be indicated
	 * @return true if request success
	 */
	public boolean requestIndication(String address,
									 BluetoothGattCharacteristic characteristic) {
		BluetoothGatt gatt = mBluetoothGatts.get(address);
		if (gatt == null || characteristic == null) {
			return false;
		}

		mService.addBleRequest(new BleRequest(
				BleRequest.RequestType.CHARACTERISTIC_INDICATION, gatt.getDevice()
				.getAddress(), characteristic));
		return true;
	}

	/**
	 * request stop notification characteristic,this request will be put in the queue.
	 * @param address remote device address
	 * @param characteristic which to be stopped notification
	 * @return true if request success
	 */
	public boolean requestStopNotification(String address,
										   BluetoothGattCharacteristic characteristic) {
		BluetoothGatt gatt = mBluetoothGatts.get(address);
		if (gatt == null || characteristic == null) {
			return false;
		}

		mService.addBleRequest(new BleRequest(
				BleRequest.RequestType.CHARACTERISTIC_STOP_NOTIFICATION, gatt.getDevice()
				.getAddress(), characteristic));
		return true;
	}

	/**
	 * set notification characteristic.
	 * @param address remote device address
	 * @param characteristic which to be sot
	 * @return true if set success
	 */
	boolean characteristicNotification(String address,
											  BluetoothGattCharacteristic characteristic) {
		BleRequest request = mService.getCurrentRequest();
		BluetoothGatt gatt = mBluetoothGatts.get(address);
		if (gatt == null || characteristic == null) {
			return false;
		}

		boolean enable = true;
		if (request.type == BleRequest.RequestType.CHARACTERISTIC_STOP_NOTIFICATION) {
			enable = false;
		}

		if (!gatt.setCharacteristicNotification(characteristic, enable)) {
			return false;
		}

		BluetoothGattDescriptor descriptor = characteristic
				.getDescriptor(ServiceBroadcast.DESC_CCC);
		if (descriptor == null) {
			return false;
		}

		byte[] val_set;
		if (request.type == BleRequest.RequestType.CHARACTERISTIC_NOTIFICATION) {
			val_set = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
		} else if (request.type == BleRequest.RequestType.CHARACTERISTIC_INDICATION) {
			val_set = BluetoothGattDescriptor.ENABLE_INDICATION_VALUE;
		} else {
			val_set = BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;
		}

		return descriptor.setValue(val_set) && requestWriteDescriptor(address,descriptor);
	}

	/**
	 * get bluetooth address
	 * @return address
	 */
	public String getBTAdapterMacAddr() {
		if (mBtAdapter != null) {
			return mBtAdapter.getAddress();
		}
		return null;
	}
	/**
	 * request to execute reliable write for remote device,this request will be put in the queue.
	 * @param address  remote device address
	 * @return true if request success
	 */
	public boolean requestExecuteReliableWrite(String address) {
		BluetoothGatt gatt = mBluetoothGatts.get(address);
		if (gatt == null) {
			Log.w(TAG, "BluetoothAdapter not initialized");
			return false;
		}
		mService.addBleRequest(new BleRequest(
				BleRequest.RequestType.RELIABLE_WRITE_COMPLETED, gatt.getDevice()
				.getAddress()));
		return true;
	}

	/**
	 * Execute reliable write.
	 * @param address  remote device address
	 * @return true if execute success
	 */
	boolean executeReliableWrite(String address){
		BluetoothGatt gatt = mBluetoothGatts.get(address);
		return gatt != null && gatt.executeReliableWrite();
	}

	/**
	 * request to read rssi for remote device,this request will be put in the queue.
	 * @param address  remote device address
	 * @return true if request success
	 */
	public boolean requestReadRssi(String address) {
		BluetoothGatt gatt = mBluetoothGatts.get(address);
		if (gatt == null) {
			Log.w(TAG, "BluetoothAdapter not initialized");
			return false;
		}
		mService.addBleRequest(new BleRequest(
				BleRequest.RequestType.READ_RSSI, gatt.getDevice()
				.getAddress()));
		return true;
	}

	/**
	 * read rssi.
	 * @param address  remote device address
	 * @return true if read success
	 */
	boolean readRssi(String address){
		BluetoothGatt gatt = mBluetoothGatts.get(address);
		return gatt != null && gatt.readRemoteRssi();
	}

	/**
	 * close bluetooth client.
	 */
	public void close() {
		for (BluetoothGatt gatt : mBluetoothGatts.values()) {
			gatt.close();
		}
	}
}
