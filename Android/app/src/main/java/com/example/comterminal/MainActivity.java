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
    private FragmentTerminal terminalFragment; // Hold a reference to the Terminal fragment

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        // Initialize fragments
        terminalFragment = new FragmentTerminal();
        FragmentDatabaseView databaseFragment = new FragmentDatabaseView(); // Новый фрагмент

        // Set the adapter for ViewPager2
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
                        if (bluetoothSocket != null) {
                            terminalFragment.setBluetoothSocket(bluetoothSocket);
                        }
                        return terminalFragment;
                    case 3:
                        return databaseFragment; // Новый фрагмент для просмотра базы данных
                    default:
                        throw new IllegalArgumentException("Invalid position");
                }
            }

            @Override
            public int getItemCount() {
                return 4; // Обновляем количество вкладок
            }
        });

        // Link TabLayout with ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Bluetooth");
                    break;
                case 1:
                    tab.setText("Scan Devices");
                    break;
                case 2:
                    tab.setText("Terminal");
                    break;
                case 3:
                    tab.setText("Database View"); // Заголовок для нового фрагмента
                    break;
            }
        }).attach();
    }

    /**
     * Method to get the BluetoothSocket.
     */
    public BluetoothSocket getBluetoothSocket() {
        return bluetoothSocket;
    }

    /**
     * Update Bluetooth socket dynamically and notify the Terminal fragment.
     */
    public void setBluetoothSocket(BluetoothSocket socket) {
        this.bluetoothSocket = socket;
        if (terminalFragment != null) {
            terminalFragment.setBluetoothSocket(socket);
        }
    }
}
