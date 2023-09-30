package com.android.sheguard.ui.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.sheguard.R;
import com.android.sheguard.model.ContactModel;
import com.android.sheguard.ui.activity.ContactsActivity;

import java.util.ArrayList;

public class ContactsAdapter extends ArrayAdapter<ContactModel> {

    ArrayList<ContactModel> contacts;
    Context context;

    public ContactsAdapter(@NonNull Context context, ArrayList<ContactModel> contacts) {
        super(context, R.layout.view_contacts_list_item, contacts);
        this.contacts = contacts;
        this.context = context;
    }

    @SuppressLint({"SetTextI18n", "InflateParams"})
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.view_contacts_list_item, null);
        }

        TextView initials = convertView.findViewById(R.id.initials);
        initials.setText(contacts.get(position).getName().substring(0, 1).toUpperCase());

        TextView contact = convertView.findViewById(R.id.name);
        contact.setText(contacts.get(position).getName());

        TextView number = convertView.findViewById(R.id.number);
        number.setText(contacts.get(position).getPhone());

        ImageView copy = convertView.findViewById(R.id.copy);
        copy.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Activity.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Contact", contacts.get(position).getName() + ": " + contacts.get(position).getPhone());
            clipboard.setPrimaryClip(clip);
            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show();
        });

        ImageView delete = convertView.findViewById(R.id.delete);
        delete.setOnClickListener(v -> ContactsActivity.removeContact(context, position));

        convertView.setOnClickListener(v -> context.startActivity(new Intent(Intent.ACTION_DIAL).setData(Uri.parse("tel:" + contacts.get(position).getPhone()))));

        return convertView;
    }
}
