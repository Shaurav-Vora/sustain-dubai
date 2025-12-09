package com.example.sustaindubai;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ActivitiesFragment extends Fragment implements SensorEventListener {

    private EcoPrefs prefs;
    private SensorManager sensorManager;
    private Sensor stepSensor;
    private boolean isSensorActive = false;

    // UI Elements
    private MaterialButton btnProveWalk, btnProveMetro, btnProveRecycle, btnProveWater;
    private TextView tvRecentLog;

    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());

    // Permission Launcher
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    startStepCounting();
                } else {
                    showSnack("Permission needed to count steps!");
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_activities, container, false);

        prefs = new EcoPrefs(requireContext());

        // Initialize Sensor Manager
        sensorManager = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        }

        // Bind UI Elements
        btnProveWalk = view.findViewById(R.id.btnProveWalk);
        btnProveMetro = view.findViewById(R.id.btnProveMetro);
        btnProveRecycle = view.findViewById(R.id.btnProveRecycle);
        btnProveWater = view.findViewById(R.id.btnProveWater);
        tvRecentLog = view.findViewById(R.id.tvRecentLog);

        // --- WALKING LOGIC (Step Counter) ---
        btnProveWalk.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACTIVITY_RECOGNITION)
                        != PackageManager.PERMISSION_GRANTED) {
                    requestPermissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION);
                } else {
                    startStepCounting();
                }
            } else {
                startStepCounting();
            }
        });

        // --- METRO/PUBLIC TRANSPORT ---
        btnProveMetro.setOnClickListener(v ->
                logActivity("Public Transport", 20, 3, 0, 0, "🚇"));

        // --- RECYCLING ---
        btnProveRecycle.setOnClickListener(v ->
                logActivity("Recycling", 500, 1, 0, 1, "♻️"));

        // --- WATER SAVING ---
        btnProveWater.setOnClickListener(v ->
                logActivity("Water Conservation", 15, 0, 10, 0, "💧"));

        return view;
    }

    // ==================== STEP COUNTING LOGIC ====================

    private void startStepCounting() {
        if (stepSensor == null) {
            // Fallback for devices without step sensor (like emulators)
            showSnack("No step sensor found. Simulating 1500 steps...");
            logWalkingPoints(1500);
            return;
        }

        showSnack("Syncing steps...");
        isSensorActive = true;
        sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (isSensorActive && event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            int currentSensorSteps = (int) event.values[0];
            int lastRedeemed = prefs.getLastRedeemedSteps();
            int newStepsToRedeem = currentSensorSteps - lastRedeemed;

            sensorManager.unregisterListener(this);
            isSensorActive = false;

            if (newStepsToRedeem > 0) {
                logWalkingPoints(newStepsToRedeem);
                prefs.setLastRedeemedSteps(currentSensorSteps);
            } else {
                showSnack("No new steps detected since last sync.");
            }
        }
    }

    private void logWalkingPoints(int newSteps) {
        int pointsEarned = newSteps / 100;

        if (pointsEarned == 0) {
            showSnack("Keep walking! You need " + (100 - newSteps) + " more steps for a point.");
            return;
        }

        // Check for level up BEFORE adding points
        int beforePoints = prefs.getPoints();
        int beforeLevel = (beforePoints / 500) + 1;

        // Add points and stats
        prefs.addPoints(pointsEarned);
        if (pointsEarned >= 10) {
            prefs.addCo2Saved(1);
        }

        // Check for level up AFTER adding points
        int afterPoints = prefs.getPoints();
        int afterLevel = (afterPoints / 500) + 1;

        if (afterLevel > beforeLevel) {
            prefs.setPendingLevelUp(true);
            prefs.setLastLevel(afterLevel);
        }

        // Update history
        addToHistory("🚶 Walking", pointsEarned, newSteps + " steps");

        String message = "Synced " + newSteps + " steps! +" + pointsEarned + " pts earned.";
        showSnack(message);
    }

    // ==================== GENERAL ACTIVITY LOGGING ====================

    private void logActivity(String activityName, int points, int co2Kg, int waterL, int wasteKg, String emoji) {
        // Check for level up BEFORE adding points
        int beforePoints = prefs.getPoints();
        int beforeLevel = (beforePoints / 500) + 1;

        // Add points and stats
        prefs.addPoints(points);
        prefs.addCo2Saved(co2Kg);
        prefs.addWaterSaved(waterL);
        prefs.addWasteDiverted(wasteKg);

        // Check for level up AFTER adding points
        int afterPoints = prefs.getPoints();
        int afterLevel = (afterPoints / 500) + 1;

        if (afterLevel > beforeLevel) {
            prefs.setPendingLevelUp(true);
            prefs.setLastLevel(afterLevel);
        }

        // Update history
        addToHistory(emoji + " " + activityName, points, null);

        // Show success message
        showSnack("Logged: " + activityName + " • +" + points + " points");
    }

    // ==================== ACTIVITY HISTORY ====================

    private void addToHistory(String activityLabel, int points, String extraInfo) {
        String timestamp = dateFormat.format(new Date());
        String newLine;

        if (extraInfo != null) {
            newLine = "• " + timestamp + " — " + activityLabel + " (" + extraInfo + ", +" + points + " pts)\n";
        } else {
            newLine = "• " + timestamp + " — " + activityLabel + " (+" + points + " pts)\n";
        }

        String existing = tvRecentLog.getText().toString();
        if (existing.contains("No activities")) {
            tvRecentLog.setText(newLine);
        } else {
            tvRecentLog.setText(newLine + existing);
        }
    }

    // ==================== HELPERS ====================

    private void showSnack(String message) {
        if (getView() != null) {
            Snackbar.make(getView(), message, Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not needed
    }

    @Override
    public void onPause() {
        super.onPause();
        if (sensorManager != null && isSensorActive) {
            sensorManager.unregisterListener(this);
            isSensorActive = false;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Optionally reload history from SharedPreferences if you store it
    }
}