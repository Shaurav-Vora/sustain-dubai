package com.example.sustaindubai;

import android.animation.ValueAnimator;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

public class RewardsFragment extends Fragment {

    private EcoPrefs prefs;
    private TextView tvPointsBalance;

    // Button definitions
    private MaterialButton btnRedeemNol, btnRedeemCareem, btnRedeemCoffee,
            btnRedeemMarket, btnRedeemTree, btnRedeemMangrove, btnRedeemEcoPack;

    // --- UPDATED COSTS TO MATCH XML ---
    private static final int COST_NOL = 500;
    private static final int COST_CAREEM = 800;
    private static final int COST_COFFEE = 300;
    private static final int COST_MARKET = 1200;
    private static final int COST_TREE = 800;
    private static final int COST_MANGROVE = 12500; // Updated from 1000 to match EV Discount
    private static final int COST_ECOPACK = 1500;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rewards, container, false);

        prefs = new EcoPrefs(requireContext());
        tvPointsBalance = view.findViewById(R.id.tvPointsBalance);

        // Bind Buttons
        btnRedeemNol = view.findViewById(R.id.btnRedeemNol);
        btnRedeemCareem = view.findViewById(R.id.btnRedeemCareem);
        btnRedeemCoffee = view.findViewById(R.id.btnRedeemCoffee);
        btnRedeemMarket = view.findViewById(R.id.btnRedeemMarket);
        btnRedeemTree = view.findViewById(R.id.btnRedeemTree);
        btnRedeemMangrove = view.findViewById(R.id.btnRedeemMangrove); // Maps to EV Card in XML
        btnRedeemEcoPack = view.findViewById(R.id.btnRedeemEcoPack);

        // --- SETUP LISTENERS WITH UPDATED TERMS ---

        btnRedeemNol.setOnClickListener(v -> redeem(
                "10 AED Nol Credit",
                COST_NOL,
                "NOL-" + generateCode(),
                "Redeemable at any RTA ticket machine. Valid for 30 days."
        ));

        btnRedeemCareem.setOnClickListener(v -> redeem(
                "Careem Electric Ride",
                COST_CAREEM,
                "CAR-" + generateCode(),
                "Minimum ride fare 50 AED. Valid on 'Hala Eco' and 'Electric' ride types only."
        ));

        btnRedeemCoffee.setOnClickListener(v -> redeem(
                "Coffee Discount",
                COST_COFFEE,
                "CAF-" + generateCode(),
                "Valid only when using a personal reusable cup. Participating locations only."
        ));

        btnRedeemMarket.setOnClickListener(v -> redeem(
                "Ripe Market Voucher",
                COST_MARKET,
                "RIPE-" + generateCode(),
                "Valid at Ripe Market Academy Park on weekends. Minimum spend 100 AED."
        ));

        btnRedeemTree.setOnClickListener(v -> redeem(
                "Ghaf Tree Planting",
                COST_TREE,
                "GHAF-" + generateCode(),
                "Certificate of planting will be emailed to your registered address within 48 hours."
        ));

        // UPDATED: Now handles Electric Vehicle Discount
        btnRedeemMangrove.setOnClickListener(v -> redeem(
                "Electric Vehicle Discount",
                COST_MANGROVE,
                "EV-" + generateCode(),
                "15% off purchase at partner dealerships. Valid for 6 months. Terms apply."
        ));

        btnRedeemEcoPack.setOnClickListener(v -> redeem(
                "Zero-Waste Starter Kit",
                COST_ECOPACK,
                "KIT-" + generateCode(),
                "Pick up available at nearest distribution center. Bring ID for verification."
        ));

        updateBalanceLabel(false);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateBalanceLabel(true);
    }

    private void updateBalanceLabel(boolean animate) {
        int targetPoints = prefs.getPoints();
        if (animate) {
            int startPoints = 0;
            try {
                String currentText = tvPointsBalance.getText().toString().replace(" pts", "");
                startPoints = Integer.parseInt(currentText);
            } catch (NumberFormatException e) { startPoints = 0; }

            ValueAnimator animator = ValueAnimator.ofInt(startPoints, targetPoints);
            animator.setDuration(800);
            animator.addUpdateListener(animation ->
                    tvPointsBalance.setText(animation.getAnimatedValue() + " pts"));
            animator.start();
        } else {
            tvPointsBalance.setText(targetPoints + " pts");
        }
    }

    private void redeem(String rewardName, int cost, String code, String terms) {
        int current = prefs.getPoints();

        // 1. Check Funds
        if (current < cost) {
            int needed = cost - current;
            Snackbar.make(requireView(), "Need " + needed + " more points!", Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(getResources().getColor(android.R.color.holo_red_dark))
                    .setTextColor(getResources().getColor(android.R.color.white))
                    .show();
            return;
        }

        // 2. Deduct Points
        prefs.addPoints(-cost);

        // 3. Update UI Balance
        updateBalanceLabel(true);

        // 4. Show Modern Success Sheet
        showRedeemSuccessSheet(rewardName, code, terms);
    }

    private void showRedeemSuccessSheet(String rewardName, String code, String terms) {
        // Use default constructor for Material 3 auto-styling
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
        View sheetView = LayoutInflater.from(getContext()).inflate(R.layout.layout_redeem_sheet, null);

        // Bind Views
        TextView tvRewardName = sheetView.findViewById(R.id.tvRewardName);
        TextView tvCode = sheetView.findViewById(R.id.tvCode);
        TextView tvTerms = sheetView.findViewById(R.id.tvTerms);
        MaterialButton btnCopy = sheetView.findViewById(R.id.btnCopyCode);
        MaterialButton btnClose = sheetView.findViewById(R.id.btnCloseSheet);

        // Set Data
        tvRewardName.setText(rewardName);
        tvCode.setText(code);
        tvTerms.setText(terms);

        // Logic: Copy Code
        btnCopy.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Promo Code", code);
            if (clipboard != null) {
                clipboard.setPrimaryClip(clip);
                Toast.makeText(getContext(), "Code Copied!", Toast.LENGTH_SHORT).show();
            }
            bottomSheetDialog.dismiss();
        });

        // Logic: Close
        btnClose.setOnClickListener(v -> bottomSheetDialog.dismiss());

        bottomSheetDialog.setContentView(sheetView);
        bottomSheetDialog.show();
    }

    private String generateCode() {
        return String.valueOf(System.currentTimeMillis() % 100000);
    }
}