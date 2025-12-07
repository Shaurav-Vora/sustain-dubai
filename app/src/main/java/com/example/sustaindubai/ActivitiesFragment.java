package com.example.sustaindubai;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ActivitiesFragment extends Fragment {

    private EcoPrefs prefs;

    private Button btnProveWalk;
    private Button btnProveMetro;
    private Button btnProveRecycle;
    private Button btnProveWater;
    private TextView tvRecentLog;

    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_activities, container, false);

        prefs = new EcoPrefs(requireContext());

        btnProveWalk = view.findViewById(R.id.btnProveWalk);
        btnProveMetro = view.findViewById(R.id.btnProveMetro);
        btnProveRecycle = view.findViewById(R.id.btnProveRecycle);
        btnProveWater = view.findViewById(R.id.btnProveWater);
        tvRecentLog = view.findViewById(R.id.tvRecentLog);

        // For now, all "Prove" buttons directly log the activity (fake verification)
        btnProveWalk.setOnClickListener(v ->
                logActivity("Walked instead of driving", 10, 1, 0, 0));

        btnProveMetro.setOnClickListener(v ->
                logActivity("Used metro / tram / bus", 20, 3, 0, 0));

        btnProveRecycle.setOnClickListener(v ->
                logActivity("Recycled bottles / cans", 500, 1, 0, 1));

        btnProveWater.setOnClickListener(v ->
                logActivity("Saved water", 15, 0, 10, 0));

        return view;
    }

    private void logActivity(String label, int points, int co2Kg, int waterL, int wasteKg) {
        // Update fake backend

        int beforePoints = prefs.getPoints();
        int beforeLevel = (beforePoints / 500) + 1;


        prefs.addPoints(points);
        prefs.addCo2Saved(co2Kg);
        prefs.addWaterSaved(waterL);
        prefs.addWasteDiverted(wasteKg);

        int afterPoints = prefs.getPoints();
        int afterLevel = (afterPoints / 500) + 1;

        if (afterLevel > beforeLevel) {
            prefs.setPendingLevelUp(true);
            prefs.setLastLevel(afterLevel);
        }

        // Time-stamped history line
        String timestamp = dateFormat.format(new Date());
        String newLine = "• " + timestamp + " — " + label + " (+" + points + " pts)\n";

        String existing = tvRecentLog.getText().toString();
        if (existing.startsWith("No activities")) {
            tvRecentLog.setText(newLine);
        } else {
            tvRecentLog.setText(newLine + existing);
        }

        // Feedback toast/snackbar
        if (getView() != null) {
            Snackbar.make(getView(),
                    "Logged: " + label + " · +" + points + " points",
                    Snackbar.LENGTH_SHORT).show();
        }
    }
}
