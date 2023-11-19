package com.android.sheguard.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.sheguard.R;
import com.android.sheguard.SheGuard;
import com.android.sheguard.common.Constants;
import com.android.sheguard.config.Prefs;
import com.android.sheguard.model.ContactModel;
import com.android.sheguard.ui.fragment.ContactsFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;

import java.util.Objects;

public class NewContactAdapter extends RecyclerView.Adapter<NewContactAdapter.ViewHolder> {

    Context context;
    View rootView;

    public NewContactAdapter(Context context, View rootView) {
        this.context = context;
        this.rootView = rootView;
    }

    @NonNull
    @Override
    public NewContactAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.view_add_new_contact, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.btnAddContact.setOnClickListener(v -> {
            String name = Objects.requireNonNull(holder.etAddName.getText()).toString().trim();
            String number = Objects.requireNonNull(holder.etAddNumber.getText()).toString().trim();

            if (name.isEmpty()) {
                Snackbar.make(rootView, "Please enter a name", Snackbar.LENGTH_LONG).show();
                return;
            } else if (number.isEmpty()) {
                Snackbar.make(rootView, "Please enter a phone number", Snackbar.LENGTH_LONG).show();
                return;
            } else if (number.length() < 10) {
                Snackbar.make(rootView, "Please enter a valid phone number", Snackbar.LENGTH_LONG).show();
                return;
            } else if (ContactsFragment.isPhoneNumberExists(number)) {
                Snackbar.make(rootView, "Contact already exists", Snackbar.LENGTH_LONG).show();
                return;
            }

            if (ContactsFragment.contacts.size() >= 10) {
                Snackbar.make(rootView, "You can add maximum 10 contacts", Snackbar.LENGTH_SHORT).show();
                return;
            }

            holder.etAddName.setText("");
            holder.etAddNumber.setText("");

            ContactsFragment.contacts.add(new ContactModel(name, number));
            ContactsFragment.adapter.notifyDataSetChanged();

            Gson gson = SheGuard.GSON;
            String jsonContacts = gson.toJson(ContactsFragment.contacts);
            Prefs.putString(Constants.CONTACTS_LIST, jsonContacts);

            if (ContactsFragment.tvEmptyList != null) {
                ContactsFragment.tvEmptyList.setVisibility(ContactsFragment.contacts.size() == 0 ? View.VISIBLE : View.GONE);
            }

            Snackbar.make(rootView, "Contact added successfully", Snackbar.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return 1;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextInputEditText etAddName, etAddNumber;
        MaterialButton btnAddContact;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            etAddName = itemView.findViewById(R.id.et_add_name);
            etAddNumber = itemView.findViewById(R.id.et_add_number);
            btnAddContact = itemView.findViewById(R.id.btn_add_contact);
        }
    }
}
