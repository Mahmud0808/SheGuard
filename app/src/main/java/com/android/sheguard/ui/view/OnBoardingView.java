package com.android.sheguard.ui.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import androidx.viewpager2.widget.ViewPager2;

import com.android.sheguard.databinding.ViewOnboardingPageBinding;
import com.android.sheguard.ui.activity.LoginRegisterActivity;
import com.android.sheguard.ui.adapter.OnBoardingPagerAdapter;
import com.android.sheguard.ui.core.Transform;
import com.android.sheguard.ui.entity.OnBoardingPage;

@SuppressWarnings("unused")
public class OnBoardingView extends FrameLayout {

    private static ViewOnboardingPageBinding binding;
    private int numberOfPages;

    public OnBoardingView(Context context) {
        this(context, null);
    }

    public OnBoardingView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OnBoardingView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public OnBoardingView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initialize(context, attrs, defStyleAttr, defStyleRes);
    }

    public static void navigateToPrevSlide() {
        int prevSlidePos = binding.slider.getCurrentItem() - 1;
        if (prevSlidePos < 0) {
            throw new IllegalArgumentException("Can't navigate to previous slide, because current slide is first");
        }
        binding.slider.setCurrentItem(prevSlidePos, true);
    }

    private void initialize(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        LayoutInflater inflater = LayoutInflater.from(context);
        binding = ViewOnboardingPageBinding.inflate(inflater, this, true);

        numberOfPages = OnBoardingPage.values().length;

        setUpSlider(binding);
        addingButtonsClickListeners(binding);
    }

    private void setUpSlider(ViewOnboardingPageBinding binding) {
        binding.slider.setAdapter(new OnBoardingPagerAdapter());

        binding.slider.setPageTransformer(Transform::setParallaxTransformation);

        binding.slider.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
                if (numberOfPages > 1) {
                    float newProgress = (position + positionOffset) / (numberOfPages - 1);
                    binding.onboardingRoot.setProgress(newProgress);
                }
            }
        });

        binding.pageIndicator.attachTo(binding.slider);
    }

    private void addingButtonsClickListeners(ViewOnboardingPageBinding binding) {
        binding.nextBtn.setOnClickListener(view -> navigateToNextSlide(binding.slider));
        binding.skipBtn.setOnClickListener(view -> navigateToLastSlide(binding.slider));
        binding.startBtn.setOnClickListener(view -> setFirstTimeLaunchToFalse());
    }

    private void setFirstTimeLaunchToFalse() {
        getContext().startActivity(new Intent(getContext(), LoginRegisterActivity.class));
        ((Activity) getContext()).finish();
    }

    private void navigateToNextSlide(ViewPager2 slider) {
        int nextSlidePos = slider.getCurrentItem() + 1;
        slider.setCurrentItem(nextSlidePos, true);
    }

    private void navigateToLastSlide(ViewPager2 slider) {
        slider.setCurrentItem(2, true);
    }
}