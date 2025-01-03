package com.example.comterminal;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import android.app.Activity;
import android.util.Log;
import android.bluetooth.BluetoothSocket;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class FragmentDeviceScan extends Fragment {

    private BluetoothAdapter bluetoothAdapter;
    private ArrayAdapter<String> deviceAdapter;
    private ArrayList<String> deviceList;
    private BluetoothSocket socket;

    private static final int REQUEST_BLUETOOTH_PERMISSIONS = 1;

    private ActivityResultLauncher<Intent> enableBluetoothLauncher;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_device_scan, container, false);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        deviceList = new ArrayList<>();
        deviceAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, deviceList);

        ListView listView = view.findViewById(R.id.deviceListView);
        listView.setAdapter(deviceAdapter);

        enableBluetoothLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (getActivity() != null && result.getResultCode() == Activity.RESULT_OK) {
                startBluetoothScan();
            } else {
                Toast.makeText(requireContext(), "Bluetooth не был включен", Toast.LENGTH_SHORT).show();
            }
        });

        checkAndRequestPermissions();

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        requireContext().registerReceiver(bluetoothReceiver, filter);

        listView.setOnItemClickListener((parent, view1, position, id) -> {
            String deviceInfo = deviceList.get(position);
            String deviceAddress = deviceInfo.substring(deviceInfo.indexOf("\n") + 1);
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
            connectToDevice(device);
        });

        Button repeatScanButton = view.findViewById(R.id.repeatScanButton);
        repeatScanButton.setOnClickListener(v -> startBluetoothScan());

        Button sendMessageButton = view.findViewById(R.id.sendMessageButton);
        sendMessageButton.setOnClickListener(v -> {
            if (socket != null && socket.isConnected()) {
                sendData(socket, "Dude!");
            } else {
                Toast.makeText(requireContext(), "Нет подключенного устройства", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        requireContext().unregisterReceiver(bluetoothReceiver);
    }

    // Метод для проверки и запроса разрешений
    private void checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{
                            Manifest.permission.BLUETOOTH_SCAN,
                            Manifest.permission.BLUETOOTH_CONNECT,
                            Manifest.permission.ACCESS_FINE_LOCATION
                    },
                    REQUEST_BLUETOOTH_PERMISSIONS);
        } else {
            startBluetoothScan();
        }
    }

    // Получение и обработка результатов запроса разрешений
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_BLUETOOTH_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startBluetoothScan();
            } else {
                Toast.makeText(requireContext(), "Bluetooth permissions required", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Начало сканирования Bluetooth устройств
    private void startBluetoothScan() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
            bluetoothAdapter.startDiscovery();
            deviceList.clear();
            deviceAdapter.notifyDataSetChanged();
            Toast.makeText(requireContext(), "Начинаю сканирование устройств", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(requireContext(), "Отсутствуют разрешения для сканирования Bluetooth", Toast.LENGTH_SHORT).show();
        }
    }

    // Обработчик найденных устройств Bluetooth
    private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null && !deviceList.contains(device.getName() + "\n" + device.getAddress())) {
                    deviceList.add(device.getName() + "\n" + device.getAddress());
                    deviceAdapter.notifyDataSetChanged();
                }
            }
        }
    };

    // Подключение к выбранному устройству
    private void connectToDevice(BluetoothDevice device) {
        if (socket != null && socket.isConnected()) {
            Toast.makeText(requireContext(), "Устройство уже подключено", Toast.LENGTH_SHORT).show();
            return;
        }
        new Thread(() -> {
            try {
                BluetoothSocket socket = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
                bluetoothAdapter.cancelDiscovery();
                socket.connect();
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Устройство подключено", Toast.LENGTH_SHORT).show();
                    this.socket = socket;  // Используем поле socket класса
                    ((MainActivity) requireActivity()).setBluetoothSocket(socket);
                });
            } catch (IOException e) {
                requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), "Не удалось подключиться", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    // Отправка данных через BluetoothSocket
    private void sendData(BluetoothSocket socket, String message) {
        new Thread(() -> {
            try (OutputStream outputStream = socket.getOutputStream()) {
                outputStream.write(message.getBytes());
                outputStream.flush();
                Log.d("FragmentTerminal", "Сообщение отправлено: " + message);
            } catch (IOException e) {
                Log.e("FragmentTerminal", "Ошибка при отправке данных: " + e.getMessage());
            }
        }).start();
    }
}
