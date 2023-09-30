package com.android.sheguard.ui.activity;

import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.android.sheguard.R;
import com.android.sheguard.databinding.ActivitySafetyTipsBinding;
import com.android.sheguard.ui.adapter.SafetyTipsAdapter;

import java.util.ArrayList;

public class SafetyTipsActivity extends AppCompatActivity {

    ActivitySafetyTipsBinding binding;
    private final ArrayList<String> safetyTips = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySafetyTipsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.header.toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            binding.header.collapsingToolbar.setTitle(getString(R.string.activity_safety_tips_title));
            binding.header.collapsingToolbar.setSubtitle(getString(R.string.activity_safety_tips_desc));
        }

        safetyTips.add(getString(R.string.safety_tips_tip_1));
        safetyTips.add(getString(R.string.safety_tips_tip_2));
        safetyTips.add(getString(R.string.safety_tips_tip_3));
        safetyTips.add(getString(R.string.safety_tips_tip_4));
        safetyTips.add(getString(R.string.safety_tips_tip_5));
        safetyTips.add(getString(R.string.safety_tips_tip_6));
        safetyTips.add(getString(R.string.safety_tips_tip_7));
        safetyTips.add(getString(R.string.safety_tips_tip_8));
        safetyTips.add(getString(R.string.safety_tips_tip_9));
        safetyTips.add(getString(R.string.safety_tips_tip_10));
        safetyTips.add(getString(R.string.safety_tips_tip_11));
        safetyTips.add(getString(R.string.safety_tips_tip_12));
        safetyTips.add(getString(R.string.safety_tips_tip_13));
        safetyTips.add(getString(R.string.safety_tips_tip_14));
        safetyTips.add(getString(R.string.safety_tips_tip_15));
        safetyTips.add(getString(R.string.safety_tips_tip_16));
        safetyTips.add(getString(R.string.safety_tips_tip_17));
        safetyTips.add(getString(R.string.safety_tips_tip_18));
        safetyTips.add(getString(R.string.safety_tips_tip_19));
        safetyTips.add(getString(R.string.safety_tips_tip_20));
        safetyTips.add(getString(R.string.safety_tips_tip_21));

        SafetyTipsAdapter adapter = new SafetyTipsAdapter(this, safetyTips);
        binding.listView.setAdapter(adapter);
        binding.listView.setNestedScrollingEnabled(true);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}