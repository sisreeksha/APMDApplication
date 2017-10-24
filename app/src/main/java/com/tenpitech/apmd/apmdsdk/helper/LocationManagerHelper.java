package com.tenpitech.apmd.apmdsdk.helper;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

public class LocationManagerHelper implements LocationListener {
    private String TAG = LocationManagerHelper.class.getName();
    private Context context;

    private static LocationManagerHelper instance = null;
    private LocationManager locationManager;
    private boolean isGPSEnabled, isNetworkEnabled, canGetLocation;
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0; // 10 meters
    private static final long MIN_TIME_BW_UPDATES = 0 * 1000; // 20 secs

    private LocationManagerHelper(Context context) {
        this.context = context;
    }

    public static synchronized LocationManagerHelper getOrCreateInstance(Context context) {
        if (instance == null)
            instance = new LocationManagerHelper(context);
        return instance;
    }

    public LocationManager getLocationManager() {
        return locationManager;
    }

    public void setLocationManager(LocationManager locationManager) {
        this.locationManager = locationManager;
    }

    //start the GPS monitoring
    public void startUsingGPS() {
        if (!(isNetworkEnabled || isGPSEnabled))
            checkOrEnableGPS();

        try {
            if (!canGetLocation && isGPSEnabled) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_DISTANCE_CHANGE_FOR_UPDATES, MIN_TIME_BW_UPDATES, this);
                canGetLocation = true;
            }
            if (!canGetLocation && isNetworkEnabled) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_DISTANCE_CHANGE_FOR_UPDATES, MIN_TIME_BW_UPDATES, this);
                canGetLocation = true;
            }
        } catch (Exception ex) {
            promptEnableGPS();
        }
    }

    //if GPS not enabled, show alert
    private void promptEnableGPS() {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.setTitle("GPS is settings");
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                context.startActivity(intent);
            }
        });
        AlertDialog dialog = alertDialog.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    //check if GPS & NET enabled
    public void checkOrEnableGPS() {
        try {
            if (locationManager != null) {
                isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                if (!(isNetworkEnabled || isGPSEnabled))
                    promptEnableGPS();
            }
        } catch (Exception ex) {
            Log.e(TAG, "Exception: " + ex.getMessage());
        }
    }

    public Location getLocation() {

        Location location = null;
        Log.i(TAG, "getLocation" + "canGetLocation: " + canGetLocation + " isGPSEnabled: " + isGPSEnabled + " isNetworkEnabled: " + isNetworkEnabled);

        Location locationFromGPSProvider = getLocationFromGPSProvider();
        //if(location == null)
        Location locationFromNetWorkProvider = getLocationFromNetWorkProvider();
        if (locationFromGPSProvider == null) {
            if (locationFromNetWorkProvider != null) {
                location = locationFromNetWorkProvider;
            }
        } else {
            if (locationFromNetWorkProvider != null) {
                double distanceInKm = distanceInKm(locationFromGPSProvider, locationFromNetWorkProvider);
                if (distanceInKm <= 0.01)
                    location = locationFromGPSProvider;
                else
                    location = locationFromNetWorkProvider;
            } else
                location = locationFromGPSProvider;
        }

        return location;
    }

    private double distanceInKm(Location location1, Location location2) {
        double theta = location1.getLongitude() - location2.getLongitude();
        double dist = Math.sin(deg2rad(location1.getLatitude()))
                * Math.sin(deg2rad(location2.getLatitude()))
                + Math.cos(deg2rad(location1.getLatitude()))
                * Math.cos(deg2rad(location2.getLatitude()))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        return (dist);
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }

    private Location getLocationFromGPSProvider() {

        Location location = null;

        try {
            if (canGetLocation) {
                if (isGPSEnabled) {
                    Log.d("getLocation", "In isGPSEnabled");
                    //Toast.makeText(context, "getting location from GPS", Toast.LENGTH_LONG).show();
                    location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                }
            }
        } catch (Exception ex) {
            Log.e(TAG, "Exception: " + ex.getMessage());
        }
        Log.e(TAG, "location: " + location);

        return location;
    }

    private Location getLocationFromNetWorkProvider() {

        Location location = null;

        try {
            if (canGetLocation) {
                if (isNetworkEnabled) {
                    Log.d("getLocation", "In isNetworkEnabled");
                    //Toast.makeText(context, "getting location from NET  provider", Toast.LENGTH_LONG).show();
                    location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                }
            }
        } catch (Exception ex) {
            Log.e(TAG, "Exception: " + ex.getMessage());
        }
        Log.e(TAG, "location: " + location);

        return location;
    }

    @Override
    public void onLocationChanged(Location location) {
        /*if (activity.getClass() == HomeActivity.class){
            Log.i(TAG, "getting location onLocationChanged");
            //Toast.makeText(context, "getting location onLocationChanged", Toast.LENGTH_LONG).show();
            ((HomeActivity) activity).populateLocationTextView(location);
        }*/
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {
        /*if (activity.getClass() == HomeActivity.class){
            Log.i(TAG, "getting location onLocationChanged");
            //Toast.makeText(context, "getting location onLocationChanged", Toast.LENGTH_LONG).show();
            ((HomeActivity) activity).populateLocationTextView(getLocation());
        }*/
    }

    @Override
    public void onProviderDisabled(String s) {
        promptEnableGPS();
    }
}
