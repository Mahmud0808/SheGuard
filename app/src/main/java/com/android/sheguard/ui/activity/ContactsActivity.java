package com.android.sheguard.ui.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.android.sheguard.R;
import com.android.sheguard.common.Constants;
import com.android.sheguard.config.Prefs;
import com.android.sheguard.databinding.ActivityContactsBinding;
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

@SuppressLint("StaticFieldLeak")
public class ContactsActivity extends AppCompatActivity {

    static ActivityContactsBinding binding;
    private static ArrayList<ContactModel> contacts;
    private static ContactsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityContactsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.header.toolbar);
        ActionBar actionBar = getSupportActionBar();
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

        adapter = new ContactsAdapter(this, contacts);
        binding.listView.setAdapter(adapter);
        binding.listView.setNestedScrollingEnabled(true);

        binding.btnAddContact.setOnClickListener(v -> {
            String name = Objects.requireNonNull(binding.etAddName.getText()).toString().trim();
            String number = Objects.requireNonNull(binding.etAddNumber.getText()).toString().trim();

            if (number.length() < 10) {
                Snackbar.make(findViewById(android.R.id.content), "Please enter a valid phone number", Snackbar.LENGTH_LONG).show();
                return;
            } else if (isPhoneNumberExists(number)) {
                Snackbar.make(findViewById(android.R.id.content), "Contact already exists", Snackbar.LENGTH_LONG).show();
                return;
            } else if (name.isEmpty()) {
                Snackbar.make(findViewById(android.R.id.content), "Please enter a name", Snackbar.LENGTH_LONG).show();
                return;
            }

            addContact(name, number);
        });

        binding.listView.setOnItemLongClickListener((parent, view, position, id) -> {
            removeContact(this, position);
            return false;
        });

        binding.tvEmptyList.setVisibility(contacts.size() == 0 ? View.VISIBLE : View.GONE);
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
    }

    public static void removeContact(Context context, int idx) {
        new MaterialAlertDialogBuilder(context, R.style.MaterialComponents_MaterialAlertDialog)
                .setMessage("Are you sure you want to remove this contact?")
                .setCancelable(false)
                .setPositiveButton("Yes", (dialog, which) -> {
                    contacts.remove(idx);
                    adapter.notifyDataSetChanged();

                    Gson gson = new Gson();
                    String jsonContacts = gson.toJson(contacts);
                    Prefs.putString(Constants.CONTACTS_LIST, jsonContacts);

                    binding.tvEmptyList.setVisibility(contacts.size() == 0 ? View.VISIBLE : View.GONE);
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

    public boolean isPhoneNumberExists(String newNumber) {
        for (ContactModel contact : contacts) {
            if (contact.getPhone().equals(newNumber)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}