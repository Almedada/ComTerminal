package com.example.comterminal;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

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
                        return new FragmentTerminal();
                    default:
                        throw new IllegalArgumentException("Invalid position");
                }
            }

            @Override
            public int getItemCount() {
                return 3; // Три вкладки
            }
        });

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
            }
        }).attach();
    }
}
