package com.example.comterminal.fragmentDatabase;

import android.os.Bundle;
import android.os.Handler;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.comterminal.R;
import com.example.comterminal.database.AppDatabase;
import com.example.comterminal.database.DropboxUpload;

import java.util.List;

public class DropboxUploadsFragment extends Fragment {

    private Handler handler;
    private AppDatabase database;
    private LinearLayout containerLayout;

    public DropboxUploadsFragment() {
        super(R.layout.fragment_dropbox_uploads);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        containerLayout = view.findViewById(R.id.containerLayout);
        Button loadButton = view.findViewById(R.id.loadButton);
        Button clearButton = view.findViewById(R.id.clearButton);

        handler = new Handler();
        database = AppDatabase.getInstance(requireContext());

        loadButton.setOnClickListener(v -> loadUploads());
        clearButton.setOnClickListener(v -> clearUploads());
    }

    private void loadUploads() {
        new Thread(() -> {
            List<DropboxUpload> uploads = database.dropboxUploadDao().getAllUploads();
            handler.post(() -> {
                containerLayout.removeAllViews();

                for (DropboxUpload upload : uploads) {
                    String formattedTime = DateFormat.format("yyyy-MM-dd HH:mm:ss", upload.timestamp).toString();
                    // Формируем полное сообщение с message_id
                    String fullMessage = "ID сообщения: " + upload.message_id + " - Статус: " + upload.status + " - Время: " + formattedTime;

                    LinearLayout uploadLayout = new LinearLayout(getContext());
                    uploadLayout.setOrientation(LinearLayout.HORIZONTAL);
                    uploadLayout.setPadding(0, 8, 0, 8);

                    TextView uploadTextView = new TextView(getContext());
                    uploadTextView.setText(fullMessage);
                    uploadTextView.setLayoutParams(new LinearLayout.LayoutParams(
                            0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

                    Button deleteButton = new Button(getContext());
                    deleteButton.setText("Удалить");
                    deleteButton.setOnClickListener(v -> deleteUpload(upload));

                    uploadLayout.addView(uploadTextView);
                    uploadLayout.addView(deleteButton);
                    containerLayout.addView(uploadLayout);
                }
            });
        }).start();
    }

    private void deleteUpload(DropboxUpload upload) {
        new Thread(() -> {
            database.dropboxUploadDao().delete(upload);
            handler.post(() -> {
                Toast.makeText(getContext(), "Загрузка удалена", Toast.LENGTH_SHORT).show();
                loadUploads();
            });
        }).start();
    }

    private void clearUploads() {
        new Thread(() -> {
            database.dropboxUploadDao().deleteAllUploads();
            handler.post(() -> {
                containerLayout.removeAllViews();
                Toast.makeText(getContext(), "Все загрузки удалены", Toast.LENGTH_SHORT).show();
            });
        }).start();
    }
}
