package com.tenpitech.apmd.apmdsdk;

//import org.bluetooth.bledemo.R;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.gson.Gson;
import com.tenpitech.apmd.apmdsdk.bean.BLEDeviceObject;
import com.tenpitech.apmd.apmdsdk.bean.ScannedDeviceAdapter;
import com.tenpitech.apmd.apmdsdk.helper.ApplicationStringHolder;
import com.tenpitech.apmd.apmdsdk.helper.LocationManagerHelper;

import java.util.ArrayList;
import java.util.List;

public class ScanningActivity extends AppCompatActivity {

    private static final long SCANNING_TIMEOUT = 5 * 1000; /* 5 seconds */
    private static final int ENABLE_BT_REQUEST_ID = 1;

    private static final String TAG = "ScanningActivity";

    private boolean mScanning = false;
    private Handler mHandler = new Handler();
    //	private DeviceListAdapter mDevicesListAdapter = null;
    private BleWrapper mBleWrapper = null;
    private List<BLEDeviceObject> deviceList = new ArrayList<BLEDeviceObject>();
    private ProgressBar progressBar;
    private Button scan;
    private ScannedDeviceAdapter mScanAdapter;
    private ListView listView;

    public final static String DEVICE_NAME = "TenPi_APMD";
    /*sisreeksha*/
    //public final static String DEVICE_NAME = "Redmi";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanning_item);
        checkSecurityForLocationService();

        // create BleWrapper with empty callback object except uiDeficeFound function (we need only that here) 
        mBleWrapper = new BleWrapper(this, new BleWrapperUiCallbacks.Null() {
            @Override
            public void uiDeviceFound(final BluetoothDevice device, final int rssi, final byte[] record) {
                handleFoundDevice(device, rssi, record);
            }

            @Override
            public void uiScanFinished() {
                showDeviceList();
                progressBar.setVisibility(View.GONE);
                scan.setText("Start Scan");
                mScanning = false;
            }

            @Override
            public void uiScanStarted() {
                progressBar.setVisibility(View.VISIBLE);
                scan.setText("Stop Scan");
                mScanning = true;
            }
        });

        // check if we have BT and BLE on board
        if (mBleWrapper.checkBleHardwareAvailable() == false) {
            bleMissing();
        }
        scan = (Button) findViewById(R.id.scan);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mScanning) {
                    deviceList.clear();
                    mBleWrapper.startScanning();
                    addScanningTimeout();
                } else {
                    mBleWrapper.stopScanning();
                }
            }
        });
        listView = (ListView) findViewById(R.id.list_datas);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                openPeripheralActivity(deviceList.get(position).getDeviceAddress(), deviceList.get(position).getDeviceName());
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // on every Resume check if BT is enabled (user could turn it off while app was in background etc.)
        if (mBleWrapper.isBtEnabled() == false) {
            // BT is not turned on - ask user to make it enabled
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, ENABLE_BT_REQUEST_ID);
            // see onActivityResult to check what is the status of our request
        }

        // initialize BleWrapper object
        mBleWrapper.initialize();

//    	mDevicesListAdapter = new DeviceListAdapter(this);
//        setListAdapter(mDevicesListAdapter);

        // Automatically start scanning for devices
        mScanning = true;
        // remember to add timeout for scanning to not run it forever and drain the battery
        addScanningTimeout();
        mBleWrapper.startScanning();

//        invalidateOptionsMenu();
    }

    ;

    @Override
    protected void onPause() {
        super.onPause();
        mScanning = false;
        mBleWrapper.stopScanning();
//    	invalidateOptionsMenu();
//
//    	mDevicesListAdapter.clearList();
    }

    ;
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.scanning, menu);
//
//        if (mScanning) {
//            menu.findItem(R.id.scanning_start).setVisible(false);
//            menu.findItem(R.id.scanning_stop).setVisible(true);
//            menu.findItem(R.id.scanning_indicator)
//                .setActionView(R.layout.progress_indicator);
//
//        } else {
//            menu.findItem(R.id.scanning_start).setVisible(true);
//            menu.findItem(R.id.scanning_stop).setVisible(false);
//            menu.findItem(R.id.scanning_indicator).setActionView(null);
//        }
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.scanning_start:
//            	mScanning = true;
//            	mBleWrapper.startScanning();
//                break;
//            case R.id.scanning_stop:
//            	mScanning = false;
//            	mBleWrapper.stopScanning();
//                break;
//        }
////
////        invalidateOptionsMenu();
//        return true;
//    }

    //
