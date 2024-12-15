package com.example.comterminal;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

public class FragmentBluetooth extends Fragment {

    private BluetoothAdapter bluetoothAdapter;
    private Button bluetoothButton;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bluetooth, container, false);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothButton = view.findViewById(R.id.bluetoothButton);

        if (bluetoothAdapter == null) {
            bluetoothButton.setText("Bluetooth not supported");
            bluetoothButton.setEnabled(false);
            return view;
        }

        bluetoothButton.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                    ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 1);
            } else {
                toggleBluetooth();
            }
        });

        return view;
    }

    private void toggleBluetooth() {
        if (!bluetoothAdapter.isEnabled()) {
            startActivity(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));
        } else {
            Toast.makeText(getContext(), "Bluetooth is already enabled", Toast.LENGTH_SHORT).show();
        }
    }
}
