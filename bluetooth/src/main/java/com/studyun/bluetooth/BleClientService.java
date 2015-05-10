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
 * ABOUT XPG: Established since June 2005, Xtreme Programming Group, Inc. (XPG)
 * is a digital solutions company based in the United States and China. XPG
 * integrates cutting-edge hardware designs, mobile applications, and cloud
 * computing technologies to bring innovative products to the marketplace. XPG's
 * partners and customers include global leading corporations in semiconductor,
 * home appliances, health/wellness electronics, toys and games, and automotive
 * industries. Visit www.xtremeprog.com for more information.
 * 
 * Copyright (C) 2013 Xtreme Programming Group, Inc. All Rights Reserved.
 */

package com.studyun.bluetooth;

import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;


public class BleClientService extends Service {

	private static final String TAG = BleClientService.class.getSimpleName();

	private final IBinder mBinder = new LocalBinder();

	private ClientBle clientBle;
	private final Queue<BleRequest> mRequestQueue = new LinkedList<>();
	private BleRequest mCurrentRequest = null;
	private static final int REQUEST_TIMEOUT = 10 * 10; // total timeout =
														// REQUEST_TIMEOUT *
														// 100ms
	private boolean mCheckTimeout = false;

	private Thread mRequestTimeout;
	private String mNotificationAddress;

