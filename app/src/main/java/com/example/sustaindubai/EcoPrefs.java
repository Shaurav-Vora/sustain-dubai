package com.example.sustaindubai;

import android.content.Context;
import android.content.SharedPreferences;

public class EcoPrefs {

    private static final String PREF_NAME = "SustainDubai_Data";
    private static final String KEY_LAST_DISPLAYED_POINTS = "last_displayed_points";
    private static final String KEY_POINTS = "points";
    private static final String KEY_CO2 = "co2_saved";
    private static final String KEY_WATER = "water_saved";
    private static final String KEY_WASTE = "waste_diverted";
    private static final String KEY_LAST_LEVEL = "last_level";
    private static final String KEY_PENDING_LEVEL = "pending_level_up";

    private final SharedPreferences prefs;

    public EcoPrefs(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public int getPoints() { return prefs.getInt(KEY_POINTS, 0); }
    public void addPoints(int delta) { prefs.edit().putInt(KEY_POINTS, getPoints() + delta).apply(); }

    public int getCo2Saved() { return prefs.getInt(KEY_CO2, 0); }
    public void addCo2Saved(int kg) { prefs.edit().putInt(KEY_CO2, getCo2Saved() + kg).apply(); }

    public int getLastDisplayedPoints() {
        return prefs.getInt(KEY_LAST_DISPLAYED_POINTS, -1);
    }

    public void setLastDisplayedPoints(int points) {
        prefs.edit().putInt(KEY_LAST_DISPLAYED_POINTS, points).apply();
    }

    public int getWaterSaved() { return prefs.getInt(KEY_WATER, 0); }
    public void addWaterSaved(int liters) { prefs.edit().putInt(KEY_WATER, getWaterSaved() + liters).apply(); }

    public int getWasteDiverted() { return prefs.getInt(KEY_WASTE, 0); }
    public void addWasteDiverted(int kg) { prefs.edit().putInt(KEY_WASTE, getWasteDiverted() + kg).apply(); }

    public int getLastLevel() { return prefs.getInt(KEY_LAST_LEVEL, 1); }
    public void setLastLevel(int level) { prefs.edit().putInt(KEY_LAST_LEVEL, level).apply(); }

    public boolean hasPendingLevelUp() { return prefs.getBoolean(KEY_PENDING_LEVEL, false); }
    public void setPendingLevelUp(boolean pending) { prefs.edit().putBoolean(KEY_PENDING_LEVEL, pending).apply(); }

    public void resetAll() { prefs.edit().clear().apply(); }
}