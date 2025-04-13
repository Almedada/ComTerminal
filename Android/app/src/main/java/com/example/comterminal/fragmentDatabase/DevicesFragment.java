package com.example.comterminal.fragmentDatabase;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.example.comterminal.R;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.room.Room;
import android.util.Log;

import com.example.comterminal.database.AppDatabase;
import com.example.comterminal.database.Device;

import java.util.List;

public class DevicesFragment extends Fragment {
    private AppDatabase database;
    private LinearLayout containerLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_devices, container, false);

        // Инициализация базы данных
        database = Room.databaseBuilder(requireContext(), AppDatabase.class, "device_database")
                .build();

        // Найдем элементы UI
        Button loadButton = view.findViewById(R.id.loadButton);
        Button clearButton = view.findViewById(R.id.clearButton);
        containerLayout = view.findViewById(R.id.containerLayout);

        // Кнопка "Загрузить устройства"
        loadButton.setOnClickListener(v -> loadData());

        // Кнопка "Очистить базу"
        clearButton.setOnClickListener(v -> clearDatabase());

        return view;
    }

    // Метод загрузки данных из базы
    private void loadData() {
        new Thread(() -> {
            List<Device> devices = database.deviceDao().getAllDevices();
            getActivity().runOnUiThread(() -> {
                containerLayout.removeAllViews();
                for (Device device : devices) {
                    // Форматируем строку, чтобы включить id устройства
                    String deviceInfo = "ID: " + device.getId() + " | " + device.getName() + " - " + device.getAddress()
                            + " | Добавлено: " + device.getFormattedTimestamp();

                    // Создаем TextView для отображения информации о каждом устройстве
                    TextView textView = new TextView(getContext());
                    textView.setText(deviceInfo);
                    textView.setTextSize(16);
                    textView.setPadding(8, 8, 8, 8);
                    containerLayout.addView(textView);
                }
            });
        }).start();
    }

    // Очистка базы данных
    private void clearDatabase() {
        new Thread(() -> {
            try {
                // Удаляем все записи из таблицы логов перед очисткой таблицы устройств
                database.logEntryDao().deleteAll();  // Очистить таблицу логов

                // Теперь можно безопасно удалить все записи из таблицы устройств
                database.deviceDao().deleteAll();  // Очистить таблицу устройств

                // Обновляем UI
                getActivity().runOnUiThread(() -> containerLayout.removeAllViews());
            } catch (Exception e) {
                Log.e("DevicesFragment", "Ошибка при очистке базы данных: ", e);
            }
        }).start();
    }

}
