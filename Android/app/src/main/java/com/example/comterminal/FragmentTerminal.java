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
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.example.comterminal.database.AppDatabase;
import com.example.comterminal.database.TerminalMessage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class FragmentTerminal extends Fragment {

    private BluetoothSocket bluetoothSocket;
    private TextView textViewOutput;
    private EditText editTextInput;
    private OutputStream outputStream;
    private InputStream inputStream;
    private Handler handler = new Handler(Looper.getMainLooper());
    private ScrollView mText_scroll_view;

    // Поле для базы данных
    private AppDatabase database;

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

        // Инициализация базы данных
        database = AppDatabase.getInstance(requireContext());

        // Инициализация элементов интерфейса
        textViewOutput = view.findViewById(R.id.textViewOutput);
        editTextInput = view.findViewById(R.id.editTextInput);
        Button buttonSend = view.findViewById(R.id.buttonSend);
        Button buttonSave = view.findViewById(R.id.buttonSave);
        mText_scroll_view = view.findViewById(R.id.text_scroll_view);

        // Устанавливаем обработчик клика для кнопки отправки
        buttonSend.setOnClickListener(v -> sendMessage());

        // Обработчик кнопки сохранения данных в БД
        buttonSave.setOnClickListener(v -> saveData());

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
                    mText_scroll_view.fullScroll(View.FOCUS_DOWN);
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

    // Метод для сохранения данных в базу данных SQLite
    private void saveData() {
        String text = textViewOutput.getText().toString();
        if (!text.isEmpty()) {
            new Thread(() -> {
                // Создаем новую запись
                TerminalMessage message = new TerminalMessage(text, System.currentTimeMillis());
                database.terminalMessageDao().insert(message); // Добавляем новую запись в базу данных
            }).start();
        }
    }
}
