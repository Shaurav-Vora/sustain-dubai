package com.example.sustaindubai;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
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
    private int lastDisplayedPoints = -1;
    private TextView tvPoints, tvCo2, tvWater, tvWaste, tvLevelLabel, tvNextMilestone;
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
        tvNextMilestone = view.findViewById(R.id.tvNextMilestone); // <--- ADD THIS LINE
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
        updateStats();
        maybeShowLevelUpBanner();
    }

    private void updateStats() {
        int newPoints = prefs.getPoints();
        int lastDisplayedPoints = prefs.getLastDisplayedPoints();

        // --- 1. POINTS TEXT ANIMATION ---
        if (lastDisplayedPoints == -1) {
            tvPoints.setText(String.valueOf(newPoints));
            prefs.setLastDisplayedPoints(newPoints);
        } else if (lastDisplayedPoints != newPoints) {
            animateTextChange(tvPoints, lastDisplayedPoints, newPoints);
        } else {
            tvPoints.setText(String.valueOf(newPoints));
        }

        // --- 2. IMPACT STATS ---
        tvCo2.setText(prefs.getCo2Saved() + " kg");
        tvWater.setText(prefs.getWaterSaved() + " L");
        tvWaste.setText(prefs.getWasteDiverted() + " kg");

        // --- 3. DYNAMIC LEVEL PROGRESS (The Fix) ---
        int pointsPerLevel = 500;
        int currentLevel = (newPoints / pointsPerLevel) + 1;
        int currentProgress = newPoints % pointsPerLevel;
        int pointsToNext = pointsPerLevel - currentProgress;

        // Update Label
        String levelName = getLevelName(currentLevel);
        tvLevelLabel.setText("Level " + currentLevel + " • " + levelName);

        // Update Helper Text (e.g., "120 pts to next level")
        tvNextMilestone.setText(pointsToNext + " pts to next reward");

        // ANIMATE THE BAR
        // Important: Set Max to 500 first!
        progressLevel.setMax(pointsPerLevel);

        // Animate from current visual state to new logical state
        ObjectAnimator animation = ObjectAnimator.ofInt(progressLevel, "progress", progressLevel.getProgress(), currentProgress);
        animation.setDuration(1200); // 1.2 seconds for a premium feel
        animation.setInterpolator(new DecelerateInterpolator()); // Starts fast, slows down at the end
        animation.start();
    }

//    private void updateStats() {
//        int newPoints = prefs.getPoints();
//        int lastDisplayedPoints = prefs.getLastDisplayedPoints();
//
//
//        // Determine starting point for animation
//        if (lastDisplayedPoints == -1) {
//            // First time ever - just set the value
//            tvPoints.setText(String.valueOf(newPoints));
//            prefs.setLastDisplayedPoints(newPoints);
//        } else if (lastDisplayedPoints != newPoints) {
//            animateTextChange(tvPoints, lastDisplayedPoints, newPoints);
//        } else {
//            tvPoints.setText(String.valueOf(newPoints));
//        }
//
//        // Update stats labels
//        tvCo2.setText(prefs.getCo2Saved() + " kg");
//        tvWater.setText(prefs.getWaterSaved() + " L");
//        tvWaste.setText(prefs.getWasteDiverted() + " kg");
//
//        // Update level label
//        int currentLevel = (newPoints / 500) + 1;
//        String levelName = getLevelName(currentLevel);
//        tvLevelLabel.setText("Level " + currentLevel + " • " + levelName);
//
//        // Smooth Progress Bar animation
//        int progressValue = newPoints % 500;
//        android.animation.ObjectAnimator.ofInt(progressLevel, "progress", progressLevel.getProgress(), progressValue)
//                .setDuration(1000)
//                .start();
//    }


    private void maybeShowLevelUpBanner() {
        if (prefs.hasPendingLevelUp()) {
            int currentLevel = (prefs.getPoints() / 500) + 1;
            String levelName = getLevelName(currentLevel);
            tvLevelUpBanner.setText("You're now a " + levelName + "!");

            // Modern bounce-in animation
            cardLevelUpBanner.setVisibility(View.VISIBLE);
            cardLevelUpBanner.setScaleX(0.8f);
            cardLevelUpBanner.setScaleY(0.8f);
            cardLevelUpBanner.setAlpha(0f);

            cardLevelUpBanner.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .alpha(1f)
                    .setDuration(400)
                    .setInterpolator(new android.view.animation.OvershootInterpolator(1.2f))
                    .start();

            prefs.setPendingLevelUp(false);
        }
    }

    private String getLevelName(int level) {
        switch (level) {
            case 1: return "Desert Seedling";
            case 2: return "Ghaf Protector";
            case 3: return "Oasis Guardian";
            case 4: return "Sustainability Champion";
            default: return "Eco Warrior Level " + level;
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
        animator.addUpdateListener(animation -> {
            tv.setText(animation.getAnimatedValue().toString());
        });
        animator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                // Save the final value after animation completes
                prefs.setLastDisplayedPoints(end);
            }
        });
        animator.start();
    }
}
