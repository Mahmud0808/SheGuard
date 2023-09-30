package com.android.sheguard.ui.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.sheguard.R;
import com.android.sheguard.model.HelplineModel;

import java.util.ArrayList;

public class HelplineAdapter extends ArrayAdapter<HelplineModel> {

    ArrayList<HelplineModel> helplines;
    Context context;

    public HelplineAdapter(@NonNull Context context, ArrayList<HelplineModel> helplines) {
        super(context, R.layout.view_helpline_list_item, helplines);
        this.helplines = helplines;
        this.context = context;
    }

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.view_helpline_list_item, null);
        }

        TextView name = convertView.findViewById(R.id.name);
        name.setText(helplines.get(position).getName());

        TextView number = convertView.findViewById(R.id.number);
        number.setText(helplines.get(position).getNumber());
        Linkify.addLinks(number, Patterns.PHONE, "tel:", Linkify.sPhoneNumberMatchFilter, Linkify.sPhoneNumberTransformFilter);
        number.setMovementMethod(LinkMovementMethod.getInstance());

        TextView details = convertView.findViewById(R.id.details);
        details.setText(helplines.get(position).getDetails());

        return convertView;
    }
}
