package com.example.comterminal;

import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class MainActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private BluetoothSocket bluetoothSocket;
    private FragmentTerminal terminalFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        // Инициализация фрагментов
        terminalFragment = new FragmentTerminal();
        FragmentDatabaseView databaseFragment = new FragmentDatabaseView();

        // Устанавливаем адаптер для ViewPager2
        viewPager.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                switch (position) {
                    case 0:
                        return new FragmentBluetooth();
                    case 1:
                        return new FragmentDeviceScan();
                    case 2:
                        // Если Bluetooth-сокет существует, передаем его в терминал
                        if (bluetoothSocket != null) {
                            terminalFragment.setBluetoothSocket(bluetoothSocket);
                        }
                        return terminalFragment;
                    case 3:
                        return databaseFragment;
                    default:
                        throw new IllegalArgumentException("Invalid position");
                }
            }

            @Override
            public int getItemCount() {
                return 4; // Общее количество вкладок
            }
        });

        // Связываем TabLayout с ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            // Устанавливаем текст для каждой вкладки в зависимости от позиции
            switch (position) {
                case 0:
                    tab.setText("Bluetooth");
                    break;
                case 1:
                    tab.setText("Поиск устройств");
                    break;
                case 2:
                    tab.setText("Терминал");
                    break;
                case 3:
                    tab.setText("База данных");
                    break;
            }
        }).attach(); // Подключаем TabLayout и ViewPager2
    }

    public BluetoothSocket getBluetoothSocket() {
        return bluetoothSocket;
    }

    public void setBluetoothSocket(BluetoothSocket socket) {
        this.bluetoothSocket = socket;
        // Если фрагмент терминала уже создан, обновляем его сокет
        if (terminalFragment != null) {
            terminalFragment.setBluetoothSocket(socket);
        }
    }
}
