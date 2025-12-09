package com.example.sustaindubai;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottom_nav);

        // Set Dashboard as default selected tab
        bottomNav.setSelectedItemId(R.id.nav_dashboard);

        // Load Dashboard fragment first
        loadFragment(new DashboardFragment());

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_dashboard) {
                selectedFragment = new DashboardFragment();
            } else if (itemId == R.id.nav_activities) {
                selectedFragment = new ActivitiesFragment();
            } else if (itemId == R.id.nav_rewards) {
                selectedFragment = new RewardsFragment();
            } else if (itemId == R.id.nav_news) {  // <--- ADD THIS BLOCK
                selectedFragment = new NewsFragment();
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment);
            }
            return true;
        });
    }

    // 8f487ba547cd4f59bab6ad50b6491d70

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }
}
