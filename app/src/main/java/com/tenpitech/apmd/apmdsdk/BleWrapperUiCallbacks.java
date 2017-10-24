package com.tenpitech.apmd.apmdsdk;

import java.util.List;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.util.Log;

import com.tenpitech.apmd.apmdsdk.bean.SensorObject;

public interface BleWrapperUiCallbacks {

	public void uiDeviceFound(final BluetoothDevice device, int rssi, byte[] record);
	
	public void uiDeviceConnected(final BluetoothGatt gatt,
								  final BluetoothDevice device);
	
	public void uiDeviceDisconnected(final BluetoothGatt gatt,
									 final BluetoothDevice device);
	public void uiScanFinished();

    public void uiScanStarted();
	
	public void uiAvailableServices(final BluetoothGatt gatt,
									final BluetoothDevice device,
									final List<BluetoothGattService> services);
	
	public void uiCharacteristicForService(final BluetoothGatt gatt,
										   final BluetoothDevice device,
										   final BluetoothGattService service,
										   final List<BluetoothGattCharacteristic> chars);

	public void uiCharacteristicsDetails(final BluetoothGatt gatt,
										 final BluetoothDevice device,
										 final BluetoothGattService service,
										 final BluetoothGattCharacteristic characteristic);
	
	public void uiNewValueForCharacteristic(final BluetoothGatt gatt,
											final BluetoothDevice device,
											final BluetoothGattService service,
											final BluetoothGattCharacteristic ch,
											final String strValue,
											final int intValue,
											final byte[] rawValue,
											final String timestamp);

	/*sisreeksha*/
	public void uiNewValueForCharacteristicWithSensorObject(final BluetoothGatt gatt,
															final BluetoothDevice device,
															final BluetoothGattService service,
															final BluetoothGattCharacteristic ch,
															final List<SensorObject> sensorObjects,
															final int intValue,
															final byte[] rawValue,
															final String timestamp);
	
	public void uiGotNotification(final BluetoothGatt gatt,
								  final BluetoothDevice device,
								  final BluetoothGattService service,
								  final BluetoothGattCharacteristic characteristic);
	
	public void uiSuccessfulWrite(final BluetoothGatt gatt,
								  final BluetoothDevice device,
								  final BluetoothGattService service,
								  final BluetoothGattCharacteristic ch,
								  final String description);
	
	public void uiFailedWrite(final BluetoothGatt gatt,
							  final BluetoothDevice device,
							  final BluetoothGattService service,
							  final BluetoothGattCharacteristic ch,
							  final String description);
	
	public void uiNewRssiAvailable(final BluetoothGatt gatt, final BluetoothDevice device, final int rssi);

    public void uiServicesDiscovered(final BluetoothGatt gatt, final BluetoothGattService service) ;

    public void uiIsNotificationEnabled(final boolean enable);
	
	/* define Null Adapter class for that interface */
	public static class Null implements BleWrapperUiCallbacks {

		private static final String TAG = "BleWrapUiCallbacks.Null";

		@Override
		public void uiDeviceConnected(BluetoothGatt gatt, BluetoothDevice device) {
            Log.d(TAG, "uiDeviceConnected, device: " + device.getName());
		}
		@Override
		public void uiDeviceDisconnected(BluetoothGatt gatt, BluetoothDevice device) {
            Log.d(TAG, "uiDeviceDisconnected, device: " + device.getName());
        }
		@Override
		public void uiAvailableServices(BluetoothGatt gatt, BluetoothDevice device,
				List<BluetoothGattService> services) {
            Log.d(TAG, "uiAvailableServices, device: " + device.getName());
        }
		@Override
		public void uiCharacteristicForService(BluetoothGatt gatt,
				BluetoothDevice device, BluetoothGattService service,
				List<BluetoothGattCharacteristic> chars) {
            Log.d(TAG, "uiCharacteristicForService, device: " + device.getName());
        }
		@Override
		public void uiCharacteristicsDetails(BluetoothGatt gatt,
				BluetoothDevice device, BluetoothGattService service,
				BluetoothGattCharacteristic characteristic) {
            Log.d(TAG, "uiCharacteristicsDetails, device: " + device.getName());
        }
		@Override
		public void uiNewValueForCharacteristic(BluetoothGatt gatt,
				BluetoothDevice device, BluetoothGattService service,
				BluetoothGattCharacteristic ch, String strValue, int intValue,
				byte[] rawValue, String timestamp) {
            Log.d(TAG, "uiNewValueForCharacteristic, device: " + device.getName());
        }

		/*sisreeksha*/
		@Override
		public void uiNewValueForCharacteristicWithSensorObject(BluetoothGatt gatt,
																BluetoothDevice device,
																BluetoothGattService service,
																BluetoothGattCharacteristic ch,
																List<SensorObject> sensorObjects,
																int intValue,
																byte[] rawValue,
																String timestamp) {
			Log.d(TAG, "uiNewValueForCharacteristicWithSensorObject, device: " + device.getName());
		}

		@Override
		public void uiGotNotification(BluetoothGatt gatt, BluetoothDevice device,
				BluetoothGattService service,
				BluetoothGattCharacteristic characteristic) {
            Log.d(TAG, "uiGotNotification, device: " + device.getName());
        }
		@Override
		public void uiSuccessfulWrite(BluetoothGatt gatt, BluetoothDevice device,
				BluetoothGattService service, BluetoothGattCharacteristic ch,
				String description) {
            Log.d(TAG, "uiSuccessfulWrite, device: " + device.getName());
        }
		@Override
		public void uiFailedWrite(BluetoothGatt gatt, BluetoothDevice device,
				BluetoothGattService service, BluetoothGattCharacteristic ch,
				String description) {
            Log.d(TAG, "uiFailedWrite, device: " + device.getName());
        }
		@Override
		public void uiNewRssiAvailable(BluetoothGatt gatt, BluetoothDevice device,
				int rssi) {
            Log.d(TAG, "uiNewRssiAvailable, device: " + device.getName());
        }

        @Override
        public void uiServicesDiscovered(BluetoothGatt gatt, BluetoothGattService service) {
            Log.println(Log.ASSERT,TAG, "uiServicesDiscovered, device: " + gatt.getDevice().getName());
        }

        @Override
        public void uiIsNotificationEnabled(boolean enable) {
            Log.println(Log.ASSERT,TAG, "uiIsNotificationEnabled, device: " + enable);
        }

        @Override
		public void uiDeviceFound(BluetoothDevice device, int rssi, byte[] record) {
            Log.d(TAG, "uiDeviceFound, device: " + device.getName());
        }

		@Override
		public void uiScanFinished() {
			Log.println(Log.ASSERT,TAG,"uiScanFinished");
		}

        @Override
        public void uiScanStarted() {
            Log.println(Log.ASSERT,TAG,"uiScanStarted");

        }

    }
}
