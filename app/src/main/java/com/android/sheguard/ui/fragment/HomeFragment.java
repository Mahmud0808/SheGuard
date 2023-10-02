package com.android.sheguard.ui.fragment;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.android.sheguard.R;
import com.android.sheguard.common.Constants;
import com.android.sheguard.config.Prefs;
import com.android.sheguard.databinding.FragmentHomeBinding;
import com.android.sheguard.service.SosService;
import com.android.sheguard.ui.activity.MainActivity;
import com.android.sheguard.util.AppUtil;
import com.android.sheguard.util.FirebaseUtil;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private LocationManager locationManager = null;
    private LocationRequest locationRequest = null;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        ((AppCompatActivity) requireActivity()).setSupportActionBar(binding.header.toolbar);
        binding.header.collapsingToolbar.setTitle(getString(R.string.activity_home_title));
        binding.header.collapsingToolbar.setSubtitle(getString(R.string.activity_home_desc, "How are you?"));
        setUserNameOnTitle();

        if (locationRequest == null) {
            locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
                    .setWaitForAccurateLocation(false)
                    .setMinUpdateIntervalMillis(2000)
                    .setMaxUpdateDelayMillis(5000)
                    .build();
        }

        NotificationManager notificationManager = (NotificationManager) requireContext().getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel1 = new NotificationChannel(getString(R.string.notification_channel_push), getString(R.string.notification_channel_push), NotificationManager.IMPORTANCE_HIGH);
        NotificationChannel channel2 = new NotificationChannel(getString(R.string.notification_channel_emergency), getString(R.string.notification_channel_emergency), NotificationManager.IMPORTANCE_DEFAULT);
        notificationManager.createNotificationChannel(channel1);
        notificationManager.createNotificationChannel(channel2);

        binding.sosButton.setOnClickListener(v -> {
            if (!isGPSEnabled()) {
                turnOnGPS();
                return;
            }

            // do other stuff
        });

        MainActivity.shakeDetection.setValue(Prefs.getBoolean(Constants.SETTINGS_SHAKE_DETECTION, false));
        MainActivity.shakeDetection.setOnChangeListener(newValue -> {
            binding.btnShakeDetection.setVisibility(newValue ? View.VISIBLE : View.GONE);
            updateButtonText();
            if (!newValue) {
                stopSosNotificationService();
            }
        });
        binding.btnShakeDetection.setVisibility(Prefs.getBoolean(Constants.SETTINGS_SHAKE_DETECTION, false) ? View.VISIBLE : View.GONE);

        updateButtonText();

        binding.btnShakeDetection.setOnClickListener(v -> {
            if (!SosService.isRunning) {
                if (AppUtil.permissionsGranted(getContext()) && isGPSEnabled()) {
                    startSosNotificationService();
                    Snackbar.make(requireActivity().findViewById(android.R.id.content), "Service Started!", Snackbar.LENGTH_LONG).show();
                } else if (!AppUtil.permissionsGranted(getContext())) {
                    multiplePermissions.launch(AppUtil.REQUIRED_PERMISSIONS);
                } else {
                    turnOnGPS();
                }
            } else {
                stopSosNotificationService();
                Snackbar.make(requireActivity().findViewById(android.R.id.content), "Service Stopped!", Snackbar.LENGTH_LONG).show();
            }

            updateButtonText();
        });

        binding.profile.setOnClickListener(v -> Navigation.findNavController(view).navigate(R.id.action_homeFragment_to_profileFragment));
        binding.contacts.setOnClickListener(v -> Navigation.findNavController(view).navigate(R.id.action_homeFragment_to_contactsFragment));
        binding.helpline.setOnClickListener(v -> Navigation.findNavController(view).navigate(R.id.action_homeFragment_to_helplineFragment));
        binding.safetyTips.setOnClickListener(v -> Navigation.findNavController(view).navigate(R.id.action_homeFragment_to_safetyTipsFragment));
        binding.settings.setOnClickListener(v -> Navigation.findNavController(view).navigate(R.id.action_homeFragment_to_settingsFragment));
        binding.about.setOnClickListener(v -> Navigation.findNavController(view).navigate(R.id.action_homeFragment_to_aboutFragment));

        FirebaseUtil.updateToken();

        return view;
    }

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

                    if (getContext() != null) {
                        binding.header.collapsingToolbar.setSubtitle(getString(R.string.activity_home_desc, userName[0]));
                    }
                });
    }

    private final ActivityResultLauncher<String[]> multiplePermissions = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
        @Override
        public void onActivityResult(Map<String, Boolean> result) {
            Iterator<Map.Entry<String, Boolean>> it = result.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, Boolean> pair = it.next();
                if (!pair.getValue()) {
                    Snackbar snackbar = Snackbar.make(requireActivity().findViewById(android.R.id.content), "Permission Must Be Granted!", Snackbar.LENGTH_INDEFINITE);
                    snackbar.setAction("Grant", v -> {
                        multiplePermissions.launch(new String[]{pair.getKey()});
                        snackbar.dismiss();
                    });
                    snackbar.show();
                }

                if (!it.hasNext() && AppUtil.permissionsGranted(getActivity())) {
                    binding.btnShakeDetection.performClick();
                }
            }
        }
    });

    private boolean isGPSEnabled() {
        if (locationManager == null) {
            locationManager = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);
        }

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private void turnOnGPS() {
        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(requireContext())
                .checkLocationSettings(new LocationSettingsRequest.Builder()
                        .addLocationRequest(locationRequest)
                        .setAlwaysShow(true)
                        .build()
                );

        result.addOnCompleteListener(task -> {
            try {
                task.getResult(ApiException.class);
            } catch (ApiException apiException) {
                switch (apiException.getStatusCode()) {
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            ResolvableApiException resolvableApiException = (ResolvableApiException) apiException;
                            resolvableApiException.startResolutionForResult(requireActivity(), 2);
                        } catch (IntentSender.SendIntentException sendIntentException) {
                            sendIntentException.printStackTrace();
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // device doesn't have location settings
                        break;
                }
            }
        });
    }

    private void startSosNotificationService() {
        if (!SosService.isRunning) {
            Intent notificationIntent = new Intent(getActivity(), SosService.class);
            notificationIntent.setAction("START");

            requireContext().startForegroundService(notificationIntent);
        }
    }

    private void stopSosNotificationService() {
        if (SosService.isRunning) {
            Intent notificationIntent = new Intent(getActivity(), SosService.class);
            notificationIntent.setAction("STOP");

            requireContext().startForegroundService(notificationIntent);
        }
    }

    private void updateButtonText() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (getContext() != null) {
                binding.btnShakeDetection.setText(SosService.isRunning ? getString(R.string.btn_stop_service) : getString(R.string.btn_start_service));
            }
        }, 200);
    }
}