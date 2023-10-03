package com.android.sheguard.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.android.sheguard.R;
import com.android.sheguard.databinding.FragmentSafetyTipsBinding;
import com.android.sheguard.ui.adapter.SafetyTipsAdapter;

import java.util.ArrayList;

@SuppressWarnings("FieldCanBeLocal")
public class SafetyTipsFragment extends Fragment {

    private final ArrayList<String> safetyTips = new ArrayList<>();
    private FragmentSafetyTipsBinding binding;
    private SafetyTipsAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSafetyTipsBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        ((AppCompatActivity) requireActivity()).setSupportActionBar(binding.header.toolbar);
        ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
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

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new SafetyTipsAdapter(requireContext(), safetyTips);
        binding.recyclerView.setAdapter(adapter);
        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));

        return view;
    }
}