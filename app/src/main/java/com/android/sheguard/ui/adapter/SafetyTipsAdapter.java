package com.android.sheguard.ui.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.sheguard.R;

import java.util.ArrayList;

public class SafetyTipsAdapter extends ArrayAdapter<String> {

    ArrayList<String> safetyTips;
    Context context;

    public SafetyTipsAdapter(@NonNull Context context, ArrayList<String> safetyTips) {
        super(context, R.layout.view_safety_tips_list_item, safetyTips);
        this.safetyTips = safetyTips;
        this.context = context;
    }

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.view_safety_tips_list_item, null);
        }

        TextView text = convertView.findViewById(R.id.text);
        text.setText(safetyTips.get(position));

        convertView.setOnLongClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Contact", safetyTips.get(position));
            clipboard.setPrimaryClip(clip);
            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show();
            return false;
        });

        return convertView;
    }
}
