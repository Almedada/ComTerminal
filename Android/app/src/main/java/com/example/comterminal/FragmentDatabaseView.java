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

    private static final String ACCESS_TOKEN = "sl.u.AFmwaabmMbTnBA0GOVYOnoGwc6lyG3_MRLoRYTh7j7LWa3xuIFN9XFhsbKLgRqGXCb_Xr1E74L8ExcpAIkQ1zPT24HzwuZTezib_cBDu_Zl8RA2_74nQsdBJhtdsirnRBWywyP-cN_kB63WKHgV0i87MWndOdF_x4E11a_5E6FKnrB2F0SlNVf9DuTUFMRf20uakZL-zHyUG1wOF3lOU7-xM1ffdNRVqeP6B2XDx8Z_fkiS_8f2AvF9YMiEgN7WW1wYy4h2US9NflGi3pcfBm3R5GfJISEHpY0qALZLt3AAcnWK4HxqUNBFWsEDBBGPTk86TqRffdhut7aAIOMx9vM1G6e2kLLEB2VJVeDal64HgW6iCwC8LZsb4a61lw9WFmuPeFXjsYjExUPAw1C3W4NCR9dD1BQ2C-vXa4sF793imymZZlB3fA3dZ6phind3MNGwDfAK9CB24YGccSy_2BWOwRRjpC5sgHEombtFLOvbx123wPbAIH1z1ebrGRBqZeWuqaLfj_WCyO-vlTONZLvOycNfMwQL5Ipu7-bx5MAVlOh-krKIcGxXpmhk2mVfwUPui_jw1mNUz_VeuXU9B40VNbRta0TRRDXK5H8ASS90OcpqKWBljfx5QOuwvRi2IJyRyI6FVzrD4gpRKy1MrSj4vGy8pCpjAtk0fFPb5CbLT0D2oEm_9QJm5AKRr33gpgjrQtDW1Goq-1PawQy5A2i9nn8rFEmSEvUl_0M_B339ZwtHk88Kb5eZ6pEcLj_XzbOH2bbQ-gumGDcg04M6mJ3hZKmLQG2fZZw2VntYNo1ka4TXTPmkJiYJuhK21OC0bfFZWQicKyv2u6_-9V16SoA5ekNJgBWmKI_A9tzDHw1Y0T-PfdnopH3xXel9JVUBcCI4dkrxJipdTXAby-t-x5nm5PvSNzRfK7jl2QQzkdaCmTkLIHA4ccyRMOkC9JyYfUyHd40Pe9xwo8oa8lgjkK_R_yrnQuCCzGj9o3V271_W5xEeW-s9hx008aMomItjaLybjL54OT2Lr9OKaUAZvecgPGdy9iZNWoEipc_oGiYUBA-HOLUPbO1e9IrPHRX9LGoVboEIRRRPYO97HR4tcPmSNH8IkVk1gO64ZjlxnuY5hTBF_1Q8w3TzYBeI2xSyE0RCMv8wiEwmXcDD7CwRR4vzH92iIAuRWW3n9_ym4nVP2JU3_dPAu2Qev1J0d-fE2jby2lzI9nDHSYOkAcIGzlHQcYVwdVgaqzM8u5NIqUWHOVF--pLB0he5cRsEeoyITb15j2rjK4RH8kM9KIYCNUuKdk8MwIQE7S_8zKw5cuS6HpUvofwe6c_NEPfQSvkaEfJA";

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
                writer.append("ID,Message,Timestamp\n");
                writer.append(message.id + "," + message.message.replace(",", " ") + "," + message.timestamp + "\n");
                writer.flush();
            } catch (IOException e) {
                handler.post(() -> Toast.makeText(getContext(), "Ошибка при создании файла", Toast.LENGTH_SHORT).show());
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