	private Runnable mTimeoutRunnable = new Runnable() {
		@Override
		public void run() {
			Log.d(TAG, "monitoring thread start");
			int mElapsed = 0;
			try {
				while (mCheckTimeout) {
					// Log.d(TAG, "monitoring timeout seconds: " + mElapsed);
					Thread.sleep(100);
					mElapsed++;

					if (mElapsed > REQUEST_TIMEOUT && mCurrentRequest != null) {
						Log.d(TAG, "-processRequest type "
								+ mCurrentRequest.type + " address "
								+ mCurrentRequest.address + " [timeout]");
						bleRequestFailed(mCurrentRequest.address,
								mCurrentRequest.type, BleRequest.FailReason.TIMEOUT);
						bleStatusAbnormal("-processRequest type "
								+ mCurrentRequest.type + " address "
								+ mCurrentRequest.address + " [timeout]");
						if (clientBle != null) {
							clientBle.disconnect(mCurrentRequest.address);
						}
						new Thread(new Runnable() {
							@Override
							public void run() {
								mCurrentRequest = null;
								processNextRequest();
							}
						}, "th-ble").start();
						break;
					}
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
				Log.d(TAG, "monitoring thread exception");
			}
			Log.d(TAG, "monitoring thread stop");
		}
	};

	public static IntentFilter getIntentFilter() {
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ServiceBroadcast.BLE_NOT_SUPPORTED);
		intentFilter.addAction(ServiceBroadcast.BLE_NO_BT_ADAPTER);
		intentFilter.addAction(ServiceBroadcast.BLE_STATUS_ABNORMAL);
		intentFilter.addAction(ServiceBroadcast.BLE_REQUEST_FAILED);
		intentFilter.addAction(ServiceBroadcast.BLE_DEVICE_FOUND);
		intentFilter.addAction(ServiceBroadcast.BLE_GATT_CONNECTED);
		intentFilter.addAction(ServiceBroadcast.BLE_GATT_DISCONNECTED);
		intentFilter.addAction(ServiceBroadcast.BLE_SERVICE_DISCOVERED);
		intentFilter.addAction(ServiceBroadcast.BLE_CHARACTERISTIC_READ);
		intentFilter.addAction(ServiceBroadcast.BLE_CHARACTERISTIC_NOTIFICATION);
		intentFilter.addAction(ServiceBroadcast.BLE_CHARACTERISTIC_WRITE);
		intentFilter.addAction(ServiceBroadcast.BLE_CHARACTERISTIC_CHANGED);
		return intentFilter;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	public class LocalBinder extends Binder {
		public BleClientService getService() {
			return BleClientService.this;
		}
	}

	@Override
	public void onCreate() {
		clientBle = ClientBle.getInstance(this);
	}

	protected void bleNotSupported() {
		Intent intent = new Intent(ServiceBroadcast.BLE_NOT_SUPPORTED);
		sendBroadcast(intent);
	}

	protected void bleNoBtAdapter() {
		Intent intent = new Intent(ServiceBroadcast.BLE_NO_BT_ADAPTER);
		sendBroadcast(intent);
	}

	public ClientBle getClientBle() {
		return clientBle;
	}

	/**
	 * Send {@link ServiceBroadcast#BLE_DEVICE_FOUND} broadcast. <br>
	 * <br>
	 * Data in the broadcast intent: <br>
	 * {@link ServiceBroadcast#EXTRA_DEVICE} device {@link BluetoothDevice} <br>
	 * {@link ServiceBroadcast#EXTRA_RSSI} rssi int<br>
	 * {@link ServiceBroadcast#EXTRA_SCAN_RECORD} scan record byte[] <br>
	 * {@link ServiceBroadcast#EXTRA_SOURCE} source int, not used now <br>
	 */
	protected void bleDeviceFound(BluetoothDevice device, int rssi,
			byte[] scanRecord, int source) {
		/*Log.d("blelib", "[" + new Date().toLocaleString() + "] device found "
				+ device.getAddress());*/
		Intent intent = new Intent(ServiceBroadcast.BLE_DEVICE_FOUND);
		intent.putExtra(ServiceBroadcast.EXTRA_DEVICE, device);
		intent.putExtra(ServiceBroadcast.EXTRA_RSSI, rssi);
		intent.putExtra(ServiceBroadcast.EXTRA_SCAN_RECORD, scanRecord);
		intent.putExtra(ServiceBroadcast.EXTRA_SOURCE, source);
		sendBroadcast(intent);
	}

	/**
	 * Send {@link ServiceBroadcast#BLE_GATT_CONNECTED} broadcast. <br>
	 * <br>
	 * Data in the broadcast intent: <br>
	 * {@link ServiceBroadcast#EXTRA_DEVICE} device {@link BluetoothDevice} <br>
	 */
	protected void bleGattConnected(BluetoothDevice device) {
		Intent intent = new Intent(ServiceBroadcast.BLE_GATT_CONNECTED);
		intent.putExtra(ServiceBroadcast.EXTRA_DEVICE, device);
		sendBroadcast(intent);
		requestProcessed(device.getAddress(), BleRequest.RequestType.CONNECT_GATT, true);
	}

	/**
	 * Send {@link ServiceBroadcast#BLE_GATT_DISCONNECTED} broadcast. <br>
	 * <br>
	 * Data in the broadcast intent: <br>
	 * {@link ServiceBroadcast#EXTRA_ADDR} device address {@link String} <br>
	 * 
	 * @param address
	 */
	protected void bleGattDisConnected(String address) {
		Intent intent = new Intent(ServiceBroadcast.BLE_GATT_DISCONNECTED);
		intent.putExtra(ServiceBroadcast.EXTRA_ADDR, address);
		sendBroadcast(intent);
		requestProcessed(address, BleRequest.RequestType.CONNECT_GATT, false);
	}

	/**
	 * Send {@link ServiceBroadcast#BLE_SERVICE_DISCOVERED} broadcast. <br>
	 * <br>
	 * Data in the broadcast intent: <br>
	 * {@link ServiceBroadcast#EXTRA_ADDR} device address {@link String} <br>
	 * 
	 * @param address
	 */
	protected void bleServiceDiscovered(String address) {
		Intent intent = new Intent(ServiceBroadcast.BLE_SERVICE_DISCOVERED);
		intent.putExtra(ServiceBroadcast.EXTRA_ADDR, address);
		sendBroadcast(intent);
		requestProcessed(address, BleRequest.RequestType.DISCOVER_SERVICE, true);
	}

	protected void requestProcessed(String address, BleRequest.RequestType requestType,
			boolean success) {
		if (mCurrentRequest != null && mCurrentRequest.type == requestType) {
			clearTimeoutThread();
			Log.d(TAG, "-processrequest type " + requestType + " address "
					+ address + " [success: " + success + "]");
			if (!success) {
				bleRequestFailed(mCurrentRequest.address, mCurrentRequest.type,
						BleRequest.FailReason.RESULT_FAILED);
			}
			new Thread(new Runnable() {
				@Override
				public void run() {
					mCurrentRequest = null;
					processNextRequest();
				}
			}, "th-ble").start();
		}
	}

	private void clearTimeoutThread() {
		if (mRequestTimeout.isAlive()) {
			try {
				mCheckTimeout = false;
				mRequestTimeout.join();
				mRequestTimeout = null;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Send {@link ServiceBroadcast#BLE_CHARACTERISTIC_READ} broadcast. <br>
	 * <br>
	 * Data in the broadcast intent: <br>
	 * {@link ServiceBroadcast#EXTRA_ADDR} device address {@link String} <br>
	 * {@link ServiceBroadcast#EXTRA_UUID} characteristic uuid {@link String}<br>
	 * {@link ServiceBroadcast#EXTRA_VALUE} data byte[] <br>
	 * 
	 * @param address
	 * @param uuid
	 * @param value
	 */
	protected void bleCharacteristicRead(String address, UUID uuid,byte[] value) {
		Intent intent = new Intent(ServiceBroadcast.BLE_CHARACTERISTIC_READ);
		intent.putExtra(ServiceBroadcast.EXTRA_ADDR, address);
		intent.putExtra(ServiceBroadcast.EXTRA_UUID, uuid);
		intent.putExtra(ServiceBroadcast.EXTRA_VALUE, value);
		sendBroadcast(intent);
		requestProcessed(address, BleRequest.RequestType.READ_CHARACTERISTIC, true);
	}

	protected void addBleRequest(BleRequest request) {
		synchronized (mRequestQueue) {
			mRequestQueue.add(request);
			processNextRequest();
		}
	}

	private void processNextRequest() {
		if (mCurrentRequest != null) {
			return;
		}

		synchronized (mRequestQueue) {
			if (mRequestQueue.isEmpty()) {
				return;
			}
			mCurrentRequest = mRequestQueue.remove();
		}
		Log.d(TAG, "+processrequest type " + mCurrentRequest.type + " address "
				+ mCurrentRequest.address + " remark " + mCurrentRequest.remark);
		boolean ret = false;
		switch (mCurrentRequest.type) {
		case CONNECT_GATT:
			ret = clientBle.connect(mCurrentRequest.address);
			break;
		case DISCOVER_SERVICE:
			ret = clientBle.discoverServices(mCurrentRequest.address);
			break;
		case CHARACTERISTIC_NOTIFICATION:
		case CHARACTERISTIC_INDICATION:
		case CHARACTERISTIC_STOP_NOTIFICATION:
			ret = clientBle.characteristicNotification(
					mCurrentRequest.address, mCurrentRequest.characteristic);
			break;
		case READ_CHARACTERISTIC:
			ret = clientBle.readCharacteristic(
					mCurrentRequest.address, mCurrentRequest.characteristic);
			break;
		case WRITE_CHARACTERISTIC:
			ret = clientBle.writeCharacteristic(
					mCurrentRequest.address, mCurrentRequest.characteristic);
			break;
		case READ_DESCRIPTOR:
			ret = clientBle.readDescriptor(
					mCurrentRequest.address, mCurrentRequest.descriptor);
			break;
		case WRITE_DESCRIPTOR:
			ret = clientBle.writeDescriptor(
					mCurrentRequest.address, mCurrentRequest.descriptor);
			break;
		case READ_RSSI:
			ret = clientBle.readRssi(mCurrentRequest.address);
			break;
		case RELIABLE_WRITE_COMPLETED:
			ret = clientBle.executeReliableWrite(mCurrentRequest.address);
			break;
		default:
			break;
		}

		if (ret) {
			startTimeoutThread();
		} else {
			Log.d(TAG, "-processrequest type " + mCurrentRequest.type
					+ " address " + mCurrentRequest.address + " [fail start]");
			bleRequestFailed(mCurrentRequest.address, mCurrentRequest.type,
					BleRequest.FailReason.START_FAILED);
			new Thread(new Runnable() {
				@Override
				public void run() {
					mCurrentRequest = null;
					processNextRequest();
				}
			}, "th-ble").start();
		}
	}

	private void startTimeoutThread() {
		mCheckTimeout = true;
		mRequestTimeout = new Thread(mTimeoutRunnable);
		mRequestTimeout.start();
	}

	protected BleRequest getCurrentRequest() {
		return mCurrentRequest;
	}

	protected void setCurrentRequest(BleRequest mCurrentRequest) {
		this.mCurrentRequest = mCurrentRequest;
	}

	/**
	 * Send {@link ServiceBroadcast#BLE_CHARACTERISTIC_NOTIFICATION} broadcast. <br>
	 * <br>
	 * Data in the broadcast intent: <br>
	 * {@link ServiceBroadcast#EXTRA_ADDR} device address {@link String} <br>
	 * {@link ServiceBroadcast#EXTRA_UUID} characteristic uuid {@link String}<br>
	 * 
	 * @param address
	 * @param uuid
	 */
	protected void bleCharacteristicNotification(String address, UUID uuid,
			boolean isEnabled) {
		Intent intent = new Intent(ServiceBroadcast.BLE_CHARACTERISTIC_NOTIFICATION);
		intent.putExtra(ServiceBroadcast.EXTRA_ADDR, address);
		intent.putExtra(ServiceBroadcast.EXTRA_UUID, uuid);
		intent.putExtra(ServiceBroadcast.EXTRA_VALUE, isEnabled);
		sendBroadcast(intent);
		if (isEnabled) {
			requestProcessed(address, BleRequest.RequestType.CHARACTERISTIC_NOTIFICATION,
					true);
		} else {
			requestProcessed(address,
					BleRequest.RequestType.CHARACTERISTIC_STOP_NOTIFICATION, true);
		}
		setNotificationAddress(address);
	}

	/**
	 * Send {@link ServiceBroadcast#BLE_CHARACTERISTIC_INDICATION} broadcast. <br>
	 * <br>
	 * Data in the broadcast intent: <br>
	 * {@link ServiceBroadcast#EXTRA_ADDR} device address {@link String} <br>
	 * {@link ServiceBroadcast#EXTRA_UUID} characteristic uuid {@link String}<br>
	 * 
	 * @param address
	 * @param uuid
	 */
	protected void bleCharacteristicIndication(String address, UUID uuid) {
		Intent intent = new Intent(ServiceBroadcast.BLE_CHARACTERISTIC_INDICATION);
		intent.putExtra(ServiceBroadcast.EXTRA_ADDR, address);
		intent.putExtra(ServiceBroadcast.EXTRA_UUID, uuid);
		sendBroadcast(intent);
		requestProcessed(address, BleRequest.RequestType.CHARACTERISTIC_INDICATION, true);
		setNotificationAddress(address);
	}

	/**
	 * Send {@link ServiceBroadcast#BLE_CHARACTERISTIC_WRITE} broadcast. <br>
	 * <br>
	 * Data in the broadcast intent: <br>
	 * {@link ServiceBroadcast#EXTRA_ADDR} device address {@link String} <br>
	 * {@link ServiceBroadcast#EXTRA_UUID} characteristic uuid {@link String}<br>
	 * 
	 * @param address remote device's address
	 * @param uuid uuid of characteristic which is wrote
	 */
	protected void bleCharacteristicWrite(String address, UUID uuid) {
		Intent intent = new Intent(ServiceBroadcast.BLE_CHARACTERISTIC_WRITE);
		intent.putExtra(ServiceBroadcast.EXTRA_ADDR, address);
		intent.putExtra(ServiceBroadcast.EXTRA_UUID, uuid);
		sendBroadcast(intent);
		requestProcessed(address, BleRequest.RequestType.WRITE_CHARACTERISTIC, true);
	}

	/**
	 * Send {@link ServiceBroadcast#BLE_CHARACTERISTIC_CHANGED} broadcast. <br>
	 * <br>
	 * Data in the broadcast intent: <br>
	 * {@link ServiceBroadcast#EXTRA_ADDR} device address {@link String} <br>
	 * {@link ServiceBroadcast#EXTRA_UUID} characteristic uuid {@link String}<br>
	 * {@link ServiceBroadcast#EXTRA_VALUE} data byte[] <br>
	 * 
	 * @param address address of remote device
	 * @param uuid uuid of characteristic which is changed
	 * @param value new value
	 */
	protected void bleCharacteristicChanged(String address, UUID uuid,
			byte[] value) {
		Intent intent = new Intent(ServiceBroadcast.BLE_CHARACTERISTIC_CHANGED);
		intent.putExtra(ServiceBroadcast.EXTRA_ADDR, address);
		intent.putExtra(ServiceBroadcast.EXTRA_UUID, uuid);
		intent.putExtra(ServiceBroadcast.EXTRA_VALUE, value);
		sendBroadcast(intent);
	}

	/**
	 * @param reason
	 */
	protected void bleStatusAbnormal(String reason) {
		Intent intent = new Intent(ServiceBroadcast.BLE_STATUS_ABNORMAL);
		intent.putExtra(ServiceBroadcast.EXTRA_VALUE, reason);
		sendBroadcast(intent);
	}

	/**
	 * Sent when BLE request failed.<br>
	 * <br>
	 * Data in the broadcast intent: <br>
	 * {@link ServiceBroadcast#EXTRA_ADDR} device address {@link String} <br>
	 * {@link ServiceBroadcast#EXTRA_REQUEST} request type
	 * {@link BleRequest.RequestType} <br>
	 * {@link ServiceBroadcast#EXTRA_REASON} fail reason {@link BleRequest.FailReason} <br>
	 */
	protected void bleRequestFailed(String address, BleRequest.RequestType type,
			BleRequest.FailReason reason) {
		Intent intent = new Intent(ServiceBroadcast.BLE_REQUEST_FAILED);
		intent.putExtra(ServiceBroadcast.EXTRA_ADDR, address);
		intent.putExtra(ServiceBroadcast.EXTRA_REQUEST, type);
		intent.putExtra(ServiceBroadcast.EXTRA_REASON, reason);
		sendBroadcast(intent);
	}

	protected void bleDescriptorRead(String address,UUID uuid) {
		Intent intent = new Intent(ServiceBroadcast.BLE_DESCRIPTOR_READ);
		intent.putExtra(ServiceBroadcast.EXTRA_ADDR, address);
		intent.putExtra(ServiceBroadcast.EXTRA_UUID, uuid);
		sendBroadcast(intent);
	}

	protected void bleReadRemoteRssi(int rssi) {
		Intent intent = new Intent(ServiceBroadcast.BLE_READ_RSSI);
		intent.putExtra(ServiceBroadcast.EXTRA_RSSI, String.valueOf(rssi));
		sendBroadcast(intent);
	}

    protected void bleReliableWriteCompleted(String address){
        Intent intent = new Intent(ServiceBroadcast.BLE_RELIABLE_WRITE);
        intent.putExtra(ServiceBroadcast.EXTRA_ADDR, address);
        sendBroadcast(intent);
    }

	protected String getNotificationAddress() {
		return mNotificationAddress;
	}

	protected void setNotificationAddress(String mNotificationAddress) {
		this.mNotificationAddress = mNotificationAddress;
	}

	void disconnect(String address){
		clientBle.disconnect(address);
	}

	@Override
	public boolean onUnbind(Intent intent) {
		clientBle.close();
		return super.onUnbind(intent);
	}
}
