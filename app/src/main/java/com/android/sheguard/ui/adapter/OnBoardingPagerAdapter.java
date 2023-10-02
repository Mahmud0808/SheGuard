package com.android.sheguard.ui.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.sheguard.databinding.ViewOnboardingPageItemBinding;
import com.android.sheguard.ui.entity.OnBoardingPage;

public class OnBoardingPagerAdapter extends RecyclerView.Adapter<OnBoardingPagerAdapter.PagerViewHolder> {

    private final OnBoardingPage[] onBoardingPageList;

    public OnBoardingPagerAdapter() {
        this.onBoardingPageList = OnBoardingPage.values();
    }

    @NonNull
    @Override
    public PagerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ViewOnboardingPageItemBinding binding = ViewOnboardingPageItemBinding.inflate(inflater, parent, false);
        return new PagerViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PagerViewHolder holder, int position) {
        holder.bind(onBoardingPageList[position]);
    }

    @Override
    public int getItemCount() {
        return onBoardingPageList.length;
    }

    public static class PagerViewHolder extends RecyclerView.ViewHolder {

        private final ViewOnboardingPageItemBinding binding;
        private final TextView titleTv;
        private final TextView subTitleTv;
        private final TextView descTV;
        private final ImageView img;

        public PagerViewHolder(ViewOnboardingPageItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            titleTv = binding.titleTv;
            subTitleTv = binding.subTitleTv;
            descTV = binding.descTV;
            img = binding.img;
        }

        public void bind(OnBoardingPage onBoardingPage) {
            Context context = binding.getRoot().getContext();
            Resources res = context.getResources();

            titleTv.setText(res.getString(onBoardingPage.getTitleResource()));
            subTitleTv.setText(res.getString(onBoardingPage.getSubTitleResource()));
            descTV.setText(res.getString(onBoardingPage.getDescriptionResource()));
            img.setImageResource(onBoardingPage.getLogoResource());
        }
    }
}
