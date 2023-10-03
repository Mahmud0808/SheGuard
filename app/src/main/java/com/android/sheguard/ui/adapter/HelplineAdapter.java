package com.android.sheguard.ui.adapter;

import android.content.Context;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.sheguard.R;
import com.android.sheguard.model.HelplineModel;

import java.util.ArrayList;

public class HelplineAdapter extends RecyclerView.Adapter<HelplineAdapter.ViewHolder> {

    private final Context context;
    private final ArrayList<HelplineModel> helplines;

    public HelplineAdapter(@NonNull Context context, ArrayList<HelplineModel> helplines) {
        this.helplines = helplines;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.view_helpline_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.name.setText(helplines.get(position).getName());

        holder.number.setText(helplines.get(position).getNumber());
        Linkify.addLinks(holder.number, Patterns.PHONE, "tel:", Linkify.sPhoneNumberMatchFilter, Linkify.sPhoneNumberTransformFilter);
        holder.number.setMovementMethod(LinkMovementMethod.getInstance());

        holder.details.setText(helplines.get(position).getDetails());
    }

    @Override
    public int getItemCount() {
        return helplines.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView name, number, details;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.name);
            number = itemView.findViewById(R.id.number);
            details = itemView.findViewById(R.id.details);
        }
    }
}
