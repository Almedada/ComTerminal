package com.example.comterminal;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import android.bluetooth.BluetoothSocket;
import java.util.UUID;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.util.ArrayList;


import java.util.List;
import android.os.ParcelUuid;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_BLUETOOTH_PERMISSIONS = 1;
    private BluetoothAdapter bluetoothAdapter;
    private ListView deviceListView;
    private ArrayList<BluetoothDevice> deviceList;
    private BluetoothDeviceListAdapter deviceListAdapter;

    // Лаунчер для разрешений Bluetooth
    private ActivityResultLauncher<Intent> enableBluetoothLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Инициализация Bluetooth
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        // Проверка наличия Bluetooth на устройстве
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Toast.makeText(this, "Bluetooth не поддерживается или выключен", Toast.LENGTH_LONG).show();
            return;
        }

        // Инициализация адаптера списка
        deviceList = new ArrayList<>();
        deviceListView = findViewById(R.id.deviceListView);
        deviceListAdapter = new BluetoothDeviceListAdapter(this, deviceList);
        deviceListView.setAdapter(deviceListAdapter);

        // Инициализация кнопки для включения Bluetooth
        Button enableBluetoothButton = findViewById(R.id.enableBluetoothButton);
        enableBluetoothButton.setOnClickListener(v -> {
            if (!bluetoothAdapter.isEnabled()) {
                enableBluetoothLauncher.launch(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));
            } else {
                startBluetoothScan();
            }
        });

        // Запуск лаунчера для разрешений
        enableBluetoothLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                startBluetoothScan();
            } else {
                Toast.makeText(this, "Bluetooth не был включен", Toast.LENGTH_SHORT).show();
            }
        });

        // Запрос разрешений на Bluetooth (для Android 12 и выше)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.BLUETOOTH_SCAN,
                            Manifest.permission.BLUETOOTH_CONNECT,
                            Manifest.permission.ACCESS_FINE_LOCATION
                    },
                    REQUEST_BLUETOOTH_PERMISSIONS);
        } else {
            startBluetoothScan();
        }

        // Регистрируем ресивер для получения найденных устройств
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(bluetoothReceiver, filter);

        // Обработка нажатия на устройство в списке
        deviceListView.setOnItemClickListener((parent, view, position, id) -> {
            BluetoothDevice device = deviceList.get(position); // Получаем BluetoothDevice
            if (device != null) {
                connectToDevice(device); // Подключаемся к устройству
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(bluetoothReceiver); // Отменяем регистрацию ресивера
    }

    // Ресивер для обработки найденных Bluetooth устройств
    private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null && !deviceList.contains(device)) {
                    deviceList.add(device); // Добавляем BluetoothDevice в список
                    deviceListAdapter.notifyDataSetChanged();
                }
            }
        }
    };

    // Функция для запуска сканирования устройств Bluetooth
    private void startBluetoothScan() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
            bluetoothAdapter.startDiscovery();
            Toast.makeText(this, "Начинаю сканирование устройств", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Отсутствуют разрешения для сканирования Bluetooth", Toast.LENGTH_SHORT).show();
        }
    }

    // Обработка результатов запроса разрешений
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_BLUETOOTH_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startBluetoothScan();
            } else {
                Toast.makeText(this, "Bluetooth permissions required", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Подключение к выбранному устройству
    private void connectToDevice(BluetoothDevice device) {
        try {
            // Используем стандартный UUID для SPP
            UUID sppUuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
            BluetoothSocket socket = device.createRfcommSocketToServiceRecord(sppUuid);
            socket.connect();
            Toast.makeText(this, "Подключение к устройству " + device.getName() + " успешно", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Ошибка подключения", Toast.LENGTH_SHORT).show();
        }
    }


}
