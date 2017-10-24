package com.tenpitech.apmd.apmdsdk.helper;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.tenpitech.apmd.apmdsdk.PeripheralActivity;
import com.tenpitech.apmd.apmdsdk.ScanningActivity;

import java.lang.reflect.Method;

public class NetEnableManager {
    private String TAG = NetEnableManager.class.getName();

    private Context context;
    private PeripheralActivity activity;

    public NetEnableManager(Context context, PeripheralActivity activity) {
        this.context = context;
        this.activity = activity;
    }

    public boolean enableNet() {
        Log.i(TAG, "enableNet");
        boolean isNetEnabled = false;
        //mobile data section
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);

        //wifi section
        WifiManager wifi = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
        boolean wifiEnabled = wifi.isWifiEnabled();
        try {
            Class cmClass = Class.forName(cm.getClass().getName());
            final Method method = cmClass.getDeclaredMethod("getMobileDataEnabled");
            method.setAccessible(true);
            // get the setting for "mobile data"
            boolean mobileDataEnabled = (Boolean) method.invoke(cm);
            Log.i(TAG, "mobileDataEnabled " + mobileDataEnabled);

            isNetEnabled = mobileDataEnabled;
            if (!isNetEnabled) {
                Log.i(TAG, "wifiEnabled " + isNetEnabled);
                if (wifiEnabled) {
                    isNetEnabled = wifiEnabled;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return isNetEnabled;
    }

    public void showAlert() {
        Log.i(TAG, "showAlert()");
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.setTitle("NETWORK settings");
        alertDialog.setMessage("Neither Mobile Data nor WIFI is not enabled. Go to settings menu");
        alertDialog.setPositiveButton("WIFI", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Intent wifiIntent = new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS);
                activity.startActivityForResult(wifiIntent, 0);
            }
        });

        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                activity.gotoScanningActivity();
            }
        });
        AlertDialog dialog = alertDialog.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }
}
