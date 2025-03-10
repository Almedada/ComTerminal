package com.example.comterminal;

import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.util.Log;

import androidx.fragment.app.Fragment;

import com.example.comterminal.database.AppDatabase;
import com.example.comterminal.database.TerminalMessage;

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
    private ScrollView mText_scroll_view;

    private AppDatabase database;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_terminal, container, false);

        MainActivity activity = (MainActivity) requireActivity();
        BluetoothSocket socket = activity.getBluetoothSocket();

        if (socket != null) {
            setBluetoothSocket(socket);
        } else {
            Log.e("FragmentTerminal", "BluetoothSocket равен null. Проверьте соединение.");
        }

        database = AppDatabase.getInstance(requireContext());

        textViewOutput = view.findViewById(R.id.textViewOutput);
        editTextInput = view.findViewById(R.id.editTextInput);
        Button buttonSend = view.findViewById(R.id.buttonSend);
        mText_scroll_view = view.findViewById(R.id.text_scroll_view);

        buttonSend.setOnClickListener(v -> sendMessage());

        return view;
    }

    public void setBluetoothSocket(BluetoothSocket socket) {
        this.bluetoothSocket = socket;
        try {
            outputStream = bluetoothSocket.getOutputStream();
            inputStream = bluetoothSocket.getInputStream();
            startListeningForData();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendMessage() {
        String message = editTextInput.getText().toString();
        if (!message.isEmpty()) {
            if (outputStream != null) {
                try {
                    outputStream.write(message.getBytes());
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

    private void startListeningForData() {
        new Thread(() -> {
            byte[] buffer = new byte[1024];
            int bytes;
            while (true) {
                try {
                    bytes = inputStream.read(buffer);
                    String readMessage = new String(buffer, 0, bytes);
                    handler.post(() -> {
                        textViewOutput.append(readMessage + "\n");
                        addMessageToLayout(readMessage); // Добавляем сообщение в ScrollView
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }).start();
    }

    // Метод для добавления сообщения в ScrollView с кнопкой сохранения
    private void addMessageToLayout(String messageText) {
        LinearLayout containerLayout = mText_scroll_view.findViewById(R.id.messageContainer); // Где контейнер для сообщений
        LinearLayout messageLayout = new LinearLayout(getContext());
        messageLayout.setOrientation(LinearLayout.HORIZONTAL);
        messageLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        TextView messageView = new TextView(getContext());
        messageView.setText(messageText);
        messageView.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)); // Текстовое сообщение

        Button saveButton = new Button(getContext());
        saveButton.setText("Сохранить");
        saveButton.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        saveButton.setOnClickListener(v -> saveMessage(messageText)); // Сохранение при нажатии

        messageLayout.addView(messageView); // Добавляем текст
        messageLayout.addView(saveButton); // Добавляем кнопку

        containerLayout.addView(messageLayout); // Добавляем в контейнер
    }

    // Метод для сохранения данных по кнопке
    private void saveMessage(String messageText) {
        new Thread(() -> {
            TerminalMessage message = new TerminalMessage(messageText, System.currentTimeMillis());
            database.terminalMessageDao().insert(message);
        }).start();
    }
}
