package com.example.comterminal;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class BluetoothDeviceListAdapter extends ArrayAdapter<BluetoothDevice> {

    public BluetoothDeviceListAdapter(Context context, List<BluetoothDevice> devices) {
        super(context, android.R.layout.simple_list_item_1, devices);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        BluetoothDevice device = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        }

        TextView textView = convertView.findViewById(android.R.id.text1);

        if (device != null) {
            String deviceName = device.getName();
            if (deviceName != null && !deviceName.isEmpty()) {
                textView.setText(deviceName);
            } else {
                textView.setText("Unknown device");
            }
        }

        return convertView;
    }
}
