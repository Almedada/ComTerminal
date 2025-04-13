package com.example.comterminal.fragmentDatabase;

import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;
import android.text.Layout;

import androidx.fragment.app.Fragment;

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.WriteMode;
import com.example.comterminal.R;
import com.example.comterminal.database.AppDatabase;
import com.example.comterminal.database.TerminalMessage;
import com.example.comterminal.database.DropboxUpload;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MessagesFragment extends Fragment {

    private AppDatabase database;
    private LinearLayout containerLayout;
    private Handler handler;

    public MessagesFragment() {
        super(R.layout.fragment_messages);
    }

    private static final String ACCESS_TOKEN =
            "sl.u.AFpTtJSMpdd7-3C77wnShMNkXyJBBjPCCaxRIkww_e_cYvyDpvts-nSETfQXH25-CesXUIeBP4a9kFFuHiDVee7Ou11q2JSas5tENvaF4MJkEOqblEaXkBfeh0BXFaJyh_8wy2p3TciRvoXJVRvzxid0JC2pG0CmfirpQX3hi-Tl6Dtu_OiQmnb8nWZlMcBscQPGkYeqC1af6UR-rjwlTuqAxx-CKcWTPL6pBN4Dfz-2pq4SAfw3QKRHDnylww8V2RO4DO0q_-3d08rnzZwTt1T4TSNsr1qOWbKwDQ_QYfE05mBeW-N-rIEQdY4yFFZn9ClaBkJ4rXsc-psPWUbDTuoxLILKmClZW169A6cR7ROQEkWSTCaLUfW7GOlPrgMSDl8oWeM2OoGZc7ZLLCJLqidQiCM5gwg6H_cOify8UMQbU7u0n3VlDud5uNMFBW-2zBIYReiuwjJyoyGF8G-6HbsvleaXfYAYbg31xZyTVmcOLOU8c4tjHob0w8WwFUw3PUpMWOfysrAIiLJd2EGWW1aynXY0YrPc5GStznpTSe6_qcly00duoAywnaeA5dqux5hQcm2aIEAt5Lpm9ivAMNaZp00U063dhF6cMhzlz6RkJFhWAYfIWMZiDWwQlotxPFtYNz0f8KoN6LKlZLxd3myilNs2Yh7684QVAJ3vOHdGyJlAQbmWQqAWx7DrJfRqfdZjHIeBQ-m4p4e2FdXP-ioO3QQPoQc4NJWlBHYwKARuMAXke--NvjYsqe7YT85tMseBAiWl2j0MvcpSwv5lBSERwlnB8-J2DtUXQX3XC6wDR8g5rDr9VaFjExsXcLPHJrNY2bjkdAIh2Xcgrjbd78bAx12SitBLfXZ4yuUqyAmnkF4s7t6_T1Ce0Ucf-dUoO3gUuzUehekcmyT4jFFNuQd4X4yk9r06yUYOtVMKgTU0cCXpq682R-66cY8nIOt9qkaeIpOkQM_JJ7nUyH656wOJDda6qZpO1KtJQvBc3MLwhx2hUy56L4OKZJE5RseRrNIii6eC4Of9GacCqd31tVtntQx2Onbh_pkvPRTqj6wDj7wxbHgYR9rA5zIYu_a34S7aLWBJHueknkfe_CNl0-VMXzVChbku-x0bLEY_58lOm6cAPMM7j79BqlI6GjD7rpBxLSA8UPDeGruZKwO33dc40alsI3jink-yZU8r3dFTLAyO10FpS_nZspBgBVjgoTKKtDmwftShQxOlY7uXBF1mHdmopl4iD8945EaMIECiG-OVg-9T2aYgP1rXdac1jOvvCaq9_kI06olpEm3Htoo5KxuNQ8XHuiqkkE1taad6CiCCCh37KHesfdZZxq5Zj8k";

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        containerLayout = view.findViewById(R.id.containerLayout);
        Button loadButton = view.findViewById(R.id.loadButton);
        Button clearButton = view.findViewById(R.id.clearButton);  // Ищем кнопку для очистки

        handler = new Handler();
        database = AppDatabase.getInstance(requireContext());

        loadButton.setOnClickListener(v -> loadData());

        // Обработчик для очистки сообщений
        clearButton.setOnClickListener(v -> clearMessages());
    }

    private void loadData() {
        new Thread(() -> {
            List<TerminalMessage> messages = database.terminalMessageDao().getAllMessages();
            handler.post(() -> {
                containerLayout.removeAllViews();
                for (TerminalMessage message : messages) {
                    addMessageToLayout(message);
                }
            });
        }).start();
    }

    private void addMessageToLayout(TerminalMessage message) {
        long timestamp = message.timestamp;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedTime = sdf.format(new Date(timestamp));
        String fullMessage = formattedTime + " - " + message.message;

        LinearLayout messageLayout = new LinearLayout(getContext());
        messageLayout.setOrientation(LinearLayout.HORIZONTAL);
        messageLayout.setPadding(0, 8, 0, 8);

        // Уменьшаем TextView для текста
        TextView messageTextView = new TextView(getContext());
        messageTextView.setText(fullMessage);
        messageTextView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        messageTextView.setMaxLines(Integer.MAX_VALUE);  // Убираем ограничение на количество строк
        messageTextView.setEllipsize(null);  // Убираем троеточие в конце
        messageTextView.setPadding(8, 8, 8, 8);  // Отступы для текста
        messageTextView.setBreakStrategy(Layout.BREAK_STRATEGY_SIMPLE);  // Обработка длинных слов

        // Уменьшаем кнопку "Удалить"
        Button deleteButton = new Button(getContext());
        deleteButton.setText("Удалить");
        deleteButton.setTextSize(10f);  // Уменьшаем размер текста
        LinearLayout.LayoutParams deleteButtonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        deleteButtonParams.width = 200;  // Фиксированная ширина
        deleteButtonParams.height = 120;  // Фиксированная высота
        deleteButton.setLayoutParams(deleteButtonParams);
        deleteButton.setPadding(8, 8, 8, 8);  // Отступы для кнопки
        deleteButton.setOnClickListener(v -> deleteMessage(message));

        // Уменьшаем кнопку "Dropbox"
        Button uploadButton = new Button(getContext());
        uploadButton.setText("Dropbox");
        uploadButton.setTextSize(10f);  // Уменьшаем размер текста
        LinearLayout.LayoutParams uploadButtonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        uploadButtonParams.width = 200;  // Фиксированная ширина
        uploadButtonParams.height = 120;  // Фиксированная высота
        uploadButton.setLayoutParams(uploadButtonParams);
        uploadButton.setPadding(8, 8, 8, 8);  // Отступы для кнопки
        uploadButton.setOnClickListener(v -> uploadMessageToDropbox(message));

        messageLayout.addView(messageTextView);
        messageLayout.addView(deleteButton);
        messageLayout.addView(uploadButton);
        containerLayout.addView(messageLayout);
    }

    private void deleteMessage(TerminalMessage message) {
        new Thread(() -> {
            database.terminalMessageDao().deleteById(message.id);
            handler.post(() -> loadData());
        }).start();
    }

    private void uploadMessageToDropbox(TerminalMessage message) {
        new Thread(() -> {
            File csvFile = new File(requireContext().getExternalFilesDir(null), "message_" + message.id + ".csv");

            try (FileWriter writer = new FileWriter(csvFile)) {
                writer.append("id,message,timestamp\n");
                writer.append(message.id + "," + message.message.replace(",", " ") + "," + message.timestamp + "\n");
                writer.flush();
            } catch (IOException e) {
                handler.post(() -> Toast.makeText(getContext(), "Ошибка при создании файла", Toast.LENGTH_SHORT).show());
                return;
            }

            uploadToDropbox(csvFile, message);
        }).start();
    }

    private void uploadToDropbox(File file, TerminalMessage message) {
        new Thread(() -> {
            try {
                DbxRequestConfig config = DbxRequestConfig.newBuilder("dropbox/app").build();
                DbxClientV2 client = new DbxClientV2(config, ACCESS_TOKEN);

                try (InputStream in = new FileInputStream(file)) {
                    client.files().uploadBuilder("/" + file.getName())
                            .withMode(WriteMode.OVERWRITE)
                            .uploadAndFinish(in);

                    long timestamp = System.currentTimeMillis();
                    DropboxUpload uploadRecord = new DropboxUpload(message.id, timestamp, "success");

                    database.dropboxUploadDao().insert(uploadRecord);

                    handler.post(() -> Toast.makeText(getContext(), "Файл загружен: " + file.getName(), Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                Log.e("DropboxError", "Ошибка загрузки: " + e.getMessage(), e);
                handler.post(() -> Toast.makeText(getContext(), "Ошибка загрузки в Dropbox", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    // Метод для очистки всех сообщений из базы данных
    private void clearMessages() {
        new Thread(() -> {
            database.terminalMessageDao().deleteAll();  // Удаляем все записи
            handler.post(() -> {
                loadData();  // Обновляем UI после очистки
                Toast.makeText(getContext(), "Сообщения очищены", Toast.LENGTH_SHORT).show();
            });
        }).start();
    }
}
