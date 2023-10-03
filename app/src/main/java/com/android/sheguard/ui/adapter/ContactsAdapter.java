package com.android.sheguard.ui.adapter;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.sheguard.R;
import com.android.sheguard.model.ContactModel;
import com.android.sheguard.ui.fragment.ContactsFragment;

import java.util.ArrayList;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ViewHolder> {

    Context context;
    ArrayList<ContactModel> contacts;

    public ContactsAdapter(@NonNull Context context, ArrayList<ContactModel> contacts) {
        this.contacts = contacts;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.view_contacts_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.initials.setText(contacts.get(position).getName().substring(0, 1).toUpperCase());
        holder.contact.setText(contacts.get(position).getName());
        holder.number.setText(contacts.get(position).getPhone());

        holder.copy.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Activity.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Contact", contacts.get(position).getName() + ": " + contacts.get(position).getPhone());
            clipboard.setPrimaryClip(clip);
            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show();
        });

        holder.delete.setOnClickListener(v -> ContactsFragment.removeContact(context, position));
        holder.itemView.setOnClickListener(v -> context.startActivity(new Intent(Intent.ACTION_DIAL).setData(Uri.parse("tel:" + contacts.get(position).getPhone()))));
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView initials, contact, number;
        ImageView copy, delete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            initials = itemView.findViewById(R.id.initials);
            contact = itemView.findViewById(R.id.name);
            number = itemView.findViewById(R.id.number);
            copy = itemView.findViewById(R.id.copy);
            delete = itemView.findViewById(R.id.delete);
        }
    }
}
