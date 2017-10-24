package com.tenpitech.apmd.apmdsdk;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.tenpitech.apmd.apmdsdk.bean.BLEDeviceObject;
import com.tenpitech.apmd.apmdsdk.bean.CurrentLocation;
import com.tenpitech.apmd.apmdsdk.bean.sensorlimit.COSensorLimitValues;
import com.tenpitech.apmd.apmdsdk.bean.sensorlimit.FirstPMSensorLimitValues;
import com.tenpitech.apmd.apmdsdk.bean.sensorlimit.HumiditySensorLimitValues;
import com.tenpitech.apmd.apmdsdk.bean.sensorlimit.NO2SensorLimitValues;
import com.tenpitech.apmd.apmdsdk.bean.sensorlimit.O3SensorLimitValues;
import com.tenpitech.apmd.apmdsdk.bean.sensorlimit.SO2SensorLimitValues;
import com.tenpitech.apmd.apmdsdk.bean.sensorlimit.SecondPMSensorLimitValues;
import com.tenpitech.apmd.apmdsdk.bean.sensorlimit.SensorLimitValues;
import com.tenpitech.apmd.apmdsdk.bean.SensorObject;
import com.tenpitech.apmd.apmdsdk.bean.ServerCallObject;
import com.tenpitech.apmd.apmdsdk.helper.ApplicationStringHolder;
import com.tenpitech.apmd.apmdsdk.helper.DatabaseHelper;
import com.tenpitech.apmd.apmdsdk.helper.LocationManagerHelper;
import com.tenpitech.apmd.apmdsdk.helper.NetEnableManager;
import com.tenpitech.apmd.apmdsdk.service.AllBackgroundService;
import com.tenpitech.apmd.apmdsdk.service.CheckDataSaved;
import com.tenpitech.apmd.apmdsdk.task.ServerCallTask;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static com.tenpitech.apmd.apmdsdk.helper.ApplicationStringHolder.SENSOR_TYPE_CO2_NAME;
import static com.tenpitech.apmd.apmdsdk.helper.ApplicationStringHolder.SENSOR_TYPE_CO_NAME;
import static com.tenpitech.apmd.apmdsdk.helper.ApplicationStringHolder.SENSOR_TYPE_HUMIDITY_NAME;
import static com.tenpitech.apmd.apmdsdk.helper.ApplicationStringHolder.SENSOR_TYPE_NO2_NAME;
import static com.tenpitech.apmd.apmdsdk.helper.ApplicationStringHolder.SENSOR_TYPE_O3_NAME;
import static com.tenpitech.apmd.apmdsdk.helper.ApplicationStringHolder.SENSOR_TYPE_PM_FIRST_NAME;
import static com.tenpitech.apmd.apmdsdk.helper.ApplicationStringHolder.SENSOR_TYPE_PM_SECOND_NAME;
import static com.tenpitech.apmd.apmdsdk.helper.ApplicationStringHolder.SENSOR_TYPE_SO2_NAME;
import static com.tenpitech.apmd.apmdsdk.helper.ApplicationStringHolder.SENSOR_TYPE_TEMPERATURE_NAME;

public class PeripheralActivity extends AppCompatActivity implements BleWrapperUiCallbacks {
    public static final String EXTRAS_DEVICE_NAME = "BLE_DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "BLE_DEVICE_ADDRESS";
    public static final String EXTRAS_DEVICE_RSSI = "BLE_DEVICE_RSSI";
    public static DatabaseHelper databasehelper;
    public static ConnectivityManager connectivityManager;
    public static String TIMESTAMP = "timestamp";
    public static SharedPreferences preferences;

    private static final String TAG = "PeripheralActivity";
    private final String PRINT_TAB = "     ";

    private String mDeviceName;
    private String mDeviceAddress;
    private String mDeviceRSSI;
    private boolean gotNotification = false;

    private BleWrapper mBleWrapper;

    /*private TextView mDeviceNameView;
    private TextView mDeviceAddressView;
    private TextView mDeviceRssiView;
    private TextView mDeviceStatus;
    private TextView mCharacterisics*/;

