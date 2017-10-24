package com.tenpitech.apmd.apmdsdk;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import com.tenpitech.apmd.apmdsdk.bean.SensorObject;
import com.tenpitech.apmd.apmdsdk.helper.ApplicationStringHolder;

public class BleWrapper {
    /* defines (in milliseconds) how often RSSI should be updated */
    private static final int RSSI_UPDATE_TIME_INTERVAL = 1500; // 1.5 seconds
    private static final String TAG = "BleWrapper";

    /* callback object through which we are returning results to the caller */
    private BleWrapperUiCallbacks mUiCallback = null;
    /* define NULL object for UI callbacks */
    private static final BleWrapperUiCallbacks NULL_CALLBACK = new BleWrapperUiCallbacks.Null();

    /* creates BleWrapper object, set its parent activity and callback object */
    public BleWrapper(Context parent, BleWrapperUiCallbacks callback) {
        this.mParent = parent;
        this.mUiCallback = callback;
        if (mUiCallback == null) mUiCallback = NULL_CALLBACK;
        Log.println(Log.ASSERT, TAG, "muicallback: " + mUiCallback.toString());
    }

    public BluetoothManager getManager() {
        return mBluetoothManager;
    }

    public BluetoothAdapter getAdapter() {
        return mBluetoothAdapter;
    }

    public BluetoothDevice getDevice() {
        return mBluetoothDevice;
    }

    public BluetoothDevice getDevice(String mDeviceAddress) {
        return BluetoothAdapter.getDefaultAdapter().getRemoteDevice(mDeviceAddress);
    }

    public BluetoothGatt getGatt() {
        return mBluetoothGatt;
    }

    public BluetoothGattService getCachedService() {
        return mBluetoothSelectedService;
    }

    public List<BluetoothGattService> getCachedServices() {
        return mBluetoothGattServices;
    }

    public boolean isConnected() {
        return mConnected;
    }

    /* run test and check if this device has BT and BLE hardware available */
    public boolean checkBleHardwareAvailable() {
        // First check general Bluetooth Hardware:
        // get BluetoothManager...
        final BluetoothManager manager = (BluetoothManager) mParent.getSystemService(Context.BLUETOOTH_SERVICE);
        if (manager == null) return false;
        // .. and then get adapter from manager
        final BluetoothAdapter adapter = manager.getAdapter();
        if (adapter == null) return false;

        // and then check if BT LE is also available
        boolean hasBle = mParent.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
        return hasBle;
    }


    /* before any action check if BT is turned ON and enabled for us
     * call this in onResume to be always sure that BT is ON when Your
     * application is put into the foreground */
    public boolean isBtEnabled() {
        final BluetoothManager manager = (BluetoothManager) mParent.getSystemService(Context.BLUETOOTH_SERVICE);
        if (manager == null) return false;

        final BluetoothAdapter adapter = manager.getAdapter();
        if (adapter == null) return false;

        return adapter.isEnabled();
    }

    /* start scanning for BT LE devices around */
    public void startScanning() {
        ScanSettings.Builder builder = new ScanSettings.Builder();
        Log.println(Log.ASSERT, TAG, "startScanning: " + mBluetoothAdapter);
        mUiCallback.uiScanStarted();
        if(mBluetoothAdapter.getBluetoothLeScanner() != null)
            mBluetoothAdapter.getBluetoothLeScanner().startScan(null, builder.build(), mDeviceFoundCallback);


    }

    /* stops current scanning */
    public void stopScanning() {
        if(mBluetoothAdapter.getBluetoothLeScanner() != null){
            mBluetoothAdapter.getBluetoothLeScanner().stopScan(mDeviceFoundCallback);
        }
        mUiCallback.uiScanFinished();
        Log.println(Log.ASSERT, TAG, "stopScanning:");
    }

