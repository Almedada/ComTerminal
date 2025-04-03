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
import android.widget.Toast;
import android.util.Log;

import androidx.fragment.app.Fragment;

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.WriteMode;
import com.example.comterminal.database.AppDatabase;
import com.example.comterminal.database.TerminalMessage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class FragmentDatabaseView extends Fragment {

    private Handler handler;
    private AppDatabase database;
    private LinearLayout containerLayout;

    private static final String ACCESS_TOKEN = "sl.u.AFo-pcLnK_lMKcZMFZeJCE0yhTlhYPyURN22PNWjxMgJySuG5u1ZcbwDpr50dp3YgcPTIrZW_1wMR-f2i188WW1grsGTmJbDlHFI_bS1lke-ML1OaReXTaxhrYn6XN0gMWo8FYuN95RXg8-jvYN42-7Djm1K85uYtvJF8JSLmaKrA869f_NjBbQwm5UwEwp7C1IT3wkThpG88iUZspIf2zYw1HbVtgZDXA3NLgn2Mde2xJGrV5GfRTMOsi7GnUp1ZzMWTcLSfwakuHTln1bjaI_ZP0l6fS63-BuPZ80q8U-8NGqpN_jmR6TUYEKblSZUYS2jk67ZCo6fpWccfFlQA2PEvo5xs2HoVCI4VqNwGNxLoG4HAz3vJ2J0Q9PBoKYmxR3M-iVgASZjDbeWphtFp3332wJ07ksaLahvP8vb6bzpN-7HYckvt85p86U71-haCkiJs9TaAm66NwH0CE6UpqjryZig79vqQTami7Dohps_tXjlECa2sikN6CmH2swEHk_hwyaRfjSNY4xGAf2Ecv7cu0KPMGMigXIgPZXkTnr70sDUkqe1rw72eSOBewXJzS0-0PtODGVWriv13TpHJnQMZhb9llzY1_MqIXxppmEL9dmVIVKp0sTf77BwEwOz8rGi5rJa9ZrgnCtekFccgGsIViFRU4i7ljmh8JK-C2FMg66U5isb3wqE88S8Ns1KROpI-IpSUUzdj5fug8ydVdjGVloSNA5K2gfya3mvTDDgkeOlwH3lV_NXa01_9EFmnTUWW3x7QXvF_uSJPmCD53V0JEX1_8gl8TJZB4gHyuT8GPYHxACL46Fh6q3lI5cgcD4GrXb8SSRd_Vp8gfvpPINuxFGcNLGx2iTm6PTS2E_sBcIZHcPArecJezvZohNg_RK6jM5o9kMPMu1B4JupYjiwL3JCYaPx4WdMkBOaZON6_FILIaXS_mbD9S_j5zLqcFRbENsDodvQu7-WorQ1knyAGXhKFc_XgX44SmKv59RHuk5Ugq98sZMMi3Zf2UH-uHLVK34Cpr88IPzeF0o5sXjbsS9qrKjW1X_Wf9tbM5lbWK6jKHZK81tQL4CrXWZmrD-SR-_7nU1eg6o_ggs-tbPD0oUTPEhL2NxA-3EdSgL2UDGAzFLnIlS6eDro6i5wdj1v4lrtFg24_u-qrzW1eWw9Vm5CgU47eHvVUuD33keFJQUcNcH5A0_cGZyDU5b543idhkoibr9dOxsfyOsAY9OArDpTl3-hfe-DfY40h2p8IxOjKkuhsc6hAm3XkxPcgTfnEQYtIFiEQSQbdby01k3kugnbMKM24whdDQ9ifJ9xOtXOezQW7Dtg9eMvh6t813E";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_database_view, container, false);

        containerLayout = view.findViewById(R.id.containerLayout);
        Button loadButton = view.findViewById(R.id.loadButton);
        Button clearButton = view.findViewById(R.id.clearButton);

        handler = new Handler();
        database = AppDatabase.getInstance(requireContext());

        loadButton.setOnClickListener(v -> loadData());
        clearButton.setOnClickListener(v -> clearDatabase());

        return view;
    }

    private void loadData() {
        new Thread(() -> {
            List<TerminalMessage> messages = database.terminalMessageDao().getAllMessages();
            handler.post(() -> {
                containerLayout.removeAllViews();

                for (TerminalMessage message : messages) {
                    String formattedTime = DateFormat.format("yyyy-MM-dd HH:mm:ss", message.timestamp).toString();
                    String fullMessage = formattedTime + " - " + message.message;

                    LinearLayout messageLayout = new LinearLayout(getContext());
                    messageLayout.setOrientation(LinearLayout.HORIZONTAL);
                    messageLayout.setPadding(0, 8, 0, 8);

                    TextView messageTextView = new TextView(getContext());
                    messageTextView.setText(fullMessage);
                    messageTextView.setLayoutParams(new LinearLayout.LayoutParams(
                            0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

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
            });
        }).start();
    }

    private void deleteMessage(TerminalMessage message) {
        new Thread(() -> {
            database.terminalMessageDao().deleteById(message.id);
            handler.post(() -> {
                Toast.makeText(getContext(), "Сообщение удалено", Toast.LENGTH_SHORT).show();
                loadData();
            });
        }).start();
    }

    private void clearDatabase() {
        new Thread(() -> {
            database.terminalMessageDao().deleteAll();
            handler.post(() -> {
                containerLayout.removeAllViews();
                Toast.makeText(getContext(), "Все данные удалены", Toast.LENGTH_SHORT).show();
            });
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
                handler.post(() -> Toast.makeText(getContext(),  "Ошибка при создании файла", Toast.LENGTH_SHORT).show());
                return;
            }

            uploadToDropbox(csvFile);
        }).start();
    }

    private void uploadToDropbox(File file) {
        new Thread(() -> {
            try {
                DbxRequestConfig config = DbxRequestConfig.newBuilder("dropbox/app").build();
                DbxClientV2 client = new DbxClientV2(config, ACCESS_TOKEN);

                try (InputStream in = new FileInputStream(file)) {
                    client.files().uploadBuilder("/" + file.getName())
                            .withMode(WriteMode.OVERWRITE)
                            .uploadAndFinish(in);
                }

                handler.post(() -> Toast.makeText(getContext(), "Файл загружен: " + file.getName(), Toast.LENGTH_SHORT).show());
            } catch (Exception e) {
                Log.e("DropboxError", "Ошибка загрузки: " + e.getMessage(), e);
                handler.post(() -> Toast.makeText(getContext(), "Ошибка загрузки в Dropbox", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}