    private TextView mDeviceStatus;

    private TextView firstPMSensorValue, firstPMSensorUnit, firstPMSensorTime;
    private TextView secondPMSensorValue, secondPMSensorUnit, secondPMSensorTime;
    private TextView humiditySensorValue, humiditySensorUnit, humiditySensorTime;
    private TextView tempSensorValue, tempSensorUnit, tempSensorTime;
    private TextView coSensorValue, coSensorUnit, coSensorTime;
    private TextView co2SensorValue, co2SensorUnit, co2SensorTime;
    private TextView no2SensorValue, no2SensorUnit, no2SensorTime;
    private TextView so2SensorValue, so2SensorUnit, so2SensorTime;
    private TextView o3SensorValue, o3SensorUnit, o3SensorTime;

    private LinearLayout firstPMSensorLinearLayout, secondPMSensorLinearLayout, humiditySensorLinearLayout,
            tempSensorLinearLayout, coSensorLinearLayout, co2SensorLinearLayout, no2SensorLinearLayout,
            so2SensorLinearLayout, o3SensorLinearLayout;


    //private ListView mListView;
    private View mListViewHeader;
    private Switch aSwitch;
    private BluetoothGattService gattService;

    private NetEnableManager netEnableManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.peripheral_activity_2);
        stopService(new Intent(getApplicationContext(), AllBackgroundService.class));

        Intent intent = getIntent();
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);

        checkSecurityForLocationService();

        initiateUIElement();

        if (mBleWrapper == null) {
            Log.i(TAG, "mBleWrapper initializing");
            mBleWrapper = new BleWrapper(this, (BleWrapperUiCallbacks) this);
        }
        if (mBleWrapper.initialize() == false) {
            finish();
        }


        databasehelper = new DatabaseHelper(getApplicationContext());
        ApplicationStringHolder.VAL = 0;
        startService(new Intent(getApplication(), CheckDataSaved.class));

        connectivityManager = (ConnectivityManager) this.getSystemService(CONNECTIVITY_SERVICE);

        //saving sensor data in shared preference to check beep time gap
        preferences = getSharedPreferences(TIMESTAMP, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date();
        editor.putString(SENSOR_TYPE_PM_FIRST_NAME, simpleDateFormat.format(date).toString());
        editor.putString(SENSOR_TYPE_PM_SECOND_NAME, simpleDateFormat.format(date).toString());
        editor.putString(SENSOR_TYPE_HUMIDITY_NAME, simpleDateFormat.format(date).toString());
        editor.putString(SENSOR_TYPE_TEMPERATURE_NAME, simpleDateFormat.format(date).toString());
        editor.putString(SENSOR_TYPE_CO2_NAME, simpleDateFormat.format(date).toString());
        editor.putString(SENSOR_TYPE_CO_NAME, simpleDateFormat.format(date).toString());
        editor.putString(SENSOR_TYPE_NO2_NAME, simpleDateFormat.format(date).toString());
        editor.putString(SENSOR_TYPE_SO2_NAME, simpleDateFormat.format(date).toString());
        editor.putString(SENSOR_TYPE_O3_NAME, simpleDateFormat.format(date).toString());
        editor.apply();

        //Toast.makeText(getApplicationContext(),simpleDateFormat.format(date).toString(),Toast.LENGTH_SHORT).show();
    }

    private void initiateUIElement() {
        /*mDeviceAddressView = (TextView) findViewById(R.id.device_address);
        mDeviceNameView = (TextView) findViewById(R.id.device_name);
        mDeviceRssiView = (TextView) findViewById(R.id.device_rssi);*/
        mDeviceStatus = (TextView) findViewById(R.id.device_status);

        getSupportActionBar().setTitle(mDeviceName);
        //mListView = (ListView) findViewById(R.id.list_datas);

        /*mCharacterisics = (TextView) findViewById(R.id.characteristics_value);
        mCharacterisics.setMovementMethod(new ScrollingMovementMethod());*/

        firstPMSensorValue = (TextView) findViewById(R.id.first_pm_sensor_value);
        firstPMSensorUnit = (TextView) findViewById(R.id.first_pm_sensor_unit);
        firstPMSensorTime = (TextView) findViewById(R.id.first_pm_sensor_time);
        firstPMSensorLinearLayout = (LinearLayout) findViewById(R.id.first_pm_sensor_layout);

        secondPMSensorValue = (TextView) findViewById(R.id.second_pm_sensor_value);
        secondPMSensorUnit = (TextView) findViewById(R.id.second_pm_sensor_unit);
        secondPMSensorTime = (TextView) findViewById(R.id.second_pm_sensor_time);
        secondPMSensorLinearLayout = (LinearLayout) findViewById(R.id.second_pm_sensor_layout);

        humiditySensorValue = (TextView) findViewById(R.id.humidity_sensor_value);
        humiditySensorUnit = (TextView) findViewById(R.id.humidity_sensor_unit);
        humiditySensorTime = (TextView) findViewById(R.id.humidity_sensor_time);
        humiditySensorLinearLayout = (LinearLayout) findViewById(R.id.humidity_sensor_layout);

        tempSensorValue = (TextView) findViewById(R.id.temperature_sensor_value);
        tempSensorUnit = (TextView) findViewById(R.id.temperature_sensor_unit);
        tempSensorTime = (TextView) findViewById(R.id.temperature_sensor_time);
        tempSensorLinearLayout = (LinearLayout) findViewById(R.id.temperature_sensor_layout);

        coSensorValue = (TextView) findViewById(R.id.co_sensor_value);
        coSensorUnit = (TextView) findViewById(R.id.co_sensor_unit);
        coSensorTime = (TextView) findViewById(R.id.co_sensor_time);
        coSensorLinearLayout = (LinearLayout) findViewById(R.id.co_sensor_layout);

        co2SensorValue = (TextView) findViewById(R.id.co2_sensor_value);
        co2SensorUnit = (TextView) findViewById(R.id.co2_sensor_unit);
        co2SensorTime = (TextView) findViewById(R.id.co2_sensor_time);
        co2SensorLinearLayout = (LinearLayout) findViewById(R.id.co2_sensor_layout);

        no2SensorValue = (TextView) findViewById(R.id.no2_sensor_value);
        no2SensorUnit = (TextView) findViewById(R.id.no2_sensor_unit);
        no2SensorTime = (TextView) findViewById(R.id.no2_sensor_time);
        no2SensorLinearLayout = (LinearLayout) findViewById(R.id.no2_sensor_layout);

        so2SensorValue = (TextView) findViewById(R.id.so2_sensor_value);
        so2SensorUnit = (TextView) findViewById(R.id.so2_sensor_unit);
        so2SensorTime = (TextView) findViewById(R.id.so2_sensor_time);
        so2SensorLinearLayout = (LinearLayout) findViewById(R.id.so2_sensor_layout);

        o3SensorValue = (TextView) findViewById(R.id.o3_sensor_value);
        o3SensorUnit = (TextView) findViewById(R.id.o3_sensor_unit);
        o3SensorTime = (TextView) findViewById(R.id.o3_sensor_time);
        o3SensorLinearLayout = (LinearLayout) findViewById(R.id.o3_sensor_layout);

        /*aSwitch = (Switch) findViewById(R.id.aswitch);

        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mBleWrapper.isConnected())
                    mBleWrapper.startCharacteristicforService(gattService, isChecked);
                else if (isChecked && !mBleWrapper.isConnected()) {
                    aSwitch.setChecked(false);
                }

            }
        });*/
    }

    @Override
    protected void onResume() {
        super.onResume();
        netEnableManager = new NetEnableManager(this, PeripheralActivity.this);
        boolean isNetEnabled = netEnableManager.enableNet();
        if (!isNetEnabled)
            netEnableManager.showAlert();

        // start automatically connecting to the device
        //mDeviceStatus.setText("connecting ...");
        /*mDeviceAddressView.setText(mDeviceAddress);
        mDeviceNameView.setText(mDeviceName);*/
        //mBleWrapper.connect(mDeviceAddress);//LLLLLLLOOOOOOOOOOOOOOOOOOOOKKKK
        Log.println(Log.ASSERT, TAG, "uiwrapper onresume= " + mBleWrapper + " device address: " + mDeviceAddress + " connect: " + mBleWrapper.connect(mDeviceAddress));


        //Log.i(TAG, "is ble connected: " + mBleWrapper.isConnected());
        startListeningToBeep();
    }

    private void startListeningToBeep() {
        if (!mBleWrapper.isConnected()) {
            try {
                Thread.sleep(1000);
                startListeningToBeep();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            mBleWrapper.startCharacteristicforService(gattService, true);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        gotoScanningActivity();
    }

    public void gotoScanningActivity() {
        mBleWrapper.diconnect();
        Intent intent = new Intent(this, ScanningActivity.class);
        startActivity(intent);
        finish();
    }

    /*sisreeksha*/
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case ApplicationStringHolder.ACCESS_FINE_LOCATION:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    startGPSAndUse(locationManager);
                }
            default:
                break;
        }
    }

    private void checkSecurityForLocationService() {
        int locationPermissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (locationPermissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, ApplicationStringHolder.ACCESS_FINE_LOCATION);
        } else {
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            startGPSAndUse(locationManager);
        }
    }

    private void startGPSAndUse(LocationManager locationManager) {
        getLocationManagerHelper().setLocationManager(locationManager);
        getLocationManagerHelper().startUsingGPS();
    }

