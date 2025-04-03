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
            "sl.u.AFoUVFVXPbvFylu3BihvpyXWYtLzr92xf38_uw_X6W6QA4Ti2huUh6VcVwSQr-yjlgqPyZxUYRPq7w4Tv9PavQWfToO7FA0MP08hLMnivmeL278YaCoVTh1UgLXI1--Br4o5qxxo8oxLqZqLH2jOBLtUKGx5Y9luWCxO7unRfxm1FyOxmcDkTc7f2tfqRtzognjmxSv41x5wTe7XGeq2ei-uk1bAmgwLRbepNEB90Iz5ih_mYi0vOSlNSRGqp3KAZ4nhsg1oikCLUAL-V_heHlke4Gg8w4-5nqGTRlQ6TVcw7g1FbxQR0KQb0HDG9uo62r-ftt-TfQ3DXrWbpaWrgdqvuOt4IzI6GtnCAIH-xsGNs51S6wi55UaJrR5sJPTBbeIfyRcAcSU0ZnoHhfcWvyaucyJCWEEz9sihjfEh48ZXO9fTLj2OTJdG0Lild7frwXr4U0izHDShB4S3an_7xEHMwXdo2xvBvmE5GGWRFDR0fou1x4VdTxv7Ft3P0JCS6FTyjcPG4sDTTmPe-9CEWpQZRf1br1uvsj-o6yIW-wbdr45r7NxpJKCL6rK2BLcek69tZmnYGB9velEIkPFiaz_Fo8tXEG8-cOFxXvNCvovzS-wu_toMdx585s4lOK-v-l2Nhmi-kHvRqJDzBF6NVfP_KZenXeOPCEZ5_ZRfLUri_FAme84Qdv7lYqATtBjwjK-S5ZiCae61sGShxndCJkLRYM5-li-Z82B9Dq-BsJKFGhszn58kz94AqjFb7vkIpLDW1OBVueT7gi0yCTmzVPRcjxPQ9J1MUskDxQ4-fOMwTQ-W5a7S3Q7NvMuKjQmhMoGhAKFsjRCVVzE1By_4lNw5Fn6G_hcBQCUbxmPiTmf5b15rAE72gM0jPswQT25NvDU2yJCxyO2NDodo7qD5SOUlh4xeWzADp4gXiTfTJ3syhjzQhJdhOkO4b9KH_BAJEAjih6PYV2SveVlzdYuzkFvqkrtLtjxaE1FX7HRw9DhsRUPZgUeSNNWNJczSAeZpZZQp3zTlv7wO2f5fGaNrPY0lL9wBb8TLwhSCSVK3cHiw8oMqUYVJz5SvLxYer0BB_qNqP_qWB5LNpO-bFjdjyALml2NouA81CaT7Ja3bEJ_uAYIAk6cuASbrzGu5BJg81CZuhZhDcmyTZBmp3gFbB02hZ-Nwbbtmlu0EsnUF_ZMHRo_qwdaHgkK-AfzCqb0hOSXaMDZx9vNDfdKs7W6Amk6rJxEGYqke_mPJsTCBwdk_EKZW0-sUfUv-Z2wg-cDDYcbsxInwC8RLB-DmphLM3Z0q37RbstyVD7NmzLmSaZCFSaS0OrWG4npBOwlYsBKxPsA";

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        containerLayout = view.findViewById(R.id.containerLayout);
        Button loadButton = view.findViewById(R.id.loadButton);

        handler = new Handler();
        database = AppDatabase.getInstance(requireContext());

        loadButton.setOnClickListener(v -> loadData());
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
        // Преобразуем timestamp (метка времени в миллисекундах) в строку
        long timestamp = message.timestamp;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedTime = sdf.format(new Date(timestamp));

        // Формируем полное сообщение
        String fullMessage = formattedTime + " - " + message.message;

        // Создаём контейнер для сообщения
        LinearLayout messageLayout = new LinearLayout(getContext());
        messageLayout.setOrientation(LinearLayout.HORIZONTAL);
        messageLayout.setPadding(0, 8, 0, 8);

        // Создаём TextView для отображения сообщения
        TextView messageTextView = new TextView(getContext());
        messageTextView.setText(fullMessage);
        messageTextView.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        // Создаём кнопку "Удалить"
        Button deleteButton = new Button(getContext());
        deleteButton.setText("Удалить");
        deleteButton.setOnClickListener(v -> deleteMessage(message));

        // Создаём кнопку для загрузки в Dropbox
        Button uploadButton = new Button(getContext());
        uploadButton.setText("Dropbox");
        uploadButton.setOnClickListener(v -> uploadMessageToDropbox(message));

        // Добавляем TextView и кнопки в контейнер
        messageLayout.addView(messageTextView);
        messageLayout.addView(deleteButton);
        messageLayout.addView(uploadButton);

        // Добавляем контейнер с сообщением в основной контейнер
        containerLayout.addView(messageLayout);
    }

    private void deleteMessage(TerminalMessage message) {
        new Thread(() -> {
            database.terminalMessageDao().deleteById(message.id);
            handler.post(() -> {
                loadData();
            });
        }).start();
    }

    private void uploadMessageToDropbox(TerminalMessage message) {
        new Thread(() -> {
            // Создаем CSV файл на устройстве
            File csvFile = new File(requireContext().getExternalFilesDir(null), "message_" + message.id + ".csv");

            try (FileWriter writer = new FileWriter(csvFile)) {
                // Записываем данные в CSV
                writer.append("id,message,timestamp\n");
                writer.append(message.id + "," + message.message.replace(",", " ") + "," + message.timestamp + "\n");
                writer.flush();
            } catch (IOException e) {
                handler.post(() -> Toast.makeText(getContext(), "Ошибка при создании файла", Toast.LENGTH_SHORT).show());
                return;
            }

            // Загружаем файл в Dropbox
            uploadToDropbox(csvFile, message);
        }).start();
    }

    private void uploadToDropbox(File file, TerminalMessage message) {
        new Thread(() -> {
            try {
                // Конфигурация Dropbox клиента
                DbxRequestConfig config = DbxRequestConfig.newBuilder("dropbox/app").build();
                DbxClientV2 client = new DbxClientV2(config, ACCESS_TOKEN);

                // Загрузка файла в Dropbox
                try (InputStream in = new FileInputStream(file)) {
                    client.files().uploadBuilder("/" + file.getName())
                            .withMode(WriteMode.OVERWRITE)  // Оверрайт, если файл с таким именем уже существует
                            .uploadAndFinish(in);

                    // Записываем запись в таблицу DropboxUpload
                    long timestamp = System.currentTimeMillis();
                    DropboxUpload uploadRecord = new DropboxUpload(message.id, timestamp, "success");

                    // Вставляем запись в базу данных
                    database.dropboxUploadDao().insert(uploadRecord);

                    // Уведомление об успешной загрузке
                    handler.post(() -> Toast.makeText(getContext(), "Файл загружен: " + file.getName(), Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                Log.e("DropboxError", "Ошибка загрузки: " + e.getMessage(), e);
                handler.post(() -> Toast.makeText(getContext(), "Ошибка загрузки в Dropbox", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}