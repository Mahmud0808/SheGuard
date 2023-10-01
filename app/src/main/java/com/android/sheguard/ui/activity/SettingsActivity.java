package com.android.sheguard.ui.activity;

import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.android.sheguard.R;
import com.android.sheguard.common.Constants;
import com.android.sheguard.config.Prefs;
import com.android.sheguard.databinding.ActivitySettingsBinding;
import com.android.sheguard.service.SosService;

public class SettingsActivity extends AppCompatActivity {

    ActivitySettingsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.header.toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            binding.header.collapsingToolbar.setTitle(getString(R.string.activity_settings_title));
            binding.header.collapsingToolbar.setSubtitle(getString(R.string.activity_settings_desc));
        }

        binding.switchShakeDetection.setChecked(Prefs.getBoolean(Constants.SETTINGS_SHAKE_DETECTION, false));
        binding.switchShakeDetection.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Prefs.putBoolean(Constants.SETTINGS_SHAKE_DETECTION, isChecked);
            HomeActivity.shakeDetection.setValue(isChecked);
        });
        binding.shakeDetectionContainer.setOnClickListener(v -> binding.switchShakeDetection.toggle());

        binding.switchSendSms.setChecked(Prefs.getBoolean(Constants.SETTINGS_SEND_SMS, true));
        binding.switchSendSms.setOnCheckedChangeListener((buttonView, isChecked) -> Prefs.putBoolean(Constants.SETTINGS_SEND_SMS, isChecked));
        binding.sendSmsContainer.setOnClickListener(v -> binding.switchSendSms.toggle());

        binding.switchSendNotification.setChecked(Prefs.getBoolean(Constants.SETTINGS_SEND_NOTIFICATION, true));
        binding.switchSendNotification.setOnCheckedChangeListener((buttonView, isChecked) -> Prefs.putBoolean(Constants.SETTINGS_SEND_NOTIFICATION, isChecked));
        binding.sendNotificationContainer.setOnClickListener(v -> binding.switchSendNotification.toggle());

        binding.switchPlaySiren.setChecked(Prefs.getBoolean(Constants.SETTINGS_PLAY_SIREN, false));
        binding.switchPlaySiren.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Prefs.putBoolean(Constants.SETTINGS_PLAY_SIREN, isChecked);
            if (!isChecked) {
                SosService.stopSiren();
            }
        });
        binding.playSirenContainer.setOnClickListener(v -> binding.switchPlaySiren.toggle());

        binding.switchCallEmergencyService.setChecked(Prefs.getBoolean(Constants.SETTINGS_CALL_EMERGENCY_SERVICE, false));
        binding.switchCallEmergencyService.setOnCheckedChangeListener((buttonView, isChecked) -> Prefs.putBoolean(Constants.SETTINGS_CALL_EMERGENCY_SERVICE, isChecked));
        binding.callEmergencyServiceContainer.setOnClickListener(v -> binding.switchCallEmergencyService.toggle());
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}