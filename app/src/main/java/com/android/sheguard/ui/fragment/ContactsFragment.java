package com.android.sheguard.ui.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.android.sheguard.R;
import com.android.sheguard.SheGuard;
import com.android.sheguard.common.Constants;
import com.android.sheguard.config.Prefs;
import com.android.sheguard.databinding.FragmentContactsBinding;
import com.android.sheguard.model.ContactModel;
import com.android.sheguard.ui.adapter.ContactsAdapter;
import com.android.sheguard.ui.adapter.NewContactAdapter;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

@SuppressLint({"StaticFieldLeak"})
@SuppressWarnings("FieldCanBeLocal")
public class ContactsFragment extends Fragment {

    public static ArrayList<ContactModel> contacts;
    public static View tvEmptyList;
    public static ContactsAdapter adapter;
    private FragmentContactsBinding binding;

    @SuppressLint("NotifyDataSetChanged")
    public static void removeContact(Context context, int idx) {
        View tvEmptyList = ((AppCompatActivity) context).findViewById(R.id.tv_empty_list);
        new MaterialAlertDialogBuilder(context, R.style.MaterialComponents_MaterialAlertDialog)
                .setMessage(context.getString(R.string.remove_contact_confirmation))
                .setCancelable(false)
                .setPositiveButton(context.getString(R.string.yes), (dialog, which) -> {
                    if (contacts.size() == 0 || idx >= contacts.size()) {
                        return;
                    }

                    contacts.remove(idx);
                    adapter.notifyDataSetChanged();

                    Gson gson = SheGuard.GSON;
                    String jsonContacts = gson.toJson(contacts);
                    Prefs.putString(Constants.CONTACTS_LIST, jsonContacts);

                    tvEmptyList.setVisibility(contacts.size() == 0 ? View.VISIBLE : View.GONE);
                    Snackbar.make(((AppCompatActivity) context).findViewById(R.id.fragmentContainerView), context.getString(R.string.contact_removed_successfully), Snackbar.LENGTH_SHORT).show();
                })
                .setNegativeButton(context.getString(R.string.no), (dialog, which) -> dialog.dismiss())
                .show();
    }

    public static boolean isPhoneNumberExists(String newNumber) {
        for (ContactModel contact : contacts) {
            if (contact.getPhone().equals(newNumber)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentContactsBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        ((AppCompatActivity) requireActivity()).setSupportActionBar(binding.header.toolbar);
        ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            binding.header.collapsingToolbar.setTitle(getString(R.string.activity_contacts_title));
            binding.header.collapsingToolbar.setSubtitle(getString(R.string.activity_contacts_desc));
        }

        tvEmptyList = view.findViewById(R.id.tv_empty_list);

        contacts = new ArrayList<>();
        Gson gson = SheGuard.GSON;
        String jsonContacts = Prefs.getString(Constants.CONTACTS_LIST, "");
        if (!jsonContacts.isEmpty()) {
            Type type = new TypeToken<List<ContactModel>>() {
            }.getType();
            contacts.addAll(gson.fromJson(jsonContacts, type));
        }

        new ContactsAdapter(requireContext(), contacts);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ContactsAdapter(requireContext(), contacts);
        ConcatAdapter concatAdapter = new ConcatAdapter(
                new NewContactAdapter(requireContext(), view),
                adapter
        );
        binding.recyclerView.setAdapter(concatAdapter);
        binding.recyclerView.setHasFixedSize(false);
        binding.recyclerView.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));

        binding.tvEmptyList.setVisibility(contacts.size() == 0 ? View.VISIBLE : View.GONE);

        return view;
    }
}