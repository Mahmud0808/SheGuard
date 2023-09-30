package com.android.sheguard.ui.activity;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.android.sheguard.R;
import com.android.sheguard.common.Constants;
import com.android.sheguard.databinding.ActivityMainBinding;
import com.android.sheguard.service.SosService;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.header.toolbar);
        binding.header.collapsingToolbar.setTitle(getString(R.string.activity_home_title));
        binding.header.collapsingToolbar.setSubtitle(getString(R.string.activity_home_desc, "Nice to meet you"));
        setUserNameOnTitle();

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel1 = new NotificationChannel(getString(R.string.notification_channel_push), getString(R.string.notification_channel_push), NotificationManager.IMPORTANCE_DEFAULT);
        NotificationChannel channel2 = new NotificationChannel(getString(R.string.notification_channel_emergency), getString(R.string.notification_channel_emergency), NotificationManager.IMPORTANCE_DEFAULT);
        notificationManager.createNotificationChannel(channel1);
        notificationManager.createNotificationChannel(channel2);

        String[] permissions;

        if (Build.VERSION.SDK_INT >= 33)
            permissions = new String[]{
                    Manifest.permission.SEND_SMS,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.POST_NOTIFICATIONS
            };
        else {
            permissions = new String[]{
                    Manifest.permission.SEND_SMS,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            };
        }

        binding.startService.setOnClickListener(v -> {
            if (permissionsGranted()) {
                Intent notificationIntent = new Intent(MainActivity.this, SosService.class);
                notificationIntent.setAction("START");

                getApplicationContext().startForegroundService(notificationIntent);
                Snackbar.make(findViewById(android.R.id.content), "Service Started!", Snackbar.LENGTH_LONG).show();
            } else {
                multiplePermissions.launch(permissions);
            }
        });

        binding.stopService.setOnClickListener(v -> {
            Intent notificationIntent = new Intent(MainActivity.this, SosService.class);
            notificationIntent.setAction("STOP");

            getApplicationContext().startForegroundService(notificationIntent);
            Snackbar.make(findViewById(android.R.id.content), "Service Stopped!", Snackbar.LENGTH_LONG).show();
        });

        binding.profile.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ProfileActivity.class)));
        binding.contacts.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ContactsActivity.class)));
        binding.helpline.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, HelplineActivity.class)));
        binding.settings.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, SettingsActivity.class)));
        binding.safetyTips.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, SafetyTipsActivity.class)));
    }

    private final ActivityResultLauncher<String[]> multiplePermissions = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
        @Override
        public void onActivityResult(Map<String, Boolean> result) {
            Iterator<Map.Entry<String, Boolean>> it = result.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, Boolean> pair = it.next();
                if (!pair.getValue()) {
                    Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Permission Must Be Granted!", Snackbar.LENGTH_INDEFINITE);
                    snackbar.setAction("Grant Permission", v -> {
                        multiplePermissions.launch(new String[]{pair.getKey()});
                        snackbar.dismiss();
                    });
                    snackbar.show();
                }

                if (!it.hasNext() && permissionsGranted()) {
                    binding.startService.performClick();
                }
            }
        }
    });

    private boolean permissionsGranted() {
        boolean granted = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        if (Build.VERSION.SDK_INT >= 33) {
            granted = granted && ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
        }

        return granted;
    }

    public void setUserNameOnTitle() {
        final String[] userName = {"Nice to meet you"};

        FirebaseFirestore.getInstance()
                .collection(Constants.FIRESTORE_COLLECTION_USERLIST)
                .document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            userName[0] = document.getString("name");
                        }
                    }

                    binding.header.collapsingToolbar.setSubtitle(getString(R.string.activity_home_desc, userName[0]));
                });
    }

}