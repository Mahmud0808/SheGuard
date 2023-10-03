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
import com.android.sheguard.databinding.FragmentHelplineBinding;
import com.android.sheguard.model.HelplineModel;
import com.android.sheguard.ui.adapter.HelplineAdapter;

import java.util.ArrayList;

public class HelplineFragment extends Fragment {

    private final ArrayList<HelplineModel> helplines = new ArrayList<>();
    private FragmentHelplineBinding binding;
    private HelplineAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHelplineBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        ((AppCompatActivity) requireActivity()).setSupportActionBar(binding.header.toolbar);
        ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            binding.header.collapsingToolbar.setTitle(getString(R.string.activity_helpline_title));
            binding.header.collapsingToolbar.setSubtitle(getString(R.string.activity_helpline_desc));
        }

        helplines.add(new HelplineModel(
                "Government helpline number for Violence against women / prevention of child marriage",
                "Multi sectoral referral and psychosocial support; This is the main helpline number to call and has been/being circulated nationwide.",
                "109"
        ));
        helplines.add(new HelplineModel(
                "National Emergency Hotline Number",
                "Immediate services to police and hospitals.",
                "999"
        ));
        helplines.add(new HelplineModel(
                "National Hotline Number",
                "Immediate reports/help for any social problems from enquiring after COVID 19 to child marriage and sexual harassment cases.",
                "333"
        ));
        helplines.add(new HelplineModel(
                "National helpline center for violence against women",
                "Immediate service to victims and links up to relevant agencies: doctors, counselors, lawyers, DNA experts, police officers.",
                "10921"
        ));
        helplines.add(new HelplineModel(
                "Kaan Pete Roi",
                "Mental Health & Psychosocial helpline.",
                "01779554391", "01779554392", "01688709965", "01688709966", "1985275286", "1852035634", "1517969150"
        ));
        helplines.add(new HelplineModel(
                "Moner Bondhu",
                "Mental Health & Psychosocial helpline.",
                "1776632344"
        ));
        helplines.add(new HelplineModel(
                "Sajida Foundation",
                "Mental Health & Psychosocial helpline. (9AM - 5PM)",
                "9678771511", "01777771515 (9AM - 5PM)"
        ));
        helplines.add(new HelplineModel(
                "Dosh Unisher Mor Helpdesk for GBV/SRHR/psychosocial support.",
                "Mental Health & Psychosocial helpline.",
                "9612600600"
        ));
        helplines.add(new HelplineModel(
                "Ain o Salish Kendra (ASK)",
                "Legal assistance, emergency shelter and mental healthcare.",
                "01724415677 (9AM - 5PM)"
        ));
        helplines.add(new HelplineModel(
                "Bandhu Social Welfare Society (In collaboration with Ministry of Social Welfare)",
                "Psychosocial support as well as guideline for SRHR and legal aspects.",
                "01714048418 (SRHR)", "01771 444666 (legal)"
        ));
        helplines.add(new HelplineModel(
                "Friendship Bangladesh",
                "Advices on primary healthcare, SGBV and psychosocial support.",
                "01880081111 (24/7 Helpline) based in Cox's Bazar"
        ));
        helplines.add(new HelplineModel(
                "Action Against Hunger (ACF BD)",
                "Psychosocial support and case management.",
                "01888066747 (For Chittagong Region, including Cox’s Bazar district)", "01869859757 (For Dhaka and rest of the districts)"
        ));

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new HelplineAdapter(requireContext(), helplines);
        binding.recyclerView.setAdapter(adapter);
        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));

        return view;
    }
}