//    /* user has selected one of the device */
//    @Override
//    protected void onListItemClick(ListView l, View v, int position, long id) {
//        final BluetoothDevice device = mDevicesListAdapter.getDevice(position);
//        if (device == null) return;
//
//        final Intent intent = new Intent(this, PeripheralActivity.class);
//        intent.putExtra(PeripheralActivity.EXTRAS_DEVICE_NAME, device.getName());
//        intent.putExtra(PeripheralActivity.EXTRAS_DEVICE_ADDRESS, device.getAddress());
//        intent.putExtra(PeripheralActivity.EXTRAS_DEVICE_RSSI, mDevicesListAdapter.getRssi(position));
//
//        if (mScanning) {
//            mScanning = false;
//            invalidateOptionsMenu();
//            mBleWrapper.stopScanning();
//        }
//
//        startActivity(intent);
//    }
//
    /* check if user agreed to enable BT */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // user didn't want to turn on BT
        if (requestCode == ENABLE_BT_REQUEST_ID) {
            if (resultCode == Activity.RESULT_CANCELED) {
                btDisabled();
                return;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
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
        LocationManagerHelper.getOrCreateInstance(this).setLocationManager(locationManager);
        LocationManagerHelper.getOrCreateInstance(this).startUsingGPS();
    }



    /* make sure that potential scanning will take no longer
     * than <SCANNING_TIMEOUT> seconds from now on */
    private void addScanningTimeout() {
        Runnable timeout = new Runnable() {
            @Override
            public void run() {
                if (mBleWrapper == null) return;
                mScanning = false;
                mBleWrapper.stopScanning();
//                invalidateOptionsMenu();
            }
        };
        mHandler.postDelayed(timeout, SCANNING_TIMEOUT);
    }

    /* add device to the current list of devices */
    private void handleFoundDevice(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
        Log.println(Log.INFO, TAG, "found device: " + device.getAddress() + " name: " + device.getName());

        if (device.getName() != null && device.getName().equals(DEVICE_NAME)) {
            BLEDeviceObject deviceObject = new BLEDeviceObject(device.getName(), device.getAddress());
            Gson gson = new Gson();
            String deviceString = gson.toJson(deviceObject);

            Log.println(Log.INFO, TAG, "found device: " + deviceString);
            //mBleWrapper.connect(device.getAddress());
            if (!deviceList.contains(deviceObject)) {
                Log.println(Log.INFO, TAG, "found device with name: " + device.getName());
                deviceList.add(deviceObject);
            }

        }
        // adding to the UI have to happen in UI thread
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                mDevicesListAdapter.addDevice(device, rssi, scanRecord);
//                mDevicesListAdapter.notifyDataSetChanged();
            }
        });
    }

    private void showDeviceList() {
        if (deviceList.size() == 1) {
            //mBleWrapper.connect(deviceList.get(0));
            openPeripheralActivity(deviceList.get(0).getDeviceAddress(), DEVICE_NAME);
        }
        /*else
        {*/
        Log.i(TAG, "device list " + deviceList.size());
        //set adapter
        //mScanAdapter = new ArrayAdapter<BLEDeviceObject>(this,android.R.layout.simple_list_item_1, deviceList);
        mScanAdapter = new ScannedDeviceAdapter(deviceList, ScanningActivity.this);
        listView.setAdapter(mScanAdapter);
        mScanAdapter.notifyDataSetChanged();
        //}
    }

    private void openPeripheralActivity(String mDeviceAddress, String mDeviceName) {
        if(mDeviceName.equals(DEVICE_NAME)){
            mDeviceName = "Pollution Detection Device";
            Intent intent = new Intent(this, PeripheralActivity.class);
            intent.putExtra(PeripheralActivity.EXTRAS_DEVICE_ADDRESS, mDeviceAddress);
            intent.putExtra(PeripheralActivity.EXTRAS_DEVICE_NAME, mDeviceName);
            //mBleWrapper.connect(mDeviceAddress);
            startActivity(intent);
            finish();
        }
    }

    private void btDisabled() {
        Toast.makeText(this, "Sorry, BT has to be turned ON for us to work!", Toast.LENGTH_LONG).show();
        finish();
    }

    private void bleMissing() {
        Toast.makeText(this, "BLE Hardware is required but not available!", Toast.LENGTH_LONG).show();
        finish();
    }
}
