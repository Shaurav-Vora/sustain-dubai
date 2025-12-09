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
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;

public class ActivitiesFragment extends Fragment implements SensorEventListener {

    private EcoPrefs prefs;
    private SensorManager sensorManager;
    private Sensor stepSensor;
    private boolean isSensorActive = false;

    // UI Elements
    private MaterialButton btnProveWalk;
    private TextView tvWalkStats; // You might want to add a text view to show live steps

    // Permission Launcher
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    startStepCounting();
                } else {
                    Toast.makeText(getContext(), "Permission needed to count steps!", Toast.LENGTH_SHORT).show();
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_activities, container, false); // Make sure this matches your XML name

        prefs = new EcoPrefs(requireContext());

        // Initialize Sensor Manager
        sensorManager = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        }

        // Bind Buttons (Use the IDs from your new dashboard XML)
        btnProveWalk = view.findViewById(R.id.btnProveWalk);

        // --- WALKING LOGIC ---
        btnProveWalk.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACTIVITY_RECOGNITION)
                        != PackageManager.PERMISSION_GRANTED) {
                    // Ask for permission
                    requestPermissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION);
                } else {
                    // Permission already granted
                    startStepCounting();
                }
            } else {
                // Old Android versions don't need runtime permission
                startStepCounting();
            }
        });

        // ... Bind other buttons (Metro, Recycle, etc.) here ...

        return view;
    }

    private void startStepCounting() {
        if (stepSensor == null) {
            Toast.makeText(getContext(), "No Step Sensor found on this device (Are you on Emulator?)", Toast.LENGTH_LONG).show();
            // Fallback for Emulator: Just simulate it
            logWalkingPoints(1500); // Pretend they walked 1500 steps
            return;
        }

        Toast.makeText(getContext(), "Syncing steps...", Toast.LENGTH_SHORT).show();
        isSensorActive = true;
        sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (isSensorActive && event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            // Android returns TOTAL steps since last reboot
            int totalStepsSinceReboot = (int) event.values[0];

            // Unregister immediately to save battery (we just wanted a sync check)
            sensorManager.unregisterListener(this);
            isSensorActive = false;

            // Log the points
            logWalkingPoints(totalStepsSinceReboot);
        }
    }

    private void logWalkingPoints(int steps) {
        // Calculate Points: e.g., 1 point for every 100 steps
        int pointsEarned = steps / 100;

        // Cap it for the demo (so they don't get 1 million points if they haven't rebooted in a year)
        if (pointsEarned > 50) pointsEarned = 50;

        prefs.addPoints(pointsEarned);
        prefs.addCo2Saved(1); // Add 1kg CO2 for effort

        String message = "Synced! " + steps + " steps detected. +" + pointsEarned + " pts earned.";
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not needed for step counting
    }

    @Override
    public void onPause() {
        super.onPause();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }
}