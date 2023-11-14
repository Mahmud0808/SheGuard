package com.android.sheguard.ui.fragment;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.android.sheguard.R;
import com.android.sheguard.common.Constants;
import com.android.sheguard.databinding.FragmentProfileBinding;
import com.android.sheguard.model.UserModel;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private LocationManager locationManager = null;
    private LocationRequest locationRequest = null;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        ((AppCompatActivity) requireActivity()).setSupportActionBar(binding.header.toolbar);
        ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            binding.header.collapsingToolbar.setTitle(getString(R.string.activity_profile_title));
            binding.header.collapsingToolbar.setSubtitle(getString(R.string.activity_profile_desc));
        }

        if (locationRequest == null) {
            locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
                    .setWaitForAccurateLocation(false)
                    .setMinUpdateIntervalMillis(2000)
                    .setMaxUpdateDelayMillis(5000)
                    .build();
        }

        getUserDetails();
        getCurrentLocation();

        binding.btnEditProfile.setOnClickListener(v -> Navigation.findNavController(view).navigate(R.id.action_profileFragment_to_editProfileFragment));

        return view;
    }

    private void getUserDetails() {
        FirebaseFirestore.getInstance()
                .collection(Constants.FIRESTORE_COLLECTION_USERLIST)
                .document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            UserModel user = new UserModel(
                                    document.getString("name"),
                                    document.getString("email"),
                                    document.getString("phone")
                            );

                            binding.tvName.setText(user.getName());
                            binding.tvEmail.setText(user.getEmail());
                            binding.tvPhone.setText(user.getPhone());
                        }
                    }
                });
    }

    private boolean isGPSEnabled() {
        if (locationManager == null) {
            locationManager = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);
        }

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private void getCurrentLocation() {
        if (!isGPSEnabled()) {
            binding.tvLocation.setText(R.string.gps_is_not_enabled);
            return;
        } else {
            binding.tvLocation.setText(R.string.getting_location);
        }

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        final int[] numberOfUpdates = {0};

        LocationServices.getFusedLocationProviderClient(requireContext())
                .requestLocationUpdates(locationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(@NonNull LocationResult locationResult) {
                        super.onLocationResult(locationResult);
                        if (getContext() == null) {
                            return;
                        }

                        numberOfUpdates[0]++;

                        if (numberOfUpdates[0] >= 3) {
                            LocationServices.getFusedLocationProviderClient(getContext())
                                    .removeLocationUpdates(this);

                            if (locationResult.getLocations().size() > 0) {
                                int idx = locationResult.getLocations().size() - 1;
                                double latitude = locationResult.getLocations().get(idx).getLatitude();
                                double longitude = locationResult.getLocations().get(idx).getLongitude();

                                Geocoder geocoder;
                                List<Address> addresses;
                                geocoder = new Geocoder(getContext(), Locale.getDefault());

                                try {
                                    addresses = geocoder.getFromLocation(latitude, longitude, 1);

                                    if (addresses != null) {
                                        StringBuilder address = new StringBuilder();

                                        for (int i = 0; i <= addresses.get(0).getMaxAddressLineIndex(); i++) {
                                            address.append(addresses.get(0).getAddressLine(i));

                                            if (i < addresses.get(0).getMaxAddressLineIndex()) {
                                                address.append("\n");
                                            }
                                        }

                                        binding.tvLocation.setText(address.toString());
                                    } else {
                                        binding.tvLocation.setText(getString(R.string.failed_to_get_location));
                                    }
                                } catch (IOException e) {
                                    binding.tvLocation.setText(getString(R.string.failed_to_get_location));
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }, Looper.getMainLooper());
    }
}