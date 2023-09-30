package com.android.sheguard.ui.entity;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import com.android.sheguard.R;

@SuppressWarnings("unused")
public enum OnBoardingPage {

    ONE(R.string.onboarding_slide1_title, R.string.onboarding_slide1_subtitle, R.string.onboarding_slide1_desc, R.drawable.ic_onboarding_img1),
    TWO(R.string.onboarding_slide2_title, R.string.onboarding_slide2_subtitle, R.string.onboarding_slide2_desc, R.drawable.ic_onboarding_img2),
    THREE(R.string.onboarding_slide3_title, R.string.onboarding_slide3_subtitle, R.string.onboarding_slide3_desc, R.drawable.ic_onboarding_img3);

    @StringRes
    private final int titleResource;

    @StringRes
    private final int subTitleResource;

    @StringRes
    private final int descriptionResource;

    @DrawableRes
    private final int logoResource;

    OnBoardingPage(int titleResource, int subTitleResource, int descriptionResource, int logoResource) {
        this.titleResource = titleResource;
        this.subTitleResource = subTitleResource;
        this.descriptionResource = descriptionResource;
        this.logoResource = logoResource;
    }

    public int getTitleResource() {
        return titleResource;
    }

    public int getSubTitleResource() {
        return subTitleResource;
    }

    public int getDescriptionResource() {
        return descriptionResource;
    }

    public int getLogoResource() {
        return logoResource;
    }
}
