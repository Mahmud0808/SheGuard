package com.android.sheguard.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.android.sheguard.R;
import com.android.sheguard.common.Constants;
import com.android.sheguard.config.Prefs;
import com.android.sheguard.databinding.FragmentContactsBinding;
import com.android.sheguard.model.ContactModel;
import com.android.sheguard.ui.adapter.ContactsAdapter;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ContactsFragment extends Fragment {

    private static ArrayList<ContactModel> contacts;
    private static ContactsAdapter adapter;
    private FragmentContactsBinding binding;

    public static void removeContact(Context context, int idx) {
        View tvEmptyList = ((AppCompatActivity) context).findViewById(R.id.tv_empty_list);
        new MaterialAlertDialogBuilder(context, R.style.MaterialComponents_MaterialAlertDialog)
                .setMessage("Are you sure you want to remove this contact?")
                .setCancelable(false)
                .setPositiveButton("Yes", (dialog, which) -> {
                    if (contacts.size() == 0) {
                        return;
                    }

                    contacts.remove(idx);
                    adapter.notifyDataSetChanged();

                    Gson gson = new Gson();
                    String jsonContacts = gson.toJson(contacts);
                    Prefs.putString(Constants.CONTACTS_LIST, jsonContacts);

                    tvEmptyList.setVisibility(contacts.size() == 0 ? View.VISIBLE : View.GONE);
                    Snackbar.make(((AppCompatActivity) context).findViewById(R.id.fragmentContainerView), "Contact removed successfully", Snackbar.LENGTH_SHORT).show();
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
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

        contacts = new ArrayList<>();
        Gson gson = new Gson();
        String jsonContacts = Prefs.getString(Constants.CONTACTS_LIST, "");
        if (!jsonContacts.isEmpty()) {
            Type type = new TypeToken<List<ContactModel>>() {
            }.getType();
            contacts.addAll(gson.fromJson(jsonContacts, type));
        }

        adapter = new ContactsAdapter(requireContext(), contacts);
        binding.listView.setAdapter(adapter);
        binding.listView.setNestedScrollingEnabled(true);

        binding.btnAddContact.setOnClickListener(v -> {
            String name = Objects.requireNonNull(binding.etAddName.getText()).toString().trim();
            String number = Objects.requireNonNull(binding.etAddNumber.getText()).toString().trim();

            if (name.isEmpty()) {
                Snackbar.make(view, "Please enter a name", Snackbar.LENGTH_LONG).show();
                return;
            } else if (number.isEmpty()) {
                Snackbar.make(view, "Please enter a phone number", Snackbar.LENGTH_LONG).show();
                return;
            } else if (number.length() < 10) {
                Snackbar.make(view, "Please enter a valid phone number", Snackbar.LENGTH_LONG).show();
                return;
            } else if (isPhoneNumberExists(number)) {
                Snackbar.make(view, "Contact already exists", Snackbar.LENGTH_LONG).show();
                return;
            }

            addContact(name, number);
        });

        binding.listView.setOnItemLongClickListener((parent, view1, position, id) -> {
            removeContact(requireContext(), position);
            return false;
        });

        binding.tvEmptyList.setVisibility(contacts.size() == 0 ? View.VISIBLE : View.GONE);

        return view;
    }

    private void addContact(String name, String number) {
        binding.etAddName.setText("");
        binding.etAddNumber.setText("");
        contacts.add(new ContactModel(name, number));
        adapter.notifyDataSetChanged();

        Gson gson = new Gson();
        String jsonContacts = gson.toJson(contacts);
        Prefs.putString(Constants.CONTACTS_LIST, jsonContacts);

        binding.tvEmptyList.setVisibility(contacts.size() == 0 ? View.VISIBLE : View.GONE);
        Snackbar.make(requireView(), "Contact added successfully", Snackbar.LENGTH_SHORT).show();
    }

    public boolean isPhoneNumberExists(String newNumber) {
        for (ContactModel contact : contacts) {
            if (contact.getPhone().equals(newNumber)) {
                return true;
            }
        }
        return false;
    }
}