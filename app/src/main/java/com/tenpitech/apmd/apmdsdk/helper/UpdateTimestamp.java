package com.tenpitech.apmd.apmdsdk.helper;

import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.tenpitech.apmd.apmdsdk.PeripheralActivity;
import com.tenpitech.apmd.apmdsdk.bean.ServerCallObject;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.tenpitech.apmd.apmdsdk.helper.ApplicationStringHolder.SENSOR_TYPE_CO2_NAME;
import static com.tenpitech.apmd.apmdsdk.helper.ApplicationStringHolder.SENSOR_TYPE_CO_NAME;
import static com.tenpitech.apmd.apmdsdk.helper.ApplicationStringHolder.SENSOR_TYPE_HUMIDITY_NAME;
import static com.tenpitech.apmd.apmdsdk.helper.ApplicationStringHolder.SENSOR_TYPE_NO2_NAME;
import static com.tenpitech.apmd.apmdsdk.helper.ApplicationStringHolder.SENSOR_TYPE_O3_NAME;
import static com.tenpitech.apmd.apmdsdk.helper.ApplicationStringHolder.SENSOR_TYPE_PM_FIRST_NAME;
import static com.tenpitech.apmd.apmdsdk.helper.ApplicationStringHolder.SENSOR_TYPE_PM_SECOND_NAME;
import static com.tenpitech.apmd.apmdsdk.helper.ApplicationStringHolder.SENSOR_TYPE_SO2_NAME;
import static com.tenpitech.apmd.apmdsdk.helper.ApplicationStringHolder.SENSOR_TYPE_TEMPERATURE_NAME;

/**
 * Created by rishav on 11/10/17.
 */

public class UpdateTimestamp {
    public static void sendValues(String string){
        Gson gson=new Gson();
        ServerCallObject callObject=gson.fromJson(string,ServerCallObject.class);
        extractValuesAndUpdate(callObject.getSensorObject().getSensorType());
    }

    public static void extractValuesAndUpdate(String string){
        SimpleDateFormat dateFormat=new SimpleDateFormat("HH:mm:ss");
        Date date=new Date();
        SharedPreferences.Editor editor= PeripheralActivity.preferences.edit();

        if(string.compareTo(SENSOR_TYPE_PM_FIRST_NAME)==0)
            editor.putString(SENSOR_TYPE_PM_FIRST_NAME,dateFormat.format(date).toString());
        if(string.compareTo(SENSOR_TYPE_PM_SECOND_NAME)==0)
            editor.putString(SENSOR_TYPE_PM_SECOND_NAME,dateFormat.format(date).toString());
        if(string.compareTo(SENSOR_TYPE_HUMIDITY_NAME)==0)
            editor.putString(SENSOR_TYPE_HUMIDITY_NAME,dateFormat.format(date).toString());
        if(string.compareTo(SENSOR_TYPE_TEMPERATURE_NAME)==0)
            editor.putString(SENSOR_TYPE_TEMPERATURE_NAME,dateFormat.format(date).toString());
        if(string.compareTo(SENSOR_TYPE_CO_NAME)==0)
            editor.putString(SENSOR_TYPE_CO_NAME,dateFormat.format(date).toString());
        if(string.compareTo(SENSOR_TYPE_CO2_NAME)==0)
            editor.putString(SENSOR_TYPE_CO2_NAME,dateFormat.format(date).toString());
        if(string.compareTo(SENSOR_TYPE_SO2_NAME)==0)
            editor.putString(SENSOR_TYPE_SO2_NAME,dateFormat.format(date).toString());
        if(string.compareTo(SENSOR_TYPE_NO2_NAME)==0)
            editor.putString(SENSOR_TYPE_NO2_NAME,dateFormat.format(date).toString());
        if(string.compareTo(SENSOR_TYPE_O3_NAME)==0)
            editor.putString(SENSOR_TYPE_O3_NAME,dateFormat.format(date).toString());
        editor.apply();
    }
}
