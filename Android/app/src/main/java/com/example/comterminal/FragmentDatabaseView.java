package com.example.comterminal;

import android.os.Bundle;
import android.os.Handler;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;  // Для уведомлений
import android.util.Log;

import androidx.fragment.app.Fragment;
import com.example.comterminal.database.AppDatabase;
import com.example.comterminal.database.TerminalMessage;
import java.util.List;

public class FragmentDatabaseView extends Fragment {

    private TextView textViewOutput;
    private Button loadButton, clearButton;
    private Handler handler;
    private AppDatabase database;
    private LinearLayout containerLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_database_view, container, false);

        textViewOutput = view.findViewById(R.id.textViewOutput);
        loadButton = view.findViewById(R.id.loadButton);
        clearButton = view.findViewById(R.id.clearButton);
        containerLayout = view.findViewById(R.id.containerLayout);

        handler = new Handler(); // Для обновления UI с потока
        database = AppDatabase.getInstance(requireContext());

        loadButton.setOnClickListener(v -> loadData());
        clearButton.setOnClickListener(v -> clearDatabase());

        return view;
    }

    // Метод для загрузки данных из базы данных
    private void loadData() {
        // Запрос данных из базы данных
        new Thread(() -> {
            List<TerminalMessage> messages = database.terminalMessageDao().getAllMessages();

            // Обновление UI на главном потоке
            handler.post(() -> {
                containerLayout.removeAllViews(); // Очищаем контейнер перед добавлением новых данных

                for (TerminalMessage message : messages) {
                    // Форматируем timestamp в удобочитаемый формат
                    String formattedTime = DateFormat.format("yyyy-MM-dd HH:mm:ss", message.timestamp).toString();
                    String fullMessage = formattedTime + " - " + message.message;

                    // Создаем контейнер для сообщения и кнопки
                    LinearLayout messageLayout = new LinearLayout(getContext());
                    messageLayout.setOrientation(LinearLayout.HORIZONTAL);
                    messageLayout.setPadding(0, 8, 0, 8);

                    // Создаем TextView для отображения сообщения
                    TextView messageTextView = new TextView(getContext());
                    messageTextView.setText(fullMessage);
                    messageTextView.setLayoutParams(new LinearLayout.LayoutParams(
                            0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
                    messageLayout.addView(messageTextView); // Добавляем текст в контейнер

                    // Создаем кнопку для удаления
                    Button deleteButton = new Button(getContext());
                    deleteButton.setText("Удалить");
                    deleteButton.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    deleteButton.setOnClickListener(v -> deleteMessage(message));

                    messageLayout.addView(deleteButton); // Добавляем кнопку удаления в контейнер

                    // Добавляем контейнер с сообщением и кнопкой в ScrollView
                    containerLayout.addView(messageLayout);
                }
            });
        }).start();
    }

    // Метод для удаления записи по ID
    private void deleteMessage(TerminalMessage message) {
        new Thread(() -> {
            // Удаляем запись из базы данных по ID
            database.terminalMessageDao().deleteById(message.id);

            // Обновление UI после удаления
            handler.post(() -> {
                // Покажем уведомление с помощью Toast
                Toast.makeText(getContext(), "Сообщение с ID " + message.id + " удалено.", Toast.LENGTH_SHORT).show();

                // Перезагружаем данные, чтобы отобразить актуальные записи
                loadData();
                Log.d("FragmentDatabaseView", "Сообщение с ID " + message.id + " удалено.");
            });
        }).start();
    }

    // Метод для очистки базы данных
    private void clearDatabase() {
        new Thread(() -> {
            // Очищаем таблицу терминальных сообщений
            database.terminalMessageDao().deleteAll();

            // Обновление UI на главном потоке после очистки
            handler.post(() -> {
                containerLayout.removeAllViews(); // Очищаем все записи из интерфейса
                Toast.makeText(getContext(), "Все данные удалены.", Toast.LENGTH_SHORT).show();
            });
        }).start();
    }
}
