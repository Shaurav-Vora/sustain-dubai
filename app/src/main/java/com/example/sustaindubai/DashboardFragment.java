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

        btnCloseLevelUp.setOnClickListener(v -> {
            cardLevelUpBanner.animate()
                    .translationY(-300f)
                    .alpha(0f)
                    .setDuration(400)
                    .withEndAction(() -> cardLevelUpBanner.setVisibility(View.GONE))
                    .start();
        });

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
        new android.os.Handler().postDelayed(this::updateStats, 300);
        maybeShowLevelUpBanner();
    }

    private void updateStats() {
        // 1. Logic for points count-up
        int oldPoints;
        try {
            oldPoints = Integer.parseInt(tvPoints.getText().toString());
        } catch (Exception e) { oldPoints = 0; }

        int newPoints = prefs.getPoints();
        animateTextChange(tvPoints, oldPoints, newPoints);

        // 2. Update stats labels
        tvCo2.setText(prefs.getCo2Saved() + " kg");
        tvWater.setText(prefs.getWaterSaved() + " L");
        tvWaste.setText(prefs.getWasteDiverted() + " kg");

        // 3. Smooth Progress Bar Fill
        android.animation.ObjectAnimator.ofInt(progressLevel, "progress", progressLevel.getProgress(), (newPoints % 500))
                .setDuration(1000)
                .start();
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
        if (prefs.hasPendingLevelUp()) {
            int currentLevel = (prefs.getPoints() / 500) + 1;
            tvLevelUpBanner.setText("🎉 You reached Level " + currentLevel + "!");

            // Modern Slide-In Animation
            cardLevelUpBanner.setVisibility(View.VISIBLE);
            cardLevelUpBanner.setTranslationY(-200f); // Start above screen
            cardLevelUpBanner.animate()
                    .translationY(0)
                    .setDuration(600)
                    .setInterpolator(new android.view.animation.OvershootInterpolator())
                    .start();

            prefs.setPendingLevelUp(false);
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
