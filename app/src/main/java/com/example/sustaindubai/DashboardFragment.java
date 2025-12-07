package com.example.sustaindubai;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;

public class DashboardFragment extends Fragment {

    private EcoPrefs prefs;


    private TextView tvPoints, tvCo2, tvWater, tvWaste, tvLevelLabel;
    private MaterialCardView cardLevelUpBanner;
    private TextView tvLevelUpBanner;
    private ImageView btnCloseLevelUp;

    private ProgressBar progressLevel;
    private MaterialCardView cardLogActivity;
    private MaterialCardView cardViewRewards;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        prefs = new EcoPrefs(requireContext());

        tvPoints = view.findViewById(R.id.tvPoints);
        tvCo2 = view.findViewById(R.id.tvCo2);
        tvWater = view.findViewById(R.id.tvWater);
        tvWaste = view.findViewById(R.id.tvWaste);
        tvLevelLabel = view.findViewById(R.id.tvLevelLabel);
        progressLevel = view.findViewById(R.id.progressLevel);
        cardLogActivity = view.findViewById(R.id.cardLogActivity);
        cardViewRewards = view.findViewById(R.id.cardViewRewards);
        cardLevelUpBanner = view.findViewById(R.id.cardLevelUpBanner);
        tvLevelUpBanner = view.findViewById(R.id.tvLevelUpBanner);
        btnCloseLevelUp = view.findViewById(R.id.btnCloseLevelUp);

        btnCloseLevelUp.setOnClickListener(v -> cardLevelUpBanner.setVisibility(View.GONE));


        // Update UI with current data
        updateStats();

        // When user taps "Log activity", switch to Activities tab
        cardLogActivity.setOnClickListener(v -> switchBottomTab(R.id.nav_activities));

        // When user taps "View rewards", switch to Rewards tab
        cardViewRewards.setOnClickListener(v -> switchBottomTab(R.id.nav_rewards));

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateStats();
        maybeShowLevelUpBanner();
    }

    private void updateStats() {

        // 1. Get the current number showing on screen
        int startPoints;
        try {
            startPoints = Integer.parseInt(tvPoints.getText().toString());
        } catch (NumberFormatException e) {
            startPoints = 0;
        }

        // 2. Get the new values from the mock backend
        int points = prefs.getPoints();
        int co2 = prefs.getCo2Saved();
        int water = prefs.getWaterSaved();
        int waste = prefs.getWasteDiverted();

        // 3. Trigger the animation for points
        animateTextChange(tvPoints, startPoints, points);

        // 4. Update the other stats normally

        tvCo2.setText(co2 + " kg");
        tvWater.setText(water + " L");
        tvWaste.setText(waste + " kg");
        tvPoints.setText(String.valueOf(points));


        int level = (points / 500) + 1;
        tvLevelLabel.setText("Level " + level + " • Desert Seedling");

        int progressToNext = points % 500;
        progressLevel.setMax(500);
        progressLevel.setProgress(progressToNext);

        showLevelUpBanner(level);
    }

    // Inside your updateStats() method or wherever banner is shown:
    private void showLevelUpBanner(int currentLevel) {
        int lastLevel = prefs.getLastLevel();
        if (currentLevel > lastLevel) {
            prefs.setLastLevel(currentLevel);
            tvLevelUpBanner.setText("🎉 You reached Level " + currentLevel + "!");

            // Modern approach: Animated entrance
            cardLevelUpBanner.setVisibility(View.VISIBLE);
            cardLevelUpBanner.setAlpha(0f);
            cardLevelUpBanner.setTranslationY(-20f);
            cardLevelUpBanner.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(500)
                    .start();
        }
    }

    private void maybeShowLevelUpBanner() {
        int currentLevel = (prefs.getPoints() / 500) + 1;
        if (prefs.hasPendingLevelUp()) {
            tvLevelUpBanner.setText("🎉 You reached Level " + currentLevel + "!");
            cardLevelUpBanner.setVisibility(View.VISIBLE);
            prefs.setPendingLevelUp(false);
        } else {
            cardLevelUpBanner.setVisibility(View.GONE);
        }
    }


    private void switchBottomTab(int menuItemId) {
        if (getActivity() == null) return;
        BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottom_nav);
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(menuItemId);
        }
    }

    private void animateTextChange(TextView tv, int start, int end) {
        android.animation.ValueAnimator animator = android.animation.ValueAnimator.ofInt(start, end);
        animator.setDuration(800);
        animator.addUpdateListener(animation -> tv.setText(animation.getAnimatedValue().toString()));
        animator.start();
    }
}
