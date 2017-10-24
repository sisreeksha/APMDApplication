package com.tenpitech.apmd.apmdsdk.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.tenpitech.apmd.apmdsdk.PeripheralActivity;
import com.tenpitech.apmd.apmdsdk.R;
import com.tenpitech.apmd.apmdsdk.helper.ApplicationStringHolder;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import static com.tenpitech.apmd.apmdsdk.helper.ApplicationStringHolder.SENSOR_TYPE_CO2_NAME;
import static com.tenpitech.apmd.apmdsdk.helper.ApplicationStringHolder.SENSOR_TYPE_CO_NAME;
import static com.tenpitech.apmd.apmdsdk.helper.ApplicationStringHolder.SENSOR_TYPE_HUMIDITY_NAME;
import static com.tenpitech.apmd.apmdsdk.helper.ApplicationStringHolder.SENSOR_TYPE_NO2_NAME;
import static com.tenpitech.apmd.apmdsdk.helper.ApplicationStringHolder.SENSOR_TYPE_O3_NAME;
import static com.tenpitech.apmd.apmdsdk.helper.ApplicationStringHolder.SENSOR_TYPE_PM_FIRST_NAME;
import static com.tenpitech.apmd.apmdsdk.helper.ApplicationStringHolder.SENSOR_TYPE_PM_SECOND_NAME;
import static com.tenpitech.apmd.apmdsdk.helper.ApplicationStringHolder.SENSOR_TYPE_SO2_NAME;
import static com.tenpitech.apmd.apmdsdk.helper.ApplicationStringHolder.SENSOR_TYPE_TEMPERATURE_NAME;

