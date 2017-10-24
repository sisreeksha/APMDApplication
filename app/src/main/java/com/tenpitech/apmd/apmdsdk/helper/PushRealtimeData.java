package com.tenpitech.apmd.apmdsdk.helper;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class PushRealtimeData extends AsyncTask<String, Void, Boolean> {

    @Override
    public Boolean doInBackground(String... params) {
        boolean isSuccess=false;
        try {
            URL url = new URL(ApplicationStringHolder.SERVER_CALL_STRING);
            HttpURLConnection httpURLConnection= (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("Accept", "application/json");
            httpURLConnection.setRequestProperty("Content-type", "application/json");
            httpURLConnection.setDoOutput(true);
            httpURLConnection.connect();
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(httpURLConnection.getOutputStream()));
            bw.write(params[0]);
            bw.flush();
            bw.close();
            int responsecode=httpURLConnection.getResponseCode();
            Log.i("Test",responsecode+" val real");
            if(responsecode==HttpURLConnection.HTTP_OK){
                isSuccess=true;
            }

        }catch (Exception e){
            Log.i("Test",e.toString());
        }

        return isSuccess;
    }
}