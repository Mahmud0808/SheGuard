package com.android.sheguard.ui.activity;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.android.sheguard.R;
import com.android.sheguard.common.Constants;
import com.android.sheguard.config.Prefs;
import com.android.sheguard.databinding.ActivityHomeBinding;
import com.android.sheguard.service.SosService;
import com.android.sheguard.util.AppUtil;
import com.android.sheguard.util.FirebaseUtil;
import com.android.sheguard.util.ObservableVariable;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

public class HomeActivity extends AppCompatActivity {

    ActivityHomeBinding binding;
    static ObservableVariable<Boolean> shakeDetection = new ObservableVariable<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.header.toolbar);
        binding.header.collapsingToolbar.setTitle(getString(R.string.activity_home_title));
        binding.header.collapsingToolbar.setSubtitle(getString(R.string.activity_home_desc, "How are you?"));
        setUserNameOnTitle();

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel1 = new NotificationChannel(getString(R.string.notification_channel_push), getString(R.string.notification_channel_push), NotificationManager.IMPORTANCE_DEFAULT);
        NotificationChannel channel2 = new NotificationChannel(getString(R.string.notification_channel_emergency), getString(R.string.notification_channel_emergency), NotificationManager.IMPORTANCE_DEFAULT);
        notificationManager.createNotificationChannel(channel1);
        notificationManager.createNotificationChannel(channel2);

        shakeDetection.setValue(Prefs.getBoolean(Constants.SETTINGS_SHAKE_DETECTION, false));
        shakeDetection.setOnChangeListener(newValue -> {
            binding.btnShakeDetection.setVisibility(newValue ? View.VISIBLE : View.GONE);
            updateButtonText();
            if (!newValue) {
                stopSosService();
            }
        });
        binding.btnShakeDetection.setVisibility(Prefs.getBoolean(Constants.SETTINGS_SHAKE_DETECTION, false) ? View.VISIBLE : View.GONE);
        updateButtonText();

        binding.btnShakeDetection.setOnClickListener(v -> {
            if (!SosService.isRunning) {
                if (AppUtil.permissionsGranted(this)) {
                    startSosService();
                    Snackbar.make(findViewById(android.R.id.content), "Service Started!", Snackbar.LENGTH_LONG).show();
                } else {
                    multiplePermissions.launch(AppUtil.REQUIRED_PERMISSIONS);
                }
            } else {
                stopSosService();
                Snackbar.make(findViewById(android.R.id.content), "Service Stopped!", Snackbar.LENGTH_LONG).show();
            }

            updateButtonText();
        });

        binding.profile.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, ProfileActivity.class)));
        binding.contacts.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, ContactsActivity.class)));
        binding.helpline.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, HelplineActivity.class)));
        binding.settings.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, SettingsActivity.class)));
        binding.safetyTips.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, SafetyTipsActivity.class)));
        binding.about.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, AboutActivity.class)));

        FirebaseUtil.updateToken();
    }

    private final ActivityResultLauncher<String[]> multiplePermissions = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
        @Override
        public void onActivityResult(Map<String, Boolean> result) {
            Iterator<Map.Entry<String, Boolean>> it = result.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, Boolean> pair = it.next();
                if (!pair.getValue()) {
                    Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Permission Must Be Granted!", Snackbar.LENGTH_INDEFINITE);
                    snackbar.setAction("Grant", v -> {
                        multiplePermissions.launch(new String[]{pair.getKey()});
                        snackbar.dismiss();
                    });
                    snackbar.show();
                }

                if (!it.hasNext() && AppUtil.permissionsGranted(HomeActivity.this)) {
                    binding.btnShakeDetection.performClick();
                }
            }
        }
    });

    public void setUserNameOnTitle() {
        final String[] userName = {"How are you?"};

        FirebaseFirestore.getInstance()
                .collection(Constants.FIRESTORE_COLLECTION_USERLIST)
                .document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            userName[0] = document.getString("name");
                            Prefs.putString(Constants.PREFS_USER_NAME, userName[0]);
                        }
                    }

                    binding.header.collapsingToolbar.setSubtitle(getString(R.string.activity_home_desc, userName[0]));
                });
    }

    private void startSosService() {
        try {
            Intent notificationIntent = new Intent(HomeActivity.this, SosService.class);
            notificationIntent.setAction("START");

            getApplicationContext().startForegroundService(notificationIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopSosService() {
        try {
            Intent notificationIntent = new Intent(HomeActivity.this, SosService.class);
            notificationIntent.setAction("STOP");
            SosService.stopSiren();

            getApplicationContext().startForegroundService(notificationIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateButtonText() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> binding.btnShakeDetection.setText(SosService.isRunning ? getString(R.string.btn_stop_service) : getString(R.string.btn_start_service)), 200);
    }
}