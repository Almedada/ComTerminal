package com.example.comterminal.fragmentDatabase;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class DatabasePagerAdapter extends FragmentStateAdapter {

    public DatabasePagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new DevicesFragment();
            case 1:
                return new MessagesFragment();
            case 2:
                return new LogsFragment();
            case 3:
                return new DropboxUploadsFragment();
            default:
                return new DevicesFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 4;  // Четыре таблицы
    }
}
