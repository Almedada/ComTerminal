package com.example.comterminal;

import android.os.Bundle;
import android.view.View;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.comterminal.fragmentDatabase.DatabasePagerAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class FragmentDatabaseView extends Fragment {

    public FragmentDatabaseView() {
        super(R.layout.fragment_database_view);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TabLayout tabLayout = view.findViewById(R.id.tabLayout);
        ViewPager2 viewPager = view.findViewById(R.id.viewPager);

        DatabasePagerAdapter pagerAdapter = new DatabasePagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0: tab.setText("Devices"); break;
                case 1: tab.setText("Messages"); break;
                case 2: tab.setText("Logs"); break;
                case 3: tab.setText("Dropbox Uploads"); break;
            }
        }).attach();
    }
}
