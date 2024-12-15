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
import android.view.LayoutInflater;  // Импортируем LayoutInflater
import android.view.View;  // Импортируем View
import android.view.ViewGroup;  // Импортируем ViewGroup
public class FragmentDeviceScan extends Fragment {

    private BluetoothAdapter bluetoothAdapter;
    private ArrayAdapter<String> deviceAdapter;
    private ArrayList<String> deviceList;
    private BluetoothSocket socket;

    private static final int REQUEST_BLUETOOTH_PERMISSIONS = 1;

    // Лаунчер для разрешений Bluetooth
    private ActivityResultLauncher<Intent> enableBluetoothLauncher;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_device_scan, container, false);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        deviceList = new ArrayList<>();
        deviceAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, deviceList);

        ListView listView = view.findViewById(R.id.deviceListView);
        listView.setAdapter(deviceAdapter);

        // Инициализация лаунчера для разрешений
        enableBluetoothLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (getActivity() != null && result.getResultCode() == Activity.RESULT_OK) {
                startBluetoothScan();
            } else {
                Toast.makeText(requireContext(), "Bluetooth не был включен", Toast.LENGTH_SHORT).show();
            }
        });

        // Запрос разрешений на Bluetooth (для Android 12 и выше)
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

        // Регистрируем ресивер для получения найденных устройств
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        requireContext().registerReceiver(bluetoothReceiver, filter);

        bluetoothAdapter.startDiscovery();

        // Устанавливаем слушатель для кликов на элементы списка
        listView.setOnItemClickListener((parent, view1, position, id) -> {
            String deviceInfo = deviceList.get(position);
            String deviceAddress = deviceInfo.substring(deviceInfo.indexOf("\n") + 1);
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
            connectToDevice(device);
        });

        // Обработчик для кнопки "Повторить поиск"
        Button repeatScanButton = view.findViewById(R.id.repeatScanButton);
        repeatScanButton.setOnClickListener(v -> startBluetoothScan()); // Повторный запуск сканирования

        // Обработчик для кнопки "Отправить сообщение"
        Button sendMessageButton = view.findViewById(R.id.sendMessageButton);
        sendMessageButton.setOnClickListener(v -> {
            if (socket != null && socket.isConnected()) {
                sendData(socket, "Dude!"); // Отправить сообщение "Dude!"
            } else {
                Toast.makeText(requireContext(), "Нет подключенного устройства", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        requireContext().unregisterReceiver(bluetoothReceiver); // Отменяем регистрацию ресивера
    }

    // Ресивер для обработки найденных Bluetooth устройств
    private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null && !deviceList.contains(device.getName() + "\n" + device.getAddress())) {
                    deviceList.add(device.getName() + "\n" + device.getAddress()); // Добавляем устройство в список
                    deviceAdapter.notifyDataSetChanged();
                }
            }
        }
    };

    // Функция для запуска сканирования устройств Bluetooth
    private void startBluetoothScan() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
            bluetoothAdapter.startDiscovery();
            deviceList.clear(); // Clear the device list before starting the scan
            deviceAdapter.notifyDataSetChanged();
            Toast.makeText(requireContext(), "Начинаю сканирование устройств", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(requireContext(), "Отсутствуют разрешения для сканирования Bluetooth", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(requireContext(), "Bluetooth permissions required", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Подключение к выбранному устройству
    private void connectToDevice(BluetoothDevice device) {
        new Thread(() -> {
            try {
                Log.d("FragmentTerminal", "Попытка подключения к устройству: " + device.getName());

                // Создаем сокет для подключения
                BluetoothSocket socket = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
                Log.d("FragmentTerminal", "Сокет создан для устройства: " + device.getName());

                bluetoothAdapter.cancelDiscovery(); // Останавливаем сканирование перед подключением
                Log.d("FragmentTerminal", "Сканирование отменено.");

                // Попытка подключения
                socket.connect();
                Log.d("FragmentTerminal", "Подключение успешно!");

                // Передаем сообщение о подключении в UI поток
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "Подключение к устройству " + device.getName() + " успешно", Toast.LENGTH_SHORT).show()
                );

                // Сохраняем сокет для дальнейшего использования
                this.socket = socket;

                // Отправка сообщения после успешного подключения
                sendData(socket, "Hello World");

            } catch (IOException e) {
                // Логируем ошибку, если подключение не удалось
                Log.e("FragmentTerminal", "Ошибка подключения: " + e.getMessage());

                // Передаем сообщение об ошибке в UI поток
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "Не удалось подключиться", Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }

    // Метод для отправки данных через BluetoothSocket
    private void sendData(BluetoothSocket socket, String message) {
        new Thread(() -> {
            try {
                // Получаем выходной поток сокета
                OutputStream outputStream = socket.getOutputStream();
                // Преобразуем сообщение в байты и отправляем
                outputStream.write(message.getBytes());
                outputStream.flush();
                Log.d("FragmentTerminal", "Сообщение отправлено: " + message);
            } catch (IOException e) {
                Log.e("FragmentTerminal", "Ошибка при отправке данных: " + e.getMessage());
            }
        }).start();
    }
}
