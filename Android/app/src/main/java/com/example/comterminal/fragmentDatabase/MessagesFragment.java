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
            "sl.u.AFoiw10SjSCtCloG7jV401oQG9ppaZTI6FqKTNA52mETnS0q0u-MJeGpzAL0mLmOGnIAyMQ5TnayVLPbUc8y8vQ7fzvkkLKaMA0B2U9vFBrOEGIxC5aTCrB1Z6eZSG74t3nIll3a3B5_YzJPhtNZvmziMJdj9cezAfsa54IeJOswWxb-uPRljzjmnIxwzZuWw7gKSzPn0c21B8VOpMMsoKWvfT-nM0ko8cDXgYfTzoznRhZtZyZJ5Uh0T3ahkriw9k4I6hUtzehWyq0MFckwegB_34D7WgTgL_P7NHsrwl1sxUGzYW12Zv3x7orE4s7sORBeLp7x_yuuqQN3wYBjo25b5j3J4LpBZl11wjKWcJL3c6jZe6j9cWyA8kaNCq1bkLzX016u9jrmtmzMIV8YeAPnIJFwp1YfEOldMiJ5LDRZtG9Y8PnxIqNTRGLmDtcbN41WM-axh1XUsDZDNZ6y6mfIXfHvMQsllnRH0Z1ugzUY9b8QOZ0T0k_fTQnwc41F9AJDkljOKxsQSvkILPjmCWDcH-5mSLe9_48PCvX7_-L2Y5FeCVJiTCMrluQkFTF8aB7JoCSu9O4aN2xiV9wCcxXETEpdw_OxRQq2EWJgpwAi5CcOsL28Ms7pyVrQnyGUIj9-7H_16-ZB-uYnmLuWn3IUKVdRqgDHPRTc86n4X04134QI-t8eJBJt1PX4sdXSwdziU98oLRaXYN3Wx_YSNhjk1xECWfl0Z7b-Qh04Ki7uGWYEjc8lv5auIE841hIbyN7tTr-WHb_LYBV-FEk0g4AMGD_DC-ft_mJDdlV2K9Fl8sMfzvzRBTUSgK5WilfZUntTi5m3Ly1a4iUpPYOyaQWYqSskceKCxy_bWtC6w-Ix9ujFrBYNQ5wPW82s04twW7TDtXNJjURWG8arlMoWHJdRWDrRZrBrUTjy3qWq0cqGH4nesnAfle9F6e_WXBTc2AAMvpdS-iKDuWart_pXEnGfSJVLMeRpFPqJo8cC1q6PzqFZXVmItH8OXId41ttbbnMx6KwF9K47Binwp5VaX6w5MNB7vDD8xvCxqfiejPCSp__2k-3HHIf_OK_ymflcAHdO8dilYpPfYOkg0G0YrUNiZDDQpQFxT8b00ocPA-7GUsG_6mfc4sCgtlhv9RfBvSTwiGCszFQnk-WpjT3-1Lt1f4j7fnXSS03CW_iThpVGu-cx3d61ubumcRjN-rhIP17yTAbwVCldEYg-btpc2oznX7VONfjwfbhff6ZkAg6k4UQ7r1x3HQBgroxlc181s3-zTo7Z7GPBFjAcBzcKoCMOKvheu3-i1BMp1SRkm7pGqwartpK5E5LzKxBt19DgBv0";

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
