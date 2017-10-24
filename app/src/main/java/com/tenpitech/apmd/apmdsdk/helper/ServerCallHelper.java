package com.tenpitech.apmd.apmdsdk.helper;

import android.util.Log;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import javax.net.ssl.HttpsURLConnection;

public class ServerCallHelper {

    private String TAG = ServerCallHelper.class.getName();

    private HttpResponse getResponse(String serviceCallString, String serverCallURL) {
        HttpResponse response = null;

        HttpClient httpClient = new DefaultHttpClient();
        // replace with your url
        HttpPost httpPost = new HttpPost(serverCallURL);

        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", "application/json");

        try {
            StringEntity se = new StringEntity(serviceCallString);
            httpPost.setEntity(se);

        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, " UnsupportedException: " + e.getMessage());
        }

        try {
            response = httpClient.execute(httpPost);
        } catch (ClientProtocolException e) {
            Log.e(TAG, " ClientProtocolEx: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            Log.e(TAG, " IOException: " + e.getMessage());
        }
        return response;
    }

    public boolean makePostRequest(String serviceCallString, String serverCallURL) {
        Log.i(TAG, "In SERVICE CALL");
        boolean success = false;
        //making POST request.
        HttpResponse response = getResponse(serviceCallString, serverCallURL);
        if (response != null) {
            int responseStatusCode = response.getStatusLine().getStatusCode();
            if (responseStatusCode == HttpsURLConnection.HTTP_OK)
                success = true;
            // write response to log
        }
        return success;
    }
}