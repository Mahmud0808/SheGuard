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

@SuppressWarnings("FieldCanBeLocal")
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
                getString(R.string.helpline_name1),
                getString(R.string.helpline_desc1),
                getString(R.string.helpline_num1)
        ));
        helplines.add(new HelplineModel(
                getString(R.string.helpline_name2),
                getString(R.string.helpline_desc2),
                getString(R.string.helpline_num2)
        ));
        helplines.add(new HelplineModel(
                getString(R.string.helpline_name3),
                getString(R.string.helpline_desc3),
                getString(R.string.helpline_num3)
        ));
        helplines.add(new HelplineModel(
                getString(R.string.helpline_name4),
                getString(R.string.helpline_desc4),
                getString(R.string.helpline_num4)
        ));
        helplines.add(new HelplineModel(
                getString(R.string.helpline_name5),
                getString(R.string.helpline_desc5),
                getString(R.string.helpline_num5_1),
                getString(R.string.helpline_num5_2),
                getString(R.string.helpline_num5_3),
                getString(R.string.helpline_num5_4),
                getString(R.string.helpline_num5_5),
                getString(R.string.helpline_num5_6),
                getString(R.string.helpline_num5_7)
        ));
        helplines.add(new HelplineModel(
                getString(R.string.helpline_name6),
                getString(R.string.helpline_desc6),
                getString(R.string.helpline_num6)
        ));
        helplines.add(new HelplineModel(
                getString(R.string.helpline_name7),
                getString(R.string.helpline_desc7),
                getString(R.string.helpline_num7_1),
                getString(R.string.helpline_num7_2)
        ));
        helplines.add(new HelplineModel(
                getString(R.string.helpline_name8),
                getString(R.string.helpline_desc8),
                getString(R.string.helpline_num8)
        ));
        helplines.add(new HelplineModel(
                getString(R.string.helpline_name9),
                getString(R.string.helpline_desc9),
                getString(R.string.helpline_num9)
        ));
        helplines.add(new HelplineModel(
                getString(R.string.helpline_name10),
                getString(R.string.helpline_desc10),
                getString(R.string.helpline_num10_1),
                getString(R.string.helpline_num10_2)
        ));
        helplines.add(new HelplineModel(
                getString(R.string.helpline_name11),
                getString(R.string.helpline_desc11),
                getString(R.string.helpline_num11)
        ));
        helplines.add(new HelplineModel(
                getString(R.string.helpline_name12),
                getString(R.string.helpline_desc12),
                getString(R.string.helpline_num12_1),
                getString(R.string.helpline_num12_2)
        ));

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new HelplineAdapter(requireContext(), helplines);
        binding.recyclerView.setAdapter(adapter);
        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));

        return view;
    }
}