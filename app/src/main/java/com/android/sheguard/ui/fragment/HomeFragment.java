package com.android.sheguard.ui.fragment;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
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
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;

import com.android.sheguard.R;
import com.android.sheguard.common.Constants;
import com.android.sheguard.config.Prefs;
import com.android.sheguard.databinding.FragmentHomeBinding;
import com.android.sheguard.service.SosService;
import com.android.sheguard.ui.activity.LoginRegisterActivity;
import com.android.sheguard.ui.activity.MainActivity;
import com.android.sheguard.util.AppUtil;
import com.android.sheguard.util.FirebaseUtil;
import com.android.sheguard.util.SosUtil;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        ((AppCompatActivity) requireActivity()).setSupportActionBar(binding.header.toolbar);
        binding.header.collapsingToolbar.setTitle(getString(R.string.activity_home_title));
        binding.header.collapsingToolbar.setSubtitle(getString(R.string.activity_home_desc, getString(R.string.unknown_user)));
        setUserNameOnTitle();
        Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).setHomeAsUpIndicator(R.drawable.ic_nav_drawer);

        NotificationManager notificationManager = (NotificationManager) requireContext().getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel1 = new NotificationChannel(getString(R.string.notification_channel_push), getString(R.string.notification_channel_push), NotificationManager.IMPORTANCE_HIGH);
        NotificationChannel channel2 = new NotificationChannel(getString(R.string.notification_channel_emergency), getString(R.string.notification_channel_emergency), NotificationManager.IMPORTANCE_DEFAULT);
        notificationManager.createNotificationChannel(channel1);
        notificationManager.createNotificationChannel(channel2);

        binding.sosButton.setOnClickListener(v -> {
            if (AppUtil.permissionsGranted(getContext()) && SosUtil.isGPSEnabled(requireContext())) {
                SosUtil.activateInstantSosMode(requireContext());
            } else if (!AppUtil.permissionsGranted(getContext())) {
                multiplePermissions.launch(AppUtil.REQUIRED_PERMISSIONS);
            } else {
                SosUtil.turnOnGPS(requireContext());
            }
        });

        MainActivity.shakeDetection.setValue(Prefs.getBoolean(Constants.SETTINGS_SHAKE_DETECTION, false));
        MainActivity.shakeDetection.setOnChangeListener(newValue -> {
            binding.btnShakeDetection.setVisibility(newValue ? View.VISIBLE : View.GONE);
            updateButtonText();
            if (!newValue) {
                SosUtil.stopSosNotificationService(requireContext());
            }
        });
        binding.btnShakeDetection.setVisibility(Prefs.getBoolean(Constants.SETTINGS_SHAKE_DETECTION, false) ? View.VISIBLE : View.GONE);

        updateButtonText();

        binding.btnShakeDetection.setOnClickListener(v -> {
            if (!SosService.isRunning) {
                if (AppUtil.permissionsGranted(getContext()) && SosUtil.isGPSEnabled(requireContext())) {
                    SosUtil.startSosNotificationService(requireContext());
                    Snackbar.make(requireActivity().findViewById(android.R.id.content), getString(R.string.service_started), Snackbar.LENGTH_LONG).show();
                } else if (!AppUtil.permissionsGranted(getContext())) {
                    multiplePermissions.launch(AppUtil.REQUIRED_PERMISSIONS);
                } else {
                    SosUtil.turnOnGPS(requireContext());
                }
            } else {
                SosUtil.stopSosNotificationService(requireContext());
                Snackbar.make(requireActivity().findViewById(android.R.id.content), getString(R.string.service_stopped), Snackbar.LENGTH_LONG).show();
            }

            updateButtonText();
        });

        binding.contacts.setOnClickListener(v -> Navigation.findNavController(view).navigate(R.id.action_homeFragment_to_contactsFragment));
        binding.helpline.setOnClickListener(v -> Navigation.findNavController(view).navigate(R.id.action_homeFragment_to_helplineFragment));
        binding.safetyTips.setOnClickListener(v -> Navigation.findNavController(view).navigate(R.id.action_homeFragment_to_safetyTipsFragment));
        binding.about.setOnClickListener(v -> Navigation.findNavController(view).navigate(R.id.action_homeFragment_to_aboutFragment));

        FirebaseUtil.updateToken();

        initializeDrawerItems();

        if (!AppUtil.permissionsGranted(getContext())) {
            multiplePermissions.launch(AppUtil.REQUIRED_PERMISSIONS);
        }

        return view;
    }

    private void initializeDrawerItems() {
        ((NavigationView) requireActivity().findViewById(R.id.navView)).setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            NavOptions navOptions = new NavOptions.Builder()
                    .setEnterAnim(0)
                    .setExitAnim(0)
                    .setPopEnterAnim(R.anim.slide_out)
                    .setPopExitAnim(R.anim.fade_in)
                    .build();

            if (id == R.id.nav_profile) {
                Navigation.findNavController(binding.getRoot()).navigate(R.id.action_homeFragment_to_profileFragment, null, navOptions);
            } else if (id == R.id.nav_settings) {
                Navigation.findNavController(binding.getRoot()).navigate(R.id.action_homeFragment_to_settingsFragment, null, navOptions);
            } else if (id == R.id.nav_logout) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getContext(), LoginRegisterActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }

            ((MainActivity) requireActivity()).toggleDrawer();
            return true;
        });
    }

    public void setUserNameOnTitle() {
        final String[] userName = {getString(R.string.unknown_user)};

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

    private void updateButtonText() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (getContext() != null) {
                binding.btnShakeDetection.setText(SosService.isRunning ? getString(R.string.btn_stop_service) : getString(R.string.btn_start_service));
            }
        }, 200);
    }

    private final ActivityResultLauncher<String[]> multiplePermissions = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
        @Override
        public void onActivityResult(Map<String, Boolean> result) {
            Iterator<Map.Entry<String, Boolean>> it = result.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, Boolean> pair = it.next();
                if (!pair.getValue()) {
                    Snackbar snackbar = Snackbar.make(requireActivity().findViewById(android.R.id.content), R.string.permission_must_be_granted, Snackbar.LENGTH_INDEFINITE);
                    snackbar.setAction(R.string.grant, v -> {
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
}