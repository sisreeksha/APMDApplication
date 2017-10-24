package com.tenpitech.apmd.apmdsdk.bean;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.tenpitech.apmd.apmdsdk.R;
import com.tenpitech.apmd.apmdsdk.ScanningActivity;

import java.util.List;

public class ScannedDeviceAdapter  extends BaseAdapter {

    private List<BLEDeviceObject> deviceObjects;
    private ScanningActivity activity;

    public ScannedDeviceAdapter(List<BLEDeviceObject> deviceObjects, ScanningActivity activity){
        this.deviceObjects = deviceObjects;
        this.activity = activity;
    }

    @Override
    public int getCount() {
        return deviceObjects.size();
    }

    @Override
    public Object getItem(int position) {
        return deviceObjects.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View eachListItemView = convertView;
        if (convertView == null)
            eachListItemView = ((LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.scanned_device_list_item, null);

        BLEDeviceObject deviceObject = deviceObjects.get(position);

        TextView deviceAddress = (TextView) eachListItemView.findViewById(R.id.device_address);
        TextView deviceName = (TextView) eachListItemView.findViewById(R.id.device_name);
        deviceAddress.setText(deviceObject.getDeviceAddress());
        deviceName.setText(deviceObject.getDeviceName());

        return eachListItemView;
    }
}
