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
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.comterminal.database.AppDatabase;
import com.example.comterminal.database.TerminalMessage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FragmentTerminal extends Fragment {

    // Сокет Bluetooth-соединения
    private BluetoothSocket bluetoothSocket;

    // Элементы интерфейса
    private TextView textViewOutput; // Поле для вывода сообщений
    private EditText editTextInput; // Поле для ввода сообщения
    private ScrollView mText_scroll_view;

    // Потоки для общения с Bluetooth-устройством
    private OutputStream outputStream;
    private InputStream inputStream;
    private Handler handler = new Handler(Looper.getMainLooper());
    private AppDatabase database;

    // Метод, вызываемый при создании фрагмента
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Загружаем layout
        View view = inflater.inflate(R.layout.fragment_terminal, container, false);

        // Получаем BluetoothSocket из MainActivity
        MainActivity activity = (MainActivity) requireActivity();
        BluetoothSocket socket = activity.getBluetoothSocket();

        // Устанавливаем Bluetooth-сокет, если доступен
        if (socket != null) {
            setBluetoothSocket(socket);
        } else {
            Log.e("FragmentTerminal", "BluetoothSocket равен null. Проверьте соединение.");
        }

        // Получаем экземпляр базы данных
        database = AppDatabase.getInstance(requireContext());

        // Привязка UI-элементов
        textViewOutput = view.findViewById(R.id.textViewOutput);
        editTextInput = view.findViewById(R.id.editTextInput);
        Button buttonSend = view.findViewById(R.id.buttonSend);
        mText_scroll_view = view.findViewById(R.id.text_scroll_view);

        // Обработчик нажатия на кнопку "Отправить"
        buttonSend.setOnClickListener(v -> sendMessage());

        return view;
    }

    // Инициализация сокета и запуск приёма данных
    public void setBluetoothSocket(BluetoothSocket socket) {
        this.bluetoothSocket = socket;
        try {
            outputStream = bluetoothSocket.getOutputStream(); // поток отправки
            inputStream = bluetoothSocket.getInputStream(); // поток приёма
            startListeningForData(); // слушаем входящие данные
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Отправка текста через Bluetooth
    private void sendMessage() {
        String message = editTextInput.getText().toString(); // читаем текст из поля ввода
        if (!message.isEmpty()) {
            if (outputStream != null) {
                try {
                    outputStream.write(message.getBytes()); // отправка данных
                    outputStream.flush();
                    mText_scroll_view.fullScroll(View.FOCUS_DOWN); // прокрутка вниз
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                Log.e("FragmentTerminal", "OutputStream равен null. Bluetooth-сокет не инициализирован.");
            }
        }
    }

    // Поток приёма данных по Bluetooth
    private void startListeningForData() {
        new Thread(() -> {
            byte[] buffer = new byte[1024]; // буфер для входящих данных
            int bytes;
            while (true) {
                try {
                    bytes = inputStream.read(buffer); // читаем из потока
                    String readMessage = new String(buffer, 0, bytes); // преобразуем в строку

                    // Обновление UI в главном потоке
                    handler.post(() -> {
                        textViewOutput.append(readMessage + "\n"); // отображаем
                        addMessageToLayout(readMessage);           // добавляем с кнопкой
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                    break; // выход из цикла при ошибке
                }
            }
        }).start();
    }

    // Отображение нового сообщения с кнопкой "Сохранить"
    private void addMessageToLayout(String messageText) {
        // Контейнер для сообщений (LinearLayout внутри ScrollView)
        LinearLayout containerLayout = mText_scroll_view.findViewById(R.id.messageContainer);

        // Горизонтальный контейнер: сообщение + кнопка
        LinearLayout messageLayout = new LinearLayout(getContext());
        messageLayout.setOrientation(LinearLayout.HORIZONTAL);
        messageLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        // Текст сообщения
        TextView messageView = new TextView(getContext());
        messageView.setText(messageText);
        messageView.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f)); // Вес 1: занимает всё доступное пространство

        // Кнопка "Сохранить"
        Button saveButton = new Button(getContext());
        saveButton.setText("Сохранить");
        saveButton.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        saveButton.setOnClickListener(v -> saveMessage(messageText)); // обработка клика

        // Добавляем элементы в горизонтальный layout
        messageLayout.addView(messageView);
        messageLayout.addView(saveButton);

        // Добавляем весь блок в контейнер
        containerLayout.addView(messageLayout);
    }

    // Сохранение сообщения в локальную БД (Room)
    private void saveMessage(String messageText) {
        new Thread(() -> {
            TerminalMessage message = new TerminalMessage(messageText, System.currentTimeMillis());
            database.terminalMessageDao().insert(message); // вставка записи
        }).start();
    }
}