public class CheckDataSaved extends Service {
    public CheckDataSaved() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, final int startId) {
        TimerTask tt = new TimerTask() {
            @Override
            public void run() {
                ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(CONNECTIVITY_SERVICE);
                new PushDelayedData().doInBackground(connectivityManager);
            }
        };

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(tt, 0, ApplicationStringHolder.SENSOR_VALUE_SEND_CHECK_TIME_PERIOD);

        TimerTask tt1 = new TimerTask() {
            @Override
            public void run() {
                SharedPreferences preferences = PeripheralActivity.preferences;
                SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
                Date date = new Date();
                ArrayList<String> arrayList = new ArrayList<>();
                try {
                    long PM2 = format.parse(format.format(date)).getTime() - format.parse(preferences.getString(SENSOR_TYPE_PM_FIRST_NAME, "")).getTime();
                    long PM10 = format.parse(format.format(date)).getTime() - format.parse(preferences.getString(SENSOR_TYPE_PM_SECOND_NAME, "")).getTime();
                    long HUM = format.parse(format.format(date)).getTime() - format.parse(preferences.getString(SENSOR_TYPE_HUMIDITY_NAME, "")).getTime();
                    long TEMP = format.parse(format.format(date)).getTime() - format.parse(preferences.getString(SENSOR_TYPE_TEMPERATURE_NAME, "")).getTime();
                    long CO2 = format.parse(format.format(date)).getTime() - format.parse(preferences.getString(SENSOR_TYPE_CO2_NAME, "")).getTime();
                    long CO = format.parse(format.format(date)).getTime() - format.parse(preferences.getString(SENSOR_TYPE_CO_NAME, "")).getTime();
                    long NO2 = format.parse(format.format(date)).getTime() - format.parse(preferences.getString(SENSOR_TYPE_NO2_NAME, "")).getTime();
                    long SO2 = format.parse(format.format(date)).getTime() - format.parse(preferences.getString(SENSOR_TYPE_SO2_NAME, "")).getTime();
                    long O3 = format.parse(format.format(date)).getTime() - format.parse(preferences.getString(SENSOR_TYPE_O3_NAME, "")).getTime();
                    Log.i("TestVal", SENSOR_TYPE_PM_FIRST_NAME + " " + PM2);
                    Log.i("TestVal", SENSOR_TYPE_PM_SECOND_NAME + " " + PM10);
                    Log.i("TestVal", SENSOR_TYPE_HUMIDITY_NAME + " " + HUM);
                    Log.i("TestVal", SENSOR_TYPE_TEMPERATURE_NAME + " " + TEMP);
                    Log.i("TestVal", SENSOR_TYPE_CO2_NAME + " " + CO2);
                    Log.i("TestVal", SENSOR_TYPE_CO_NAME + " " + CO);
                    Log.i("TestVal", SENSOR_TYPE_NO2_NAME + " " + NO2);
                    Log.i("TestVal", SENSOR_TYPE_SO2_NAME + " " + SO2);
                    Log.i("TestVal", SENSOR_TYPE_O3_NAME + " " + O3);
                    if (PM2 >= ApplicationStringHolder.SENSOR_TYPE_PM_FIRST_TIME_GAP) {
                        arrayList.add(SENSOR_TYPE_PM_FIRST_NAME);
                    }
                    if (PM10 >= ApplicationStringHolder.SENSOR_TYPE_PM_SECOND_TIME_GAP) {
                        arrayList.add(SENSOR_TYPE_PM_SECOND_NAME);
                    }
                    if (HUM >= ApplicationStringHolder.SENSOR_TYPE_HUMIDITY_TIME_GAP) {
                        arrayList.add(SENSOR_TYPE_HUMIDITY_NAME);
                    }
                    if (TEMP >= ApplicationStringHolder.SENSOR_TYPE_TEMPERATURE_TIME_GAP) {
                        arrayList.add(SENSOR_TYPE_TEMPERATURE_NAME);
                    }
                    if (CO >= ApplicationStringHolder.SENSOR_TYPE_CO_TIME_GAP) {
                        arrayList.add(SENSOR_TYPE_CO_NAME);
                    }
                    if (CO2 >= ApplicationStringHolder.SENSOR_TYPE_CO2_TIME_GAP) {
                        arrayList.add(SENSOR_TYPE_CO2_NAME);
                    }
                    if (NO2 >= ApplicationStringHolder.SENSOR_TYPE_NO2_TIME_GAP) {
                        arrayList.add(SENSOR_TYPE_NO2_NAME);
                    }
                    if (SO2 >= ApplicationStringHolder.SENSOR_TYPE_SO2_TIME_GAP) {
                        arrayList.add(SENSOR_TYPE_SO2_NAME);
                    }
                    if (O3 >= ApplicationStringHolder.SENSOR_TYPE_O3_TIME_GAP) {
                        arrayList.add(SENSOR_TYPE_O3_NAME);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                for (String string : arrayList) {
                    Notification.Builder builder = new Notification.Builder(getApplicationContext());
                    builder.setSmallIcon(R.mipmap.xic_launcher);
                    builder.setContentTitle("Notify");
                    builder.setContentText(string);
                    NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    notificationManager.notify(arrayList.indexOf(string), builder.build());
                }
            }
        };
        Timer timer1 = new Timer();
        timer1.scheduleAtFixedRate(tt1, ApplicationStringHolder.SENSOR_BEEP_CHECK_TIME_DELAY, ApplicationStringHolder.SENSOR_BEEP_CHECK_TIME_PERIOD);

        return super.onStartCommand(intent, flags, startId);
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

class PushDelayedData extends AsyncTask<ConnectivityManager, Void, Void> {

    private ConnectivityManager connectivityManager;

    @Override
    protected Void doInBackground(ConnectivityManager... params) {
        NetworkInfo networkInfo = params[0].getActiveNetworkInfo();
        //Log.i("Test","PushData Started "+ApplicationStringHolder.VAL);

        if (networkInfo != null && ApplicationStringHolder.VAL > 0) {
            //Log.i("Test",networkInfo.getState()+"");
            ArrayList<String> arrayList = PeripheralActivity.databasehelper.getResult();
            if (arrayList.size() == ApplicationStringHolder.MAX_STORAGE) {
                ApplicationStringHolder.VAL = ApplicationStringHolder.MAX_STORAGE;
            }
            //Log.i("Test",arrayList.size()+"");

            //send each first row from table to sever,if success delete
            while (arrayList.size() != 0) {
                URL url = null;
                try {
                    url = new URL(ApplicationStringHolder.DELAYED_SERVER_CALL_STRING);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setRequestProperty("Content-Type", "application/json");
                    httpURLConnection.setDoOutput(true);
                    httpURLConnection.connect();
                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(httpURLConnection.getOutputStream()));
                    bw.write(arrayList.get(0));
                    bw.flush();
                    bw.close();
                    int responsecode = httpURLConnection.getResponseCode();
                    //Log.i("Test",responsecode+" val delayed");

                    if (responsecode == HttpURLConnection.HTTP_OK) {
                        PeripheralActivity.databasehelper.removeResult(arrayList.get(0));
                        arrayList.remove(arrayList.get(0));
                    }
                    httpURLConnection.disconnect();
                } catch (Exception e) {
                    //Log.i("Test",e.toString());
                }
            }

        }
        return null;
    }
}