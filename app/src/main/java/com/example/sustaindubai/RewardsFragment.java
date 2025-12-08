package com.example.sustaindubai;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.Locale;

public class RewardsFragment extends Fragment {

    private EcoPrefs prefs;
    private TextView tvPointsBalance;

    // Define Buttons for all 7 rewards in your XML
    private MaterialButton btnRedeemNol;
    private MaterialButton btnRedeemCareem;
    private MaterialButton btnRedeemCoffee;
    private MaterialButton btnRedeemMarket;
    private MaterialButton btnRedeemTree;
    private MaterialButton btnRedeemMangrove;
    private MaterialButton btnRedeemEcoPack;

    // Costs updated to match your XML text
    private static final int COST_NOL = 500;
    private static final int COST_CAREEM = 800;
    private static final int COST_COFFEE = 300;
    private static final int COST_MARKET = 1200;
    private static final int COST_TREE = 800;
    private static final int COST_MANGROVE = 1000;
    private static final int COST_ECOPACK = 1500;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rewards, container, false);

        prefs = new EcoPrefs(requireContext());

        // 1. Bind Views
        tvPointsBalance = view.findViewById(R.id.tvPointsBalance);

        btnRedeemNol = view.findViewById(R.id.btnRedeemNol);
        btnRedeemCareem = view.findViewById(R.id.btnRedeemCareem);
        btnRedeemCoffee = view.findViewById(R.id.btnRedeemCoffee);
        btnRedeemMarket = view.findViewById(R.id.btnRedeemMarket);
        btnRedeemTree = view.findViewById(R.id.btnRedeemTree);
        btnRedeemMangrove = view.findViewById(R.id.btnRedeemMangrove);
        btnRedeemEcoPack = view.findViewById(R.id.btnRedeemEcoPack);

        // 2. Transport Category
        btnRedeemNol.setOnClickListener(v ->
                redeem("10 AED Nol Credit", COST_NOL, "NOL-" + generateCode()));

        btnRedeemCareem.setOnClickListener(v ->
                redeem("Careem Electric Ride", COST_CAREEM, "CAR-" + generateCode()));

        // 3. Food & Beverage Category
        btnRedeemCoffee.setOnClickListener(v ->
                redeem("Coffee Discount", COST_COFFEE, "CAF-" + generateCode()));

        btnRedeemMarket.setOnClickListener(v ->
                redeem("Ripe Market Voucher", COST_MARKET, "RIPE-" + generateCode()));

        // 4. Environmental Category
        btnRedeemTree.setOnClickListener(v ->
                redeem("Ghaf Tree Planting", COST_TREE, "GHAF-" + generateCode()));

        btnRedeemMangrove.setOnClickListener(v ->
                redeem("Mangrove Protection", COST_MANGROVE, "GROV-" + generateCode()));

        // 5. Lifestyle Category
        btnRedeemEcoPack.setOnClickListener(v ->
                redeem("Zero-Waste Kit", COST_ECOPACK, "KIT-" + generateCode()));

        updateBalanceLabel(false); // false = no animation on initial load
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateBalanceLabel(true); // true = animate numbers when returning to screen
    }

    private void updateBalanceLabel(boolean animate) {
        int targetPoints = prefs.getPoints();

        if (animate) {
            // Get current displayed value (or 0 if error)
            int startPoints = 0;
            try {
                String currentText = tvPointsBalance.getText().toString().replace(" pts", "");
                startPoints = Integer.parseInt(currentText);
            } catch (NumberFormatException e) {
                startPoints = 0;
            }

            // Animate from old value to new value
            ValueAnimator animator = ValueAnimator.ofInt(startPoints, targetPoints);
            animator.setDuration(800);
            animator.addUpdateListener(animation ->
                    tvPointsBalance.setText(animation.getAnimatedValue() + " pts"));
            animator.start();
        } else {
            tvPointsBalance.setText(targetPoints + " pts");
        }
    }

    private void redeem(String rewardName, int cost, String code) {
        int current = prefs.getPoints();

        // 1. Check Funds
        if (current < cost) {
            int needed = cost - current;
            showSnack("Not enough points! You need " + needed + " more.");
            return;
        }

        // 2. Deduct Points
        prefs.addPoints(-cost);

        // 3. Show Success & Code
        // We use a Dialog or a Long Snackbar because tvLastRedeemed was removed
        String successMsg = "Redeemed: " + rewardName + "\nCode: " + code;

        Snackbar snackbar = Snackbar.make(requireView(), successMsg, Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction("COPY", v -> {
            // Optional: Copy to clipboard logic could go here
            Toast.makeText(getContext(), "Code Copied!", Toast.LENGTH_SHORT).show();
        });
        snackbar.show();

        // 4. Update UI
        updateBalanceLabel(true);
    }

    private void showSnack(String msg) {
        if (getView() != null) {
            Snackbar.make(getView(), msg, Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(getResources().getColor(android.R.color.holo_red_dark))
                    .setTextColor(getResources().getColor(android.R.color.white))
                    .show();
        }
    }

    // Helper to generate a random 5 digit code
    private String generateCode() {
        return String.valueOf(System.currentTimeMillis() % 100000);
    }
}