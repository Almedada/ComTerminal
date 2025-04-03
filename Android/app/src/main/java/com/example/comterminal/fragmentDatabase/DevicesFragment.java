package com.example.comterminal.fragmentDatabase;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.example.comterminal.R;


import androidx.fragment.app.Fragment;

import com.example.comterminal.database.AppDatabase;
import com.example.comterminal.database.Device;

import java.util.List;

public class DevicesFragment extends Fragment {

    private AppDatabase database;
    private LinearLayout containerLayout;
    private Handler handler;

    public DevicesFragment() {
        super(R.layout.fragment_devices);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        containerLayout = view.findViewById(R.id.containerLayout);
        Button loadButton = view.findViewById(R.id.loadButton);

        handler = new Handler();
        database = AppDatabase.getInstance(requireContext());

        loadButton.setOnClickListener(v -> loadData());
    }

    private void loadData() {
        new Thread(() -> {
            List<Device> devices = database.deviceDao().getAllDevices();
            handler.post(() -> {
                containerLayout.removeAllViews();
                for (Device device : devices) {
                    addDeviceToLayout(device);
                }
            });
        }).start();
    }

    private void addDeviceToLayout(Device device) {
        String deviceInfo = device.name + " - " + device.macAddress;

        LinearLayout deviceLayout = new LinearLayout(getContext());
        deviceLayout.setOrientation(LinearLayout.HORIZONTAL);
        deviceLayout.setPadding(0, 8, 0, 8);

        TextView deviceTextView = new TextView(getContext());
        deviceTextView.setText(deviceInfo);
        deviceTextView.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        Button deleteButton = new Button(getContext());
        deleteButton.setText("Удалить");
        deleteButton.setOnClickListener(v -> deleteDevice(device));

        deviceLayout.addView(deviceTextView);
        deviceLayout.addView(deleteButton);
        containerLayout.addView(deviceLayout);
    }

    private void deleteDevice(Device device) {
        new Thread(() -> {
            database.deviceDao().delete(device);
            handler.post(() -> {
                loadData();
            });
        }).start();
    }
}
