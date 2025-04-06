package com.example.comterminal.fragmentDatabase;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Date;
import android.widget.Toast;
import android.util.Log;

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
import java.util.List;

public class MessagesFragment extends Fragment {

    private AppDatabase database;
    private LinearLayout containerLayout;
    private Handler handler;

    public MessagesFragment() {
        super(R.layout.fragment_messages);
    }

    private static final String ACCESS_TOKEN =
            "sl.u.AFpGI0Oe3fj42XuPFMmxa2k12AxfJEYteMs9nsKR_99FxaeC0ne3WfOliHijKa6hmjStiEN6TvhMBwWGVwY6qqzxX3tFQYNct3LqWH0NDSLXLVbOhtkpslxaIpcqULr4vveOzIUeYPi_d03rpmuCox0kd7qY19ZAIt1K__21atT2VtqVs-cPJWPNRtSZQ78VcUxBghmjWaICqX9anYXEaajZitwRM4irBJFfgtb6LSYVOj6BfYNB2qXstv8w2w1A_N5sUPmoiIXlpvMmNu62Sju_MLXqhlptNp0UtIVUHP9VAKllpJAqS3J1vPmskGb9mU9mxh9wZuCOtpk6wEb7ixXQ1F4X5b7jI_FXB2doDrDpK6-Wg4-jwUO9oBtn4WnX4OdgoqHBujTU-jsZwMTHKdy3PavOylVQIkx4QjKo_etMzuRdwKfhdOTaLmROVQV1wNFqS8y_vQRwdCUHzznpghZKDBA2gSpCRf6eMFTPx8dv99IEmglIQUVSgtwMkwNkrQdzw9XdAjv9fJdLxlXzEQ3i-MjoDaQwFlJi6e8QEJYB1lpjoF60ddGPyt0snO7LHKGkr2K_buu-XFVZXyob_F6lEbA3P_cW2FUgyQP2u4-K_8w1NL-WPoMDHdO--V-riikPaDqSJjCDTszi-94UftTapFSLPbLaxAjt5QZPLdbphT0Ogj6oyBxlWGr2ueRtXqbxhkKSZAWC9X-zYQB-ozc3dMDpKZclD0ePvEv351ZIlx8MvPWhrNXxnRkzZCL8MHBqRS9kj-He6iv9g5KwgcHQFalRnoXyCggEQr-h23B2UigioNlfDISBwLWbHZdr-s6Hntk2r52F2CL-jvbEnXdZd1Y6p5rlaqB1yjsIJL8O2c6UvBUJpACqjlYL6sypESylhLgAYzrpUqRyySu-Oh5oMI5qQV5IajGYs-399qbO1LnfNt5qLgKJSjcAUjWh2t0Wv1bYik4syo_Xg4zKh2zgReNdc59ryt3l-3ffRr31fBUhSdaQFiI7Cf9VuQcaEdkoR5I4rIDr4tBL8EH_-xHFFcIQB1XAooHd9D2R0Sk1rLOfhIurG32H0-dAm0veye5vR1aY5XHMxNRptyiuX4kcZ6PfipW56g9_2FSZKATls5ujcd_ZE7YV1cjXxM5qhPfBplQHN80tLUYwYdXyXsHCkGMjHBlhtK66ChYIJOazu1_c0sO4vqq306ktKL_vTxHMJa9K1FZfMhSjF26PLBF61Bn7ZqNCktRH8L7H2Q3UvnFESaMA7xUXPklMsXL5V3BgpphPam621xQIGJMGtibm-w9fXkdoLX1aHe9IQ76JrsgiXsr9GzBUZqTCSxwOh8c";

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

        TextView messageTextView = new TextView(getContext());
        messageTextView.setText(fullMessage);
        messageTextView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        Button deleteButton = new Button(getContext());
        deleteButton.setText("Удалить");
        deleteButton.setOnClickListener(v -> deleteMessage(message));

        Button uploadButton = new Button(getContext());
        uploadButton.setText("Dropbox");
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