    /* initialize BLE and get BT Manager & Adapter */
    public boolean initialize() {
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) mParent.getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                return false;
            }
        }

        if (mBluetoothAdapter == null) mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.println(Log.ASSERT, TAG, "mBluetoothAdapter is null Initialize()");
            return false;
        }
        return true;
    }

    /* connect to the device with specified address */
    public boolean connect(final String deviceAddress) {
        if (mBluetoothAdapter == null || deviceAddress == null) return false;
        mDeviceAddress = deviceAddress;

        // check if we need to connect from scratch or just reconnect to previous device
        if (mBluetoothGatt != null && mBluetoothGatt.getDevice().getAddress().equals(deviceAddress)) {
            // just reconnect
            Log.println(Log.ASSERT, TAG, "just reconnect mBlegatt: " + mBluetoothGatt);
            return mBluetoothGatt.connect();
        } else {
            // connect from scratch
            // get BluetoothDevice object for specified address
            mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(mDeviceAddress);
            if (mBluetoothDevice == null) {
                // we got wrong address - that device is not available!
                return false;
            }
            // connect with remote device
            mBluetoothGatt = mBluetoothDevice.connectGatt(mParent, false, mBleCallback);
            Log.println(Log.ASSERT, TAG, "mBlegatt: " + mBluetoothGatt);
        }
        return true;
    }

    /* disconnect the device. It is still possible to reconnect to it later with this Gatt client */
    public void diconnect() {
        if (mBluetoothGatt != null) mBluetoothGatt.disconnect();
        mUiCallback.uiDeviceDisconnected(mBluetoothGatt, mBluetoothDevice);
    }

    /* close GATT client completely */
    public void close() {
        if (mBluetoothGatt != null) mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    /* request new RSSi value for the connection*/
    public void readPeriodicalyRssiValue(final boolean repeat) {
        mTimerEnabled = repeat;
        // check if we should stop checking RSSI value
        if (mConnected == false || mBluetoothGatt == null || mTimerEnabled == false) {
            mTimerEnabled = false;
            return;
        }

        mTimerHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mBluetoothGatt == null ||
                        mBluetoothAdapter == null ||
                        mConnected == false) {
                    mTimerEnabled = false;
                    return;
                }

                // request RSSI value
                mBluetoothGatt.readRemoteRssi();
                // add call it once more in the future
                readPeriodicalyRssiValue(mTimerEnabled);
            }
        }, RSSI_UPDATE_TIME_INTERVAL);
    }

    /* starts monitoring RSSI value */
    public void startMonitoringRssiValue() {
        readPeriodicalyRssiValue(true);
    }

    /* stops monitoring of RSSI value */
    public void stopMonitoringRssiValue() {
        readPeriodicalyRssiValue(false);
    }

    /* request to discover all services available on the remote devices
     * results are delivered through callback object */
    public void startServicesDiscovery() {
        if (mBluetoothGatt != null) mBluetoothGatt.discoverServices();
    }

    /* gets services and calls UI callback to handle them
     * before calling getServices() make sure service discovery is finished! */
    public void getSupportedServices() {
        if (mBluetoothGattServices != null && mBluetoothGattServices.size() > 0)
            mBluetoothGattServices.clear();
        // keep reference to all services in local array:
        if (mBluetoothGatt != null) mBluetoothGattServices = mBluetoothGatt.getServices();

        mUiCallback.uiAvailableServices(mBluetoothGatt, mBluetoothDevice, mBluetoothGattServices);
    }

    /* get all characteristic for particular service and pass them to the UI callback */
    public void getCharacteristicsForService(final BluetoothGattService service, boolean enable) {
        Log.println(Log.ASSERT, TAG, "getCharacteristicsForService()");
        if (service == null) return;
        List<BluetoothGattCharacteristic> chars = null;

        chars = service.getCharacteristics();
        mUiCallback.uiCharacteristicForService(mBluetoothGatt, mBluetoothDevice, service, chars);
        // keep reference to the last selected service
        mBluetoothSelectedService = service;
        setNotificationForCharacteristic(chars.get(0), enable);
    }

    /* request to fetch newest value stored on the remote device for particular characteristic */
    public void requestCharacteristicValue(BluetoothGattCharacteristic ch) {
        Log.println(Log.ASSERT, TAG, "requestCharacteristicValue()");
        if (mBluetoothAdapter == null || mBluetoothGatt == null) return;

        mBluetoothGatt.readCharacteristic(ch);
        // new value available will be notified in Callback Object
    }

    /* get characteristic's value (and parse it for some types of characteristics) 
     * before calling this You should always update the value by calling requestCharacteristicValue() */
    public void getCharacteristicValue(BluetoothGattCharacteristic ch) {

        Log.println(Log.ASSERT, TAG, "getCharacteristicValue()");
        if (mBluetoothAdapter == null || mBluetoothGatt == null || ch == null) return;

        byte[] rawValue = ch.getValue();
        String strValue = null;
        Map<String, int[]> typeStringDecimalMap = null;
        int intValue = 0;

        // lets read and do real parsing of some characteristic to get meaningful value from it 
        UUID uuid = ch.getUuid();
        
        /*if(uuid.equals(BleDefinedUUIDs.Characteristic.HEART_RATE_MEASUREMENT)) { // heart rate
            // follow https://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.heart_rate_measurement.xml
        	// first check format used by the device - it is specified in bit 0 and tells us if we should ask for index 1 (and uint8) or index 2 (and uint16)
        	int index = ((rawValue[0] & 0x01) == 1) ? 2 : 1;
        	// also we need to define format
        	int format = (index == 1) ? BluetoothGattCharacteristic.FORMAT_UINT8 : BluetoothGattCharacteristic.FORMAT_UINT16;
        	// now we have everything, get the value
        	intValue = ch.getIntValue(format, index);
        	strValue = intValue + " bpm"; // it is always in bpm units
        }
        else if (uuid.equals(BleDefinedUUIDs.Characteristic.HEART_RATE_MEASUREMENT) || // manufacturer name string
        		 uuid.equals(BleDefinedUUIDs.Characteristic.MODEL_NUMBER_STRING) || // model number string)
        		 uuid.equals(BleDefinedUUIDs.Characteristic.FIRMWARE_REVISION_STRING)) // firmware revision string
        {
        	// follow https://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.manufacturer_name_string.xml etc.
        	// string value are usually simple utf8s string at index 0
        	strValue = ch.getStringValue(0);
        }
        else if(uuid.equals(BleDefinedUUIDs.Characteristic.APPEARANCE)) { // appearance
        	// follow: https://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.gap.appearance.xml
        	intValue  = ((int)rawValue[1]) << 8;
        	intValue += rawValue[0];
        	strValue = BleNamesResolver.resolveAppearance(intValue);
        }
        else if(uuid.equals(BleDefinedUUIDs.Characteristic.BODY_SENSOR_LOCATION)) { // body sensor location
        	// follow: https://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.body_sensor_location.xml
        	intValue = rawValue[0];
        	strValue = BleNamesResolver.resolveHeartRateSensorLocation(intValue);
        }
        else if(uuid.equals(BleDefinedUUIDs.Characteristic.BATTERY_LEVEL)) { // battery level
        	// follow: https://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.battery_level.xml
        	intValue = rawValue[0];
        	strValue = "" + intValue + "% battery level";
        }        
        else {*/
        // not known type of characteristic, so we need to handle this in "general" way
        // get first four bytes and transform it to integer
        intValue = 0;
        if (rawValue.length > 0) intValue = (int) rawValue[0];
        if (rawValue.length > 1) intValue = intValue + ((int) rawValue[1] << 8);
        if (rawValue.length > 2) intValue = intValue + ((int) rawValue[2] << 8);
        if (rawValue.length > 3) intValue = intValue + ((int) rawValue[3] << 8);


        if (rawValue.length > 0) {
            final StringBuilder stringBuilder = new StringBuilder(rawValue.length);

            stringBuilder.append("0x");
            for (byte byteChar : rawValue) {
                stringBuilder.append("-");
                String hexValue = String.format("%02X", byteChar);
                stringBuilder.append(hexValue);
                Log.println(Log.ASSERT, TAG, "\t stringBuilder byteChar: " + hexValue);
            }
            strValue = stringBuilder.toString();
            typeStringDecimalMap = hexToDecimal(strValue);
            Log.println(Log.ASSERT, TAG, "\t stringBuilder: " + stringBuilder.toString());
        }
        //}
        String timestamp = new SimpleDateFormat("HH:mm:ss.SSS").format(new Date());

        Log.println(Log.ASSERT, TAG, "\tTime: " + timestamp + "\trawValue: " + rawValue.toString() + "\t strValue: " + strValue);
        mUiCallback.uiNewValueForCharacteristic(mBluetoothGatt,
                mBluetoothDevice,
                mBluetoothSelectedService,
                ch,
                strValue,
                intValue,
                rawValue,
                timestamp);

        for (String typeString : typeStringDecimalMap.keySet()) {
            Log.println(Log.ASSERT, TAG, "\t for sensor  type: " + typeString + " create with value: " + strValue);

            List<SensorObject> sensorObjects = createSensorObject(typeString, typeStringDecimalMap.get(typeString));
            mUiCallback.uiNewValueForCharacteristicWithSensorObject(mBluetoothGatt, mBluetoothDevice, mBluetoothSelectedService,
                    ch, sensorObjects, intValue, rawValue, timestamp);
        }
    }

    private Map<String, int[]> hexToDecimal(String strValue) {
        Map<String, int[]> typeStringDecimalValue = new HashMap<String, int[]>();
        int[] decimalValue = new int[2];
        String[] hexValues = strValue.split("-");

        String sensorType = hexValues[1];
        Log.i(TAG, hexValues[3] + " & " + hexValues[4]);

        decimalValue[0] = Integer.parseInt((hexValues[3] + hexValues[4]), 16);
        if (sensorType.equals(ApplicationStringHolder.SENSOR_TYPE_PM_VALUE)) {
            //for 01 sensor
            decimalValue[1] = Integer.parseInt((hexValues[5] + hexValues[6]), 16);
        }
        typeStringDecimalValue.put(sensorType,decimalValue);

        return typeStringDecimalValue;
    }

    private List<SensorObject> createSensorObject(String typeValue, int[] decimalValue) {
        List<SensorObject> objects = new ArrayList<SensorObject>();

        String timestamp = new SimpleDateFormat("HH:mm:ss.SSS").format(new Date());

        if (typeValue.equals(ApplicationStringHolder.SENSOR_TYPE_PM_VALUE)) {
            /*pm2.5*/
            SensorObject firstPMObject = new SensorObject();
            firstPMObject.setSensorType(ApplicationStringHolder.SENSOR_TYPE_PM_FIRST_NAME);
            Log.println(Log.ASSERT, TAG, "\t decimal value: " + decimalValue[0]);
            firstPMObject.setDecimalValue(decimalValue[0]);
            firstPMObject.setMeasuredValue((float) decimalValue[0] / 10);
            firstPMObject.setMeasurementUnit(ApplicationStringHolder.SENSOR_UNIT_UG_M3);
            firstPMObject.setTimestamp(timestamp);
            objects.add(firstPMObject);

            /*pm10*/
            SensorObject secondPMObject = new SensorObject();
            secondPMObject.setSensorType(ApplicationStringHolder.SENSOR_TYPE_PM_SECOND_NAME);
            Log.println(Log.ASSERT, TAG, "\t decimal value: " + decimalValue[1]);
            secondPMObject.setDecimalValue(decimalValue[1]);
            secondPMObject.setMeasuredValue((float)decimalValue[1] / 10);
            secondPMObject.setMeasurementUnit(ApplicationStringHolder.SENSOR_UNIT_UG_M3);
            secondPMObject.setTimestamp(timestamp);
            objects.add(secondPMObject);

        } else if (typeValue.equals(ApplicationStringHolder.SENSOR_TYPE_HUMIDITY_VALUE)) {
            /*humidity*/
            SensorObject humidityObject = new SensorObject();
            humidityObject.setSensorType(ApplicationStringHolder.SENSOR_TYPE_HUMIDITY_NAME);
            Log.println(Log.ASSERT, TAG, "\t decimal value: " + decimalValue[0]);
            humidityObject.setDecimalValue(decimalValue[0]);
            humidityObject.setMeasuredValue((float) decimalValue[0] / 10);
            humidityObject.setMeasurementUnit(ApplicationStringHolder.SENSOR_UNIT_PERCENTAGE);
            humidityObject.setTimestamp(timestamp);
            objects.add(humidityObject);

        } else if (typeValue.equals(ApplicationStringHolder.SENSOR_TYPE_TEMPERATURE_VALUE)) {
            /*temperature*/
            SensorObject temperatureObject = new SensorObject();
            temperatureObject.setSensorType(ApplicationStringHolder.SENSOR_TYPE_TEMPERATURE_NAME);
            Log.println(Log.ASSERT, TAG, "\t decimal value: " + decimalValue[0]);
            temperatureObject.setDecimalValue(decimalValue[0]);
            temperatureObject.setMeasuredValue((float)decimalValue[0] / 10);
            temperatureObject.setMeasurementUnit(ApplicationStringHolder.SENSOR_UNIT_DC);
            temperatureObject.setTimestamp(timestamp);
            objects.add(temperatureObject);

        } else if (typeValue.equals(ApplicationStringHolder.SENSOR_TYPE_CO_VALUE)) {
            /*co*/
            SensorObject coObject = new SensorObject();
            coObject.setSensorType(ApplicationStringHolder.SENSOR_TYPE_CO_NAME);
            Log.println(Log.ASSERT, TAG, "\t decimal value: " + decimalValue[0]);
            coObject.setDecimalValue(decimalValue[0]);
            coObject.setMeasuredValue((float)decimalValue[0] / 100);
            coObject.setMeasurementUnit(ApplicationStringHolder.SENSOR_UNIT_PPM);
            coObject.setTimestamp(timestamp);
            objects.add(coObject);

        } else if (typeValue.equals(ApplicationStringHolder.SENSOR_TYPE_CO2_VALUE)) {
            /*co2*/
            SensorObject co2Object = new SensorObject();
            co2Object.setSensorType(ApplicationStringHolder.SENSOR_TYPE_CO2_NAME);
            Log.println(Log.ASSERT, TAG, "\t decimal value: " + decimalValue[0]);
            co2Object.setDecimalValue(decimalValue[0]);
            co2Object.setMeasuredValue((float)decimalValue[0]/* / 1000*/);
            co2Object.setMeasurementUnit(ApplicationStringHolder.SENSOR_UNIT_PPM);
            co2Object.setTimestamp(timestamp);
            objects.add(co2Object);

        } else if (typeValue.equals(ApplicationStringHolder.SENSOR_TYPE_NO2_VALUE)) {
            /*no2*/
            SensorObject no2Object = new SensorObject();
            no2Object.setSensorType(ApplicationStringHolder.SENSOR_TYPE_NO2_NAME);
            Log.println(Log.ASSERT, TAG, "\t decimal value: " + decimalValue[0]);
            no2Object.setDecimalValue(decimalValue[0]);
            no2Object.setMeasuredValue((float)decimalValue[0] / 1000);
            no2Object.setMeasurementUnit(ApplicationStringHolder.SENSOR_UNIT_PPM);
            no2Object.setTimestamp(timestamp);
            objects.add(no2Object);

        } else if (typeValue.equals(ApplicationStringHolder.SENSOR_TYPE_SO2_VALUE)) {
            /*so2*/
            SensorObject so2Object = new SensorObject();
            so2Object.setSensorType(ApplicationStringHolder.SENSOR_TYPE_SO2_NAME);
            Log.println(Log.ASSERT, TAG, "\t decimal value: " + decimalValue[0]);
            so2Object.setDecimalValue(decimalValue[0]);
            so2Object.setMeasuredValue((float)decimalValue[0] / 1000);
            so2Object.setMeasurementUnit(ApplicationStringHolder.SENSOR_UNIT_PPM);
            so2Object.setTimestamp(timestamp);
            objects.add(so2Object);

        } else if (typeValue.equals(ApplicationStringHolder.SENSOR_TYPE_O3_VALUE)) {
            /*o3*/
            SensorObject o3Object = new SensorObject();
            o3Object.setSensorType(ApplicationStringHolder.SENSOR_TYPE_O3_NAME);
            Log.println(Log.ASSERT, TAG, "\t decimal value: " + decimalValue[0]);
            o3Object.setDecimalValue(decimalValue[0]);
            o3Object.setMeasuredValue((float)decimalValue[0] / 1000);
            o3Object.setMeasurementUnit(ApplicationStringHolder.SENSOR_UNIT_PPM);
            o3Object.setTimestamp(timestamp);
            objects.add(o3Object);
        }

        return objects;
    }

    /* reads and return what what FORMAT is indicated by characteristic's properties
     * seems that value makes no sense in most cases */
    public int getValueFormat(BluetoothGattCharacteristic ch) {
        Log.println(Log.ASSERT, TAG, "getValueFormat()");
        int properties = ch.getProperties();

        if ((BluetoothGattCharacteristic.FORMAT_FLOAT & properties) != 0)
            return BluetoothGattCharacteristic.FORMAT_FLOAT;
        if ((BluetoothGattCharacteristic.FORMAT_SFLOAT & properties) != 0)
            return BluetoothGattCharacteristic.FORMAT_SFLOAT;
        if ((BluetoothGattCharacteristic.FORMAT_SINT16 & properties) != 0)
            return BluetoothGattCharacteristic.FORMAT_SINT16;
        if ((BluetoothGattCharacteristic.FORMAT_SINT32 & properties) != 0)
            return BluetoothGattCharacteristic.FORMAT_SINT32;
        if ((BluetoothGattCharacteristic.FORMAT_SINT8 & properties) != 0)
            return BluetoothGattCharacteristic.FORMAT_SINT8;
        if ((BluetoothGattCharacteristic.FORMAT_UINT16 & properties) != 0)
            return BluetoothGattCharacteristic.FORMAT_UINT16;
        if ((BluetoothGattCharacteristic.FORMAT_UINT32 & properties) != 0)
            return BluetoothGattCharacteristic.FORMAT_UINT32;
        if ((BluetoothGattCharacteristic.FORMAT_UINT8 & properties) != 0)
            return BluetoothGattCharacteristic.FORMAT_UINT8;

        return 0;
    }

    /* set new value for particular characteristic */
    public void writeDataToCharacteristic(final BluetoothGattCharacteristic ch, final byte[] dataToWrite) {
        Log.println(Log.ASSERT, TAG, "writeDataToCharacteristic()");
        if (mBluetoothAdapter == null || mBluetoothGatt == null || ch == null) return;

        // first set it locally....
        ch.setValue(dataToWrite);
        // ... and then "commit" changes to the peripheral
        mBluetoothGatt.writeCharacteristic(ch);
    }

    /* enables/disables notification for characteristic */
    public void setNotificationForCharacteristic(BluetoothGattCharacteristic ch, boolean enabled) {
        Log.println(Log.ASSERT, TAG, "setNotificationForCharacteristic()");
        if (mBluetoothAdapter == null || mBluetoothGatt == null) return;

        boolean success = mBluetoothGatt.setCharacteristicNotification(ch, enabled);
        if (!success) {
            Log.e("------", "Seting proper notification status for characteristic failed!");
        }

        // This is also sometimes required (e.g. for heart rate monitors) to enable notifications/indications
        // see: https://developer.bluetooth.org/gatt/descriptors/Pages/DescriptorViewer.aspx?u=org.bluetooth.descriptor.gatt.client_characteristic_configuration.xml
        BluetoothGattDescriptor descriptor = ch.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
        if (descriptor != null) {
            byte[] val = enabled ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE : BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;
            descriptor.setValue(val);
            mBluetoothGatt.writeDescriptor(descriptor);
            mUiCallback.uiIsNotificationEnabled(enabled);
        }
    }


    private ScanCallback mDeviceFoundCallback = new ScanCallback() {
        public void onScanResult(int callbackType, ScanResult result) {
            mUiCallback.uiDeviceFound(result.getDevice(), result.getRssi(), result.getScanRecord().getBytes());
            Log.println(Log.ASSERT, TAG, "onScanResult");
        }

        /**
         * Callback when batch results are delivered.
         *
         * @param results List of scan results that are previously scanned.
         */
        public void onBatchScanResults(List<ScanResult> results) {
        }

        /**
         * Callback when scan could not be started.
         *
         * @param errorCode Error code (one of SCAN_FAILED_*) for scan failure.
         */
        public void onScanFailed(int errorCode) {
        }

    };

    /* callbacks called for any action on particular Ble Device */
    private final BluetoothGattCallback mBleCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.println(Log.ASSERT, TAG, "gatt:" + gatt + "\tstatus: " + status + "\tnewstate: " + newState);
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.println(Log.ASSERT, TAG, "Connected: " + gatt.getDevice().getName());
                mConnected = true;
                //mUiCallback.uiDeviceConnected(mBluetoothGatt, mBluetoothDevice);
                mUiCallback.uiDeviceConnected(gatt, gatt.getDevice());
                // now we can start talking with the device, e.g.
                mBluetoothGatt.readRemoteRssi();
                // response will be delivered to callback object!

                // in our case we would also like automatically to call for services discovery
                startServicesDiscovery();

                // and we also want to get RSSI value to be updated periodically
                startMonitoringRssiValue();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                mConnected = false;
                Log.println(Log.ASSERT, TAG, "Device Disconnected: " + gatt.getDevice().getName());
                mUiCallback.uiDeviceDisconnected(gatt, gatt.getDevice());