//    @Override
//    public boolean onPrepareOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.peripheral_menu, menu);
//        if(mBleWrapper.isConnected())
//        {
//            menu.findItem(R.id.peripheral_connect).setVisible(true);
//            menu.findItem(R.id.peripheral_disconnect).setVisible(false);
//        }
//        else {
//
//            menu.findItem(R.id.peripheral_connect).setVisible(false);
//            menu.findItem(R.id.peripheral_disconnect).setVisible(true);
//        }
//        return true;
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.peripheral_menu, menu);
        if (mBleWrapper.isConnected()) {
            menu.findItem(R.id.peripheral_connect).setVisible(false);
            menu.findItem(R.id.peripheral_disconnect).setVisible(true);
        } else {

            menu.findItem(R.id.peripheral_connect).setVisible(true);
            menu.findItem(R.id.peripheral_disconnect).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.peripheral_connect:
                mBleWrapper.connect(mDeviceAddress);
                break;
            case R.id.peripheral_disconnect:
                //mBleWrapper.close();
                mBleWrapper.diconnect();
                break;
        }
        return true;
    }

    @Override
    public void uiDeviceFound(BluetoothDevice device, int rssi, byte[] record) {
        Log.d(TAG, "uiDeviceFound, device: " + device.getName());

    }

    @Override
    public void uiDeviceConnected(BluetoothGatt gatt, BluetoothDevice device) {

        Log.println(Log.ASSERT, TAG, "uiDeviceConnected, device: " + device.getName());
        if (gatt.getDevice().getAddress().equals(mDeviceAddress)) {
            supportInvalidateOptionsMenu();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    mDeviceStatus.setText("Connected");
                    mDeviceStatus.setTextColor(getResources().getColor(R.color.md_green_700));
                }
            });
        }
    }

    @Override
    public void uiDeviceDisconnected(BluetoothGatt gatt, BluetoothDevice device) {
        if (gatt.getDevice().getAddress().equals(mDeviceAddress)) {
            supportInvalidateOptionsMenu();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    mDeviceStatus.setText("Disconnected");
                    mDeviceStatus.setTextColor(Color.RED);
                }
            });
        }
        Log.d(TAG, "uiDeviceDisconnected, device: " + device.getName());
    }

    @Override
    public void uiScanFinished() {

    }

    @Override
    public void uiScanStarted() {

    }

    @Override
    public void uiAvailableServices(BluetoothGatt gatt, BluetoothDevice device, List<BluetoothGattService> services) {
        Log.d(TAG, "uiAvailableServices, device: " + device.getName());
    }

    @Override
    public void uiCharacteristicForService(BluetoothGatt gatt, BluetoothDevice device, BluetoothGattService service, List<BluetoothGattCharacteristic> chars) {
        Log.d(TAG, "uiCharacteristicForService, device: " + device.getName());
    }

    @Override
    public void uiCharacteristicsDetails(BluetoothGatt gatt, BluetoothDevice device, BluetoothGattService service, BluetoothGattCharacteristic characteristic) {
        Log.d(TAG, "uiCharacteristicsDetails, device: " + device.getName());
    }

    @Override
    public void uiNewValueForCharacteristic(BluetoothGatt gatt, BluetoothDevice device, BluetoothGattService service, BluetoothGattCharacteristic ch, String strValue, int intValue, byte[] rawValue, String timestamp) {
        Log.d(TAG, "uiNewValueForCharacteristic, device: " + device.getName());
        Log.i(TAG, "int value: " + intValue);
        //the characteristics value is here

        appendColoredText(timestamp + PRINT_TAB + strValue + "\n", Color.GRAY);
    }

    @Override
    public void uiNewValueForCharacteristicWithSensorObject(BluetoothGatt gatt, BluetoothDevice device, BluetoothGattService service, BluetoothGattCharacteristic ch, List<SensorObject> sensorObjects, int intValue, byte[] rawValue, String timestamp) {
        Log.d(TAG, "uiNewValueForCharacteristicWithSensorObject, device: " + device.getName());
        //the characteristics value is here
        //if (netEnableManager.enableNet())
        writeValueInTable(sensorObjects);
        /*else
            netEnableManager.showAlert();*/
    }

    @Override
    public void uiGotNotification(BluetoothGatt gatt, BluetoothDevice device, BluetoothGattService service, BluetoothGattCharacteristic characteristic) {
        Log.d(TAG, "uiGotNotification, device: " + device.getName());
    }

    @Override
    public void uiSuccessfulWrite(BluetoothGatt gatt, BluetoothDevice device, BluetoothGattService service, BluetoothGattCharacteristic ch, String description) {
        Log.d(TAG, "uiSuccessfulWrite, device: " + device.getName());
    }

    @Override
    public void uiFailedWrite(BluetoothGatt gatt, BluetoothDevice device, BluetoothGattService service, BluetoothGattCharacteristic ch, String description) {
        Log.d(TAG, "uiFailedWrite, device: " + device.getName());
    }

    @Override
    public void uiNewRssiAvailable(BluetoothGatt gatt, BluetoothDevice device, final int rssi) {
        Log.d(TAG, "uiNewRssiAvailable, device: " + device.getName());
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //mDeviceRssiView.setText(String.valueOf(rssi));
            }
        });
    }

    @Override
    public void uiServicesDiscovered(BluetoothGatt gatt, BluetoothGattService service) {
        this.gattService = service;

        /*if (mBleWrapper.isConnected()){
            mBleWrapper.startCharacteristicforService(gattService, true);
        }*/

        //show gattService is discovered
        String timestamp = new SimpleDateFormat("HH:mm:ss.SSS").format(new Date());
        appendColoredText(timestamp + PRINT_TAB + "Services discovered\n", R.color.md_green_500);
    }

    @Override
    public void uiIsNotificationEnabled(boolean enable) {
        //Update ui that notification is enabled/disabled
        String timestamp = new SimpleDateFormat("HH:mm:ss.SSS").format(new Date());
        if (enable)
            appendColoredText(timestamp + PRINT_TAB + "Notification is enabled\n", R.color.md_green_400);
        else appendColoredText(timestamp + PRINT_TAB + "Notification is disabled\n", Color.RED);
    }


    /**
     * a custom helper method for coloured text in the characteristic textview
     */

    private LocationManagerHelper getLocationManagerHelper() {
        return LocationManagerHelper.getOrCreateInstance(this);
    }

    public void appendColoredText(final String text, final int color) {
        /*runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int start = mCharacterisics.getText().length();
                mCharacterisics.append(text);
                int end = mCharacterisics.getText().length();
                Log.println(Log.ASSERT, TAG, "hello");
                Spannable spannableText = (Spannable) mCharacterisics.getText();
                spannableText.setSpan(new ForegroundColorSpan(color), start, end, 0);
            }
        });*/
    }

    private Context getContext() {
        return this;
    }

    public void writeValueInTable(final List<SensorObject> sensorObjects) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
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

                    if (sensorObject.getSensorType().equals(SENSOR_TYPE_PM_FIRST_NAME)) {
                        firstPMSensorValue.setText(sensorObject.getMeasuredValue() + "");
                        firstPMSensorUnit.setText(sensorObject.getMeasurementUnit());
                        firstPMSensorTime.setText(sensorObject.getTimestamp());
                        setColorOfSensorLayout(sensorObject.getSensorType(), sensorObject.getMeasuredValue(), firstPMSensorLinearLayout);

                    } else if (sensorObject.getSensorType().equals(ApplicationStringHolder.SENSOR_TYPE_PM_SECOND_NAME)) {
                        secondPMSensorValue.setText(sensorObject.getMeasuredValue() + "");
                        secondPMSensorUnit.setText(sensorObject.getMeasurementUnit());
                        secondPMSensorTime.setText(sensorObject.getTimestamp());
                        setColorOfSensorLayout(sensorObject.getSensorType(), sensorObject.getMeasuredValue(), secondPMSensorLinearLayout);

                    } else if (sensorObject.getSensorType().equals(ApplicationStringHolder.SENSOR_TYPE_HUMIDITY_NAME)) {
                        humiditySensorValue.setText(sensorObject.getMeasuredValue() + "");
                        humiditySensorUnit.setText(sensorObject.getMeasurementUnit());
                        humiditySensorTime.setText(sensorObject.getTimestamp());
                        setColorOfSensorLayout(sensorObject.getSensorType(), sensorObject.getMeasuredValue(), humiditySensorLinearLayout);

                    } else if (sensorObject.getSensorType().equals(ApplicationStringHolder.SENSOR_TYPE_TEMPERATURE_NAME)) {
                        tempSensorValue.setText(sensorObject.getMeasuredValue() + "");
                        tempSensorUnit.setText(sensorObject.getMeasurementUnit());
                        tempSensorTime.setText(sensorObject.getTimestamp());
                        //setColorOfSensorLayout(sensorObject.getSensorType(), sensorObject.getMeasuredValue(), tempSensorLinearLayout);

                    } else if (sensorObject.getSensorType().equals(ApplicationStringHolder.SENSOR_TYPE_CO_NAME)) {
                        coSensorValue.setText(sensorObject.getMeasuredValue() + "");
                        coSensorUnit.setText(sensorObject.getMeasurementUnit());
                        coSensorTime.setText(sensorObject.getTimestamp());
                        setColorOfSensorLayout(sensorObject.getSensorType(), sensorObject.getMeasuredValue(), coSensorLinearLayout);

                    } else if (sensorObject.getSensorType().equals(ApplicationStringHolder.SENSOR_TYPE_CO2_NAME)) {
                        co2SensorValue.setText(sensorObject.getMeasuredValue() + "");
                        co2SensorUnit.setText(sensorObject.getMeasurementUnit());
                        co2SensorTime.setText(sensorObject.getTimestamp());
                        //setColorOfSensorLayout(sensorObject.getSensorType(), sensorObject.getMeasuredValue(), co2SensorLinearLayout);

                    } else if (sensorObject.getSensorType().equals(ApplicationStringHolder.SENSOR_TYPE_NO2_NAME)) {
                        no2SensorValue.setText(sensorObject.getMeasuredValue() + "");
                        no2SensorUnit.setText(sensorObject.getMeasurementUnit());
                        no2SensorTime.setText(sensorObject.getTimestamp());
                        setColorOfSensorLayout(sensorObject.getSensorType(), sensorObject.getMeasuredValue(), no2SensorLinearLayout);

                    } else if (sensorObject.getSensorType().equals(ApplicationStringHolder.SENSOR_TYPE_SO2_NAME)) {
                        so2SensorValue.setText(sensorObject.getMeasuredValue() + "");
                        so2SensorUnit.setText(sensorObject.getMeasurementUnit());
                        so2SensorTime.setText(sensorObject.getTimestamp());
                        setColorOfSensorLayout(sensorObject.getSensorType(), sensorObject.getMeasuredValue(), so2SensorLinearLayout);

                    } else if (sensorObject.getSensorType().equals(ApplicationStringHolder.SENSOR_TYPE_O3_NAME)) {
                        o3SensorValue.setText(sensorObject.getMeasuredValue() + "");
                        o3SensorUnit.setText(sensorObject.getMeasurementUnit());
                        o3SensorTime.setText(sensorObject.getTimestamp());
                        setColorOfSensorLayout(sensorObject.getSensorType(), sensorObject.getMeasuredValue(), o3SensorLinearLayout);
                    }

                    Gson gson = new Gson();
                    String string = gson.toJson(serverCallObject);
                    Log.i(TAG, "serverCallObject string:writeValueInTable " + string);
                    ServerCallTask task = new ServerCallTask(string, getContext());
                    task.execute();

                }
            }
        });
    }

    private void setColorOfSensorLayout(String sensorType, float value, LinearLayout linearLayout) {
        SensorLimitValues sensorLimitValues = null;
        if (sensorType.equals(SENSOR_TYPE_PM_FIRST_NAME))
            sensorLimitValues = new FirstPMSensorLimitValues();
        else if (sensorType.equals(ApplicationStringHolder.SENSOR_TYPE_PM_SECOND_NAME))
            sensorLimitValues = new SecondPMSensorLimitValues();
        else if (sensorType.equals(ApplicationStringHolder.SENSOR_TYPE_HUMIDITY_NAME))
            sensorLimitValues = new HumiditySensorLimitValues();
        else if (sensorType.equals(ApplicationStringHolder.SENSOR_TYPE_NO2_NAME)) {
            sensorLimitValues = new NO2SensorLimitValues();
            value = (value * 1000);
        } else if (sensorType.equals(ApplicationStringHolder.SENSOR_TYPE_CO_NAME)) {
            sensorLimitValues = new COSensorLimitValues();
            value = (value * 1000);
        } else if (sensorType.equals(ApplicationStringHolder.SENSOR_TYPE_O3_NAME)) {
            sensorLimitValues = new O3SensorLimitValues();
            value = (value * 1000);
        } else if (sensorType.equals(ApplicationStringHolder.SENSOR_TYPE_SO2_NAME)) {
            sensorLimitValues = new SO2SensorLimitValues();
            value = (value * 1000);
        }


        if (value >= sensorLimitValues.get_good_low_value() && value <= sensorLimitValues.get_good_high_value())
            linearLayout.setBackgroundColor(getResources().getColor(R.color.sensorLinerLayoutBackgroundGood));
        else if (value >= sensorLimitValues.get_satisfactory_low_value() && value <= sensorLimitValues.get_satisfactory_high_value())
            linearLayout.setBackgroundColor(getResources().getColor(R.color.sensorLinerLayoutBackgroundSatisfactory));
        else if (value >= sensorLimitValues.get_moderate_low_value() && value <= sensorLimitValues.get_moderate_high_value())
            linearLayout.setBackgroundColor(getResources().getColor(R.color.sensorLinerLayoutBackgroundModerate));
        else if (value >= sensorLimitValues.get_poor_low_value() && value <= sensorLimitValues.get_poor_high_value())
            linearLayout.setBackgroundColor(getResources().getColor(R.color.sensorLinerLayoutBackgroundPoor));
        if (value >= sensorLimitValues.get_very_poor_low_value() && value <= sensorLimitValues.get_very_poor_high_value())
            linearLayout.setBackgroundColor(getResources().getColor(R.color.sensorLinerLayoutBackgroundVeryPoor));
        if (value >= sensorLimitValues.get_severe_low_value())
            linearLayout.setBackgroundColor(getResources().getColor(R.color.sensorLinerLayoutBackgroundSevere));

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Intent service=new Intent(getApplicationContext(),AllBackgroundService.class);
        service.putExtra(EXTRAS_DEVICE_NAME,mDeviceName);
        service.putExtra(EXTRAS_DEVICE_ADDRESS,mDeviceAddress);
        startService(service);

    }
}
