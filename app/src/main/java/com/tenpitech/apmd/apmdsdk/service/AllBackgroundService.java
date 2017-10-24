package com.tenpitech.apmd.apmdsdk.service;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.IBinder;
import android.support.annotation.IntDef;
import android.util.Log;

import com.google.gson.Gson;
import com.tenpitech.apmd.apmdsdk.BleWrapperUiCallbacks;
import com.tenpitech.apmd.apmdsdk.bean.BLEDeviceObject;
import com.tenpitech.apmd.apmdsdk.bean.CurrentLocation;
import com.tenpitech.apmd.apmdsdk.bean.SensorObject;
import com.tenpitech.apmd.apmdsdk.bean.ServerCallObject;
import com.tenpitech.apmd.apmdsdk.helper.ApplicationStringHolder;
import com.tenpitech.apmd.apmdsdk.helper.LocationManagerHelper;
import com.tenpitech.apmd.apmdsdk.task.ServerCallTask;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static com.tenpitech.apmd.apmdsdk.PeripheralActivity.EXTRAS_DEVICE_ADDRESS;
import static com.tenpitech.apmd.apmdsdk.PeripheralActivity.EXTRAS_DEVICE_NAME;
import static com.tenpitech.apmd.apmdsdk.helper.ApplicationStringHolder.SENSOR_TYPE_PM_FIRST_NAME;

public class AllBackgroundService extends Service implements BleWrapperUiCallbacks {

    private static final String TAG = "AllBackgroundService";
    private String mDeviceName;
    private String mDeviceAddress;


    public AllBackgroundService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        startGPSAndUse(locationManager);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);

        return super.onStartCommand(intent, flags, startId);
    }

    //BluetoothScanning

    @Override
    public void uiDeviceFound(BluetoothDevice device, int rssi, byte[] record) {

    }

    @Override
    public void uiDeviceConnected(BluetoothGatt gatt, BluetoothDevice device) {

    }

    @Override
    public void uiDeviceDisconnected(BluetoothGatt gatt, BluetoothDevice device) {

    }

    @Override
    public void uiScanFinished() {

    }

    @Override
    public void uiScanStarted() {

    }

    @Override
    public void uiAvailableServices(BluetoothGatt gatt, BluetoothDevice device, List<BluetoothGattService> services) {

    }

    @Override
    public void uiCharacteristicForService(BluetoothGatt gatt, BluetoothDevice device, BluetoothGattService service, List<BluetoothGattCharacteristic> chars) {

    }

    @Override
    public void uiCharacteristicsDetails(BluetoothGatt gatt, BluetoothDevice device, BluetoothGattService service, BluetoothGattCharacteristic characteristic) {

    }

    @Override
    public void uiNewValueForCharacteristic(BluetoothGatt gatt, BluetoothDevice device, BluetoothGattService service, BluetoothGattCharacteristic ch, String strValue, int intValue, byte[] rawValue, String timestamp) {

    }

    @Override
    public void uiNewValueForCharacteristicWithSensorObject(BluetoothGatt gatt, BluetoothDevice device, BluetoothGattService service, BluetoothGattCharacteristic ch, List<SensorObject> sensorObjects, int intValue, byte[] rawValue, String timestamp) {

    }

    @Override
    public void uiGotNotification(BluetoothGatt gatt, BluetoothDevice device, BluetoothGattService service, BluetoothGattCharacteristic characteristic) {

    }

    @Override
    public void uiSuccessfulWrite(BluetoothGatt gatt, BluetoothDevice device, BluetoothGattService service, BluetoothGattCharacteristic ch, String description) {

    }

    @Override
    public void uiFailedWrite(BluetoothGatt gatt, BluetoothDevice device, BluetoothGattService service, BluetoothGattCharacteristic ch, String description) {

    }

    @Override
    public void uiNewRssiAvailable(BluetoothGatt gatt, BluetoothDevice device, int rssi) {

    }

    @Override
    public void uiServicesDiscovered(BluetoothGatt gatt, BluetoothGattService service) {

    }

    @Override
    public void uiIsNotificationEnabled(boolean enable) {

    }

    //Location Manager

    private LocationManagerHelper getLocationManagerHelper() {
        return LocationManagerHelper.getOrCreateInstance(this);
    }

    private void startGPSAndUse(LocationManager locationManager) {
        getLocationManagerHelper().setLocationManager(locationManager);
        getLocationManagerHelper().startUsingGPS();
    }


    private void writeValueInTable(final List<SensorObject> sensorObjects) {
        for (SensorObject sensorObject : sensorObjects) {
            ServerCallObject serverCallObject = new ServerCallObject();
            Location location = getLocationManagerHelper().getLocation();
            CurrentLocation currentLocation = new CurrentLocation(location.getLatitude(), location.getLongitude());
            serverCallObject.setLocation(currentLocation);
            BLEDeviceObject deviceObject = new BLEDeviceObject(mDeviceName, mDeviceAddress);
            serverCallObject.setBleDeviceObject(deviceObject);
            String date = new SimpleDateFormat("dd/MMM/yyyy").format(new Date());
            serverCallObject.setDate(date);
            serverCallObject.setSensorObject(sensorObject);
            Gson gson = new Gson();
            String string = gson.toJson(serverCallObject);
            Log.i(TAG, "serverCallObject string:writeValueInTable " + string);
            ServerCallTask task = new ServerCallTask(string, getApplicationContext());
            task.execute();
        }
    }
}

