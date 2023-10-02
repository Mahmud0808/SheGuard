package com.android.sheguard.ui.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.android.sheguard.databinding.ActivityOnboardingBinding;
import com.android.sheguard.ui.view.OnBoardingView;
import com.google.firebase.auth.FirebaseAuth;

public class OnBoardingActivity extends AppCompatActivity {

    ActivityOnboardingBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOnboardingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null && firebaseAuth.getCurrentUser().isEmailVerified()) {
            startActivity(new Intent(OnBoardingActivity.this, MainActivity.class));
            finishAffinity();
        }
    }

    @Override
    public void onBackPressed() {
        try {
            OnBoardingView.navigateToPrevSlide();
        } catch (Exception ignored) {
            OnBoardingActivity.this.finish();
            System.exit(0);
            super.onBackPressed();
        }
    }
}