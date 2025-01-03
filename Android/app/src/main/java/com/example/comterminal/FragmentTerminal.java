package com.example.comterminal;

import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FragmentTerminal extends Fragment {

    private BluetoothSocket bluetoothSocket;
    private TextView textViewOutput;
    private EditText editTextInput;
    private OutputStream outputStream;
    private InputStream inputStream;
    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Загружаем разметку для этого фрагмента
        View view = inflater.inflate(R.layout.fragment_terminal, container, false);

        // Получаем ссылку на MainActivity, чтобы извлечь BluetoothSocket
        MainActivity activity = (MainActivity) requireActivity();
        BluetoothSocket socket = activity.getBluetoothSocket();

        // Если BluetoothSocket не равен null, инициализируем его
        if (socket != null) {
            setBluetoothSocket(socket); // Устанавливаем сокет через метод setBluetoothSocket
        } else {
            Log.e("FragmentTerminal", "BluetoothSocket равен null. Проверьте соединение.");
        }

        // Инициализация элементов интерфейса
        textViewOutput = view.findViewById(R.id.textViewOutput);
        editTextInput = view.findViewById(R.id.editTextInput);
        Button buttonSend = view.findViewById(R.id.buttonSend);

        // Устанавливаем обработчик клика для кнопки отправки
        buttonSend.setOnClickListener(v -> sendMessage());

        return view;
    }

    // Метод для установки Bluetooth-сокета
    public void setBluetoothSocket(BluetoothSocket socket) {
        this.bluetoothSocket = socket;
        try {
            outputStream = bluetoothSocket.getOutputStream();
            inputStream = bluetoothSocket.getInputStream();
            startListeningForData(); // Начинаем прослушивание входящих данных
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Метод для отправки сообщения
    private void sendMessage() {
        String message = editTextInput.getText().toString();
        if (!message.isEmpty()) {
            if (outputStream != null) {
                try {
                    outputStream.write(message.getBytes()); // Отправка данных по Bluetooth
                    outputStream.flush();
                    editTextInput.setText(""); // Очистка поля ввода
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                Log.e("FragmentTerminal", "OutputStream равен null. Bluetooth-сокет не инициализирован.");
            }
        }
    }

    // Метод для прослушивания входящих данных
    private void startListeningForData() {
        new Thread(() -> {
            byte[] buffer = new byte[1024];
            int bytes;
            while (true) {
                try {
                    // Чтение данных из входящего потока
                    bytes = inputStream.read(buffer);
                    String readMessage = new String(buffer, 0, bytes);
                    // Обновление UI на главном потоке
                    handler.post(() -> textViewOutput.append(readMessage + "\n"));
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }).start();
    }
}
