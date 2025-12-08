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

public class RewardsFragment extends Fragment {

    private EcoPrefs prefs;

    private TextView tvPointsBalance;
    private TextView tvLastRedeemed;

    private Button btnRedeemNol;
    private Button btnRedeemCoffee;
    private Button btnRedeemTree;
    private Button btnRedeemEcoPack;

    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());

    private static final int COST_NOL = 500;
    private static final int COST_COFFEE = 300;
    private static final int COST_TREE = 800;
    private static final int COST_ECOPACK = 1000;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rewards, container, false);

        prefs = new EcoPrefs(requireContext());

        tvPointsBalance = view.findViewById(R.id.tvPointsBalance);
//        tvLastRedeemed = view.findViewById(R.id.tvLastRedeemed);

        btnRedeemNol = view.findViewById(R.id.btnRedeemNol);
        btnRedeemCoffee = view.findViewById(R.id.btnRedeemCoffee);
        btnRedeemTree = view.findViewById(R.id.btnRedeemTree);
        btnRedeemEcoPack = view.findViewById(R.id.btnRedeemEcoPack);

        btnRedeemNol.setOnClickListener(v ->
                redeem("10 AED Nol top‑up", COST_NOL, "NOL-" + System.currentTimeMillis() % 100000));

        btnRedeemCoffee.setOnClickListener(v ->
                redeem("Sustainable coffee voucher", COST_COFFEE, "CAF-" + System.currentTimeMillis() % 100000));

        btnRedeemTree.setOnClickListener(v -> {
            redeem("Plant a Ghaf tree", COST_TREE, "GHAF-" + System.currentTimeMillis() % 100000);
            // optional: could also bump a 'trees planted' stat here later
        });

        btnRedeemEcoPack.setOnClickListener(v ->
                redeem("Home eco‑starter pack", COST_ECOPACK, "ECO-" + System.currentTimeMillis() % 100000));

        updateBalanceLabel();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateBalanceLabel();
    }

    private void updateBalanceLabel() {
        int pts = prefs.getPoints();
        tvPointsBalance.setText("Balance: " + pts + " pts");
    }

    private void redeem(String rewardName, int cost, String code) {
        int current = prefs.getPoints();
        if (current < cost) {
            int needed = cost - current;
            showSnack("Not enough points. You need " + needed + " more.");
            return;
        }

        prefs.addPoints(-cost);

        String timestamp = dateFormat.format(new Date());
        String message = timestamp + " — " + rewardName + " redeemed. Code: " + code;
        tvLastRedeemed.setText(message);

        showSnack("Redeemed: " + rewardName + " · Code: " + code);
        updateBalanceLabel();
    }

    private void showSnack(String msg) {
        if (getView() != null) {
            Snackbar.make(getView(), msg, Snackbar.LENGTH_LONG).show();
        }
    }
}