//                try {
//                    mBluetoothGatt.close();
//                } catch (Exception e) {
//                    Log.d("DDDD", "close ignoring: " + e);
//                }
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // now, when services discovery is finished, we can call getServices() for Gatt
                Log.println(Log.ASSERT, TAG, "mBleCallback, onServiceDiscovered(), Anil, should have called all supported services");
                //getSupportedServices();
                BluetoothGattService service = gatt.getService(UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb"));
                Log.println(Log.ASSERT, TAG, "service: " + service.toString());
//                getCharacteristicsForService(service);
                mUiCallback.uiServicesDiscovered(gatt, service);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            Log.println(Log.ASSERT, TAG, "onCharaceristicRead()");
            // we got response regarding our request to fetch characteristic value
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // and it success, so we can get the value
                getCharacteristicValue(characteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            Log.println(Log.ASSERT, TAG, "onCharaceristicChanged()");
            // characteristic's value was updated due to enabled notification, lets get this value
            // the value itself will be reported to the UI inside getCharacteristicValue
            getCharacteristicValue(characteristic);
            // also, notify UI that notification are enabled for particular characteristic
            mUiCallback.uiGotNotification(mBluetoothGatt, mBluetoothDevice, mBluetoothSelectedService, characteristic);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            String deviceName = gatt.getDevice().getName();

            Log.println(Log.ASSERT, TAG, "onCharaceristicWrite()");
            /*String serviceName = BleNamesResolver.resolveServiceName(characteristic.getService().getUuid().toString().toLowerCase(Locale.getDefault()));
            String charName = BleNamesResolver.resolveCharacteristicName(characteristic.getUuid().toString().toLowerCase(Locale.getDefault()));
        	String description = "Device: " + deviceName + " Service: " + serviceName + " Characteristic: " + charName;
        	
        	// we got response regarding our request to write new value to the characteristic
        	// let see if it failed or not
        	if(status == BluetoothGatt.GATT_SUCCESS) {
        		 mUiCallback.uiSuccessfulWrite(mBluetoothGatt, mBluetoothDevice, mBluetoothSelectedService, characteristic, description);
        	}
        	else {//alert.show();
        		 mUiCallback.uiFailedWrite(mBluetoothGatt, mBluetoothDevice, mBluetoothSelectedService, characteristic, description + " STATUS = " + status);
        	}*/
            Log.d(TAG, "mBleCallback, onCharacteristicWrite(), Anil, characteristic: " + characteristic);

        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);

            Log.println(Log.ASSERT, TAG, "onDescriptorRead()");
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            Log.println(Log.ASSERT, TAG, "onDescriptorWrite()" +
                    descriptor.getCharacteristic().getValue().toString());

            //check for descriptor
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // we got new value of RSSI of the connection, pass it to the UI
                mUiCallback.uiNewRssiAvailable(mBluetoothGatt, mBluetoothDevice, rssi);
            }
        }
    };

    public void startCharacteristicforService(BluetoothGattService service, boolean enable) {
        Log.println(Log.ASSERT, TAG, "service: " + service.toString());
        if (mConnected) getCharacteristicsForService(service, enable);

    }

    private Context mParent = null;
    private boolean mConnected = false;
    private String mDeviceAddress = "";

    private BluetoothManager mBluetoothManager = null;
    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothDevice mBluetoothDevice = null;
    private BluetoothGatt mBluetoothGatt = null;
    private BluetoothGattService mBluetoothSelectedService = null;
    private List<BluetoothGattService> mBluetoothGattServices = null;

    private Handler mTimerHandler = new Handler();
    private boolean mTimerEnabled = false;
}
