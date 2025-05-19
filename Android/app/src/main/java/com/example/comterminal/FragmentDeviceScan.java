package com.example.comterminal;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.comterminal.database.Device;
import com.example.comterminal.database.LogEntry;
import com.example.comterminal.database.AppDatabase;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FragmentDeviceScan extends Fragment {

    private BluetoothAdapter bluetoothAdapter; // Адаптер Bluetooth — основная точка доступа к Bluetooth
    private ArrayAdapter<String> deviceAdapter; // Адаптер для отображения найденных устройств
    private ArrayList<String> deviceList; // Список найденных устройств
    private BluetoothSocket socket; // Сокет активного соединения с устройством

    private static final int REQUEST_BLUETOOTH_PERMISSIONS = 1;

    private ActivityResultLauncher<Intent> enableBluetoothLauncher; // Запрос на включение Bluetooth через Activity Result API
    private ProgressBar progressBar; // Прогресс-бар при сканировании

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_device_scan, container, false);

        // Получаем Bluetooth-адаптер
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Инициализируем список и адаптеры
        deviceList = new ArrayList<>();
        deviceAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, deviceList);

        ArrayList<String> connectedDevicesList = new ArrayList<>();
        ArrayAdapter<String> connectedDevicesAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, connectedDevicesList);

        ListView deviceListView = view.findViewById(R.id.deviceListView);
        ListView connectedDevicesListView = view.findViewById(R.id.connectedDevicesListView);
        deviceListView.setAdapter(deviceAdapter);
        connectedDevicesListView.setAdapter(connectedDevicesAdapter);

        progressBar = view.findViewById(R.id.progressBar);

        // Используется для обработки результата включения Bluetooth (новый способ вместо startActivityForResult)
        enableBluetoothLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == android.app.Activity.RESULT_OK) {
                startBluetoothScan(); // если пользователь включил Bluetooth — начинаем сканирование
            } else {
                Toast.makeText(requireContext(), "Bluetooth не был включен", Toast.LENGTH_SHORT).show();
            }
        });

        // Загружаем уже спаренные устройства
        for (BluetoothDevice device : bluetoothAdapter.getBondedDevices()) {
            connectedDevicesList.add(device.getName() + "\n" + device.getAddress());
        }
        connectedDevicesAdapter.notifyDataSetChanged();

        // Обработка клика по списку подключённых устройств
        connectedDevicesListView.setOnItemClickListener((parent, view1, position, id) -> {
            String deviceInfo = connectedDevicesList.get(position);
            String deviceAddress = deviceInfo.substring(deviceInfo.indexOf("\n") + 1);
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
            connectToDevice(device); // инициируем соединение
        });

        // Обработка клика по списку найденных устройств
        deviceListView.setOnItemClickListener((parent, view1, position, id) -> {
            String deviceInfo = deviceList.get(position);
            String deviceAddress = deviceInfo.substring(deviceInfo.indexOf("\n") + 1);
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
            connectToDevice(device);
        });

        // Кнопка "Повторное сканирование"
        Button repeatScanButton = view.findViewById(R.id.repeatScanButton);
        repeatScanButton.setOnClickListener(v -> startBluetoothScan());

        // Кнопка "Отправить сообщение"
        Button sendMessageButton = view.findViewById(R.id.sendMessageButton);
        sendMessageButton.setOnClickListener(v -> {
            if (socket != null && socket.isConnected()) {
                sendData(socket, "Соединение разорвано!");
                saveDisconnectionLog(socket); // логируем отключение
            } else {
                Toast.makeText(requireContext(), "Нет подключенного устройства", Toast.LENGTH_SHORT).show();
            }
        });

        // Проверка разрешений Bluetooth
        checkAndRequestPermissions();

        // Регистрация приёмника для событий Bluetooth
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        requireContext().registerReceiver(bluetoothReceiver, filter);

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Отменяем регистрацию приёмника при уничтожении фрагмента
        requireContext().unregisterReceiver(bluetoothReceiver);
    }

    // Проверка разрешений и их запрос при необходимости
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
            startBluetoothScan(); // если всё уже разрешено — запускаем сканирование
        }
    }

    // Результат запроса разрешений
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_BLUETOOTH_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startBluetoothScan();
            } else {
                Toast.makeText(requireContext(), "Требуются разрешения Bluetooth", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Запуск сканирования устройств
    private void startBluetoothScan() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
            bluetoothAdapter.startDiscovery(); // начинаем поиск
            deviceList.clear(); // очищаем предыдущий список
            deviceAdapter.notifyDataSetChanged();
            progressBar.setVisibility(View.VISIBLE); // показываем прогресс
            Toast.makeText(requireContext(), "Начинаю сканирование устройств", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(requireContext(), "Отсутствуют разрешения для сканирования Bluetooth", Toast.LENGTH_SHORT).show();
        }
    }

    // Приёмник событий Bluetooth
    private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Добавляем новое устройство в список
                if (device != null && !deviceList.contains(device.getName() + "\n" + device.getAddress())) {
                    deviceList.add(device.getName() + "\n" + device.getAddress());
                    deviceAdapter.notifyDataSetChanged();
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(intent.getAction())) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Сканирование завершено", Toast.LENGTH_SHORT).show();
            }
        }
    };

    // Установка соединения с выбранным устройством
    private void connectToDevice(BluetoothDevice device) {
        if (socket != null && socket.isConnected()) {
            Toast.makeText(requireContext(), "Устройство уже подключено", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            try {
                // Используем стандартный UUID для Serial Port Profile (SPP)
                BluetoothSocket socket = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
                bluetoothAdapter.cancelDiscovery(); // обязательно отменяем сканирование перед соединением
                socket.connect(); // соединяемся

                // Обновляем UI в основном потоке
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Устройство подключено", Toast.LENGTH_SHORT).show();
                    this.socket = socket;
                    ((MainActivity) requireActivity()).setBluetoothSocket(socket); // передаём сокет в активити
                    saveDeviceToDatabase(device); // сохраняем инфо об устройстве
                    saveLogToDatabase(device);    // логируем подключение
                });
            } catch (IOException e) {
                requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), "Не удалось подключиться", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    // Сохраняем новое устройство в БД
    private void saveDeviceToDatabase(BluetoothDevice device) {
        long currentTime = System.currentTimeMillis();
        Device newDevice = new Device(device.getName(), device.getAddress(), currentTime);

        AppDatabase db = AppDatabase.getInstance(requireContext());
        Executors.newSingleThreadExecutor().execute(() -> db.deviceDao().insert(newDevice));
    }

    // Сохраняем лог подключения
    private void saveLogToDatabase(BluetoothDevice device) {
        long currentTime = System.currentTimeMillis();
        AppDatabase db = AppDatabase.getInstance(requireContext());

        Executors.newSingleThreadExecutor().execute(() -> {
            Device existingDevice = db.deviceDao().getDeviceByAddress(device.getAddress());
            if (existingDevice != null) {
                LogEntry logEntry = new LogEntry(existingDevice.id, currentTime, "подключено");
                db.logEntryDao().insert(logEntry);
            }
        });
    }

    // Сохраняем лог отключения
    private void saveDisconnectionLog(BluetoothSocket socket) {
        long currentTime = System.currentTimeMillis();
        AppDatabase db = AppDatabase.getInstance(requireContext());

        Executors.newSingleThreadExecutor().execute(() -> {
            Device existingDevice = db.deviceDao().getDeviceByAddress(socket.getRemoteDevice().getAddress());
            if (existingDevice != null) {
                LogEntry logEntry = new LogEntry(existingDevice.id, currentTime, "отключено");
                db.logEntryDao().insert(logEntry);
            }
        });
    }
}
