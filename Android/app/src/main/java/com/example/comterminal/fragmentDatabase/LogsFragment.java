package com.example.comterminal.fragmentDatabase;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.comterminal.R;
import android.util.Log;

import com.example.comterminal.database.AppDatabase;
import com.example.comterminal.database.LogEntry;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LogsFragment extends Fragment {

    private AppDatabase database;
    private LinearLayout containerLayout;
    private Handler handler;

    public LogsFragment() {
        super(R.layout.fragment_logs);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        containerLayout = view.findViewById(R.id.containerLayout);
        Button loadButton = view.findViewById(R.id.loadButton);
        Button clearButton = view.findViewById(R.id.clearButton);  // Кнопка очистки логов

        handler = new Handler();
        database = AppDatabase.getInstance(requireContext());

        loadButton.setOnClickListener(v -> loadData());

        clearButton.setOnClickListener(v -> clearLogs());
    }

    private void loadData() {
        new Thread(() -> {
            List<LogEntry> logs = database.logEntryDao().getAllLogs();
            Log.d("LogsFragment", "Logs loaded: " + logs.size());
            handler.post(() -> {
                containerLayout.removeAllViews();
                for (LogEntry log : logs) {
                    addLogToLayout(log);
                }
            });
        }).start();
    }

    private void addLogToLayout(LogEntry log) {
        String formattedTime = formatTimestamp(log.timestamp);
        String logInfo = "ID устройства: " + log.device_id + " | " + formattedTime + " - " + log.status;

        Log.d("LogsFragment", "Adding log: " + logInfo);

        LinearLayout logLayout = new LinearLayout(getContext());
        logLayout.setOrientation(LinearLayout.HORIZONTAL);
        logLayout.setPadding(0, 8, 0, 8);

        TextView logTextView = new TextView(getContext());
        logTextView.setText(logInfo);
        logTextView.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        Button deleteButton = new Button(getContext());
        deleteButton.setText("Удалить");
        deleteButton.setOnClickListener(v -> deleteLog(log));

        logLayout.addView(logTextView);
        logLayout.addView(deleteButton);
        containerLayout.addView(logLayout);
    }

    private String formatTimestamp(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    private void deleteLog(LogEntry log) {
        new Thread(() -> {
            database.logEntryDao().delete(log);
            handler.post(() -> loadData());
        }).start();
    }

    private void clearLogs() {
        new Thread(() -> {
            try {
                database.logEntryDao().deleteAll();
                handler.post(() -> loadData());
            } catch (Exception e) {
                Log.e("LogsFragment", "Ошибка при очистке логов: ", e);
            }
        }).start();
    }
}
