package com.tenpitech.apmd.apmdsdk.task;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.tenpitech.apmd.apmdsdk.PeripheralActivity;
import com.tenpitech.apmd.apmdsdk.helper.ApplicationStringHolder;
import com.tenpitech.apmd.apmdsdk.helper.PushRealtimeData;
import com.tenpitech.apmd.apmdsdk.helper.UpdateTimestamp;

public class ServerCallTask extends AsyncTask<Void, Void, Boolean> {
    private boolean isSuccess;
    private String string;
    private Context context;
    private AlertDialog.Builder alert;

    public ServerCallTask(String string, Context context) {
        this.string = string;
        this.context = context;
        alert = new AlertDialog.Builder(context);
        UpdateTimestamp.sendValues(string);

    }

    @Override
    protected Boolean doInBackground(Void... params) {
        //isSuccess = new ServerCallHelper().makePostRequest(string, ApplicationStringHolder.SERVER_CALL_STRING);
        PushRealtimeData pushRealtimeData=new PushRealtimeData();
        isSuccess=pushRealtimeData.doInBackground(new String[]{string}).booleanValue();
        Log.i("doInBackground", "isSuccess " + isSuccess);
        return isSuccess;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
        if (!isSuccess) {
            Log.i("onPostExecute", "failure");

            alert.setTitle("Alert");
            alert.setMessage("Could not post data to the server!! Please check if server is active.");
            alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }

            });
            //alert.show();

            if(ApplicationStringHolder.VAL==ApplicationStringHolder.MAX_STORAGE){
                ApplicationStringHolder.VAL=0;
            }
            PeripheralActivity.databasehelper.insertResult(string);

            //Toast.makeText(context, "Could not post data to the server!! Please check if server is active.", Toast.LENGTH_LONG).show();
        }
    }
}
