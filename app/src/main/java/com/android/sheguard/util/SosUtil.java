package com.android.sheguard.util;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Looper;
import android.telephony.SmsManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.android.sheguard.R;
import com.android.sheguard.SheGuard;
import com.android.sheguard.api.NotificationAPI;
import com.android.sheguard.common.Constants;
import com.android.sheguard.config.Prefs;
import com.android.sheguard.model.ContactModel;
import com.android.sheguard.service.SosService;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SosUtil {

    private static String mLocation = "";
    private static boolean sentSMS = false;
    private static boolean sentNotification = false;
    private static boolean calledEmergency = false;
    private static AudioManager audioManager = null;
    private static LocationRequest locationRequest = null;
    private static LocationManager locationManager = null;
    private static NotificationAPI notificationApiService = null;
    private static final MediaPlayer mediaPlayer = new MediaPlayer();
    private static final SmsManager smsManager = SmsManager.getDefault();

    static {
        if (locationRequest == null) {
            locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
                    .setWaitForAccurateLocation(false)
                    .setMinUpdateIntervalMillis(2000)
                    .setMaxUpdateDelayMillis(5000)
                    .build();
        }

        if (notificationApiService == null) {
            notificationApiService = NotificationClient.getClient("https://fcm.googleapis.com/").create(NotificationAPI.class);
        }
    }

    public static boolean isGPSEnabled(Context context) {
        if (locationManager == null) {
            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        }

        Log.i("SOS", "isGPSEnabled: " + locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER));
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public static void turnOnGPS(Context context) {
        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(context)
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
                            resolvableApiException.startResolutionForResult((AppCompatActivity) context, 2);
                        } catch (IntentSender.SendIntentException sendIntentException) {
                            Log.i("SOS", "turnOnGPS: " + sendIntentException.getMessage());
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // device doesn't have location settings
                        break;
                }
            }
        });
    }

    public static void startSosNotificationService(Context context) {
        if (!SosService.isRunning) {
            Intent notificationIntent = new Intent(context, SosService.class);
            notificationIntent.setAction("START");

            context.startForegroundService(notificationIntent);
        }
    }

    public static void stopSosNotificationService(Context context) {
        if (SosService.isRunning) {
            Intent notificationIntent = new Intent(context, SosService.class);
            notificationIntent.setAction("STOP");

            context.startForegroundService(notificationIntent);
        }
    }

    public static void activateInstantSosMode(Context context) {
        if (mediaPlayer.isPlaying()) {
            stopSiren();
            resetValues();
            Log.i("SOS", "Stopping Siren");
            Log.i("SOS", "Resetting Values");
            return;
        }

        resetValues();

        ArrayList<ContactModel> contacts = new ArrayList<>();
        Gson gson = SheGuard.GSON;
        String jsonContacts = Prefs.getString(Constants.CONTACTS_LIST, "");

        if (Prefs.getBoolean(Constants.SETTINGS_CALL_EMERGENCY_SERVICE, false) && !calledEmergency) {
            callEmergency(context);
            calledEmergency = true;
        }

        if (!jsonContacts.isEmpty()) {
            Type type = new TypeToken<List<ContactModel>>() {
            }.getType();
            contacts.addAll(gson.fromJson(jsonContacts, type));

            sendLocation(context, contacts);
        }

        if (Prefs.getBoolean(Constants.SETTINGS_PLAY_SIREN, false) && !mediaPlayer.isPlaying()) {
            playSiren(context);
            Log.i("SOS", "Playing Siren");
        } else {
            stopSiren();
            Log.i("SOS", "Stopping Siren");
        }
    }

    private static void sendLocation(Context context, ArrayList<ContactModel> contacts) {
        if (isGPSEnabled(context) || !mLocation.isEmpty()) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            final int[] numberOfUpdates = {0};

            LocationServices.getFusedLocationProviderClient(context)
                    .requestLocationUpdates(locationRequest, new LocationCallback() {
                        @Override
                        public void onLocationResult(@NonNull LocationResult locationResult) {
                            super.onLocationResult(locationResult);
                            numberOfUpdates[0]++;

                            if (numberOfUpdates[0] >= 3) {
                                LocationServices.getFusedLocationProviderClient(context)
                                        .removeLocationUpdates(this);

                                if (!locationResult.getLocations().isEmpty()) {
                                    int idx = locationResult.getLocations().size() - 1;
                                    double latitude = locationResult.getLocations().get(idx).getLatitude();
                                    double longitude = locationResult.getLocations().get(idx).getLongitude();

                                    mLocation = "https://maps.google.com/maps?q=loc:" + latitude + "," + longitude;
                                    Log.i("SOS", "sendLocation: received location");

                                    if (Prefs.getBoolean(Constants.SETTINGS_SEND_SMS, true) && !sentSMS) {
                                        sendSMS(context, contacts);
                                        sentSMS = true;
                                    }

                                    if (Prefs.getBoolean(Constants.SETTINGS_SEND_NOTIFICATION, true) && !sentNotification) {
                                        sendNotification(context, contacts);
                                        sentNotification = true;
                                    }
                                }
                            }
                        }
                    }, Looper.getMainLooper());
        }
    }

    private static void sendSMS(Context context, ContactModel contact) {
        smsManager.sendTextMessage(contact.getPhone(), null, context.getString(R.string.sos_message, contact.getName(), mLocation), null, null);
        Log.i("SOS", "sendSMS: sent");
    }

    private static void sendSMS(Context context, ArrayList<ContactModel> contacts) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        for (ContactModel contact : contacts) {
            sendSMS(context, contact);
        }
    }

    public static void sendNotification(Context context, ArrayList<ContactModel> contacts) {
        for (ContactModel contact : contacts) {
            FirebaseFirestore.getInstance()
                    .collection(Constants.FIRESTORE_COLLECTION_PHONE2UID)
                    .document(contact.getPhone())
                    .get()
                    .addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            DocumentSnapshot document1 = task1.getResult();

                            if (document1.exists() && document1.getString("uid") != null) {
                                Log.i("SOS", "sendNotification: uid found");

                                FirebaseFirestore.getInstance()
                                        .collection(Constants.FIRESTORE_COLLECTION_TOKENS)
                                        .document(Objects.requireNonNull(document1.getString("uid")))
                                        .get()
                                        .addOnCompleteListener(task2 -> {
                                            if (task2.isSuccessful()) {
                                                DocumentSnapshot document2 = task2.getResult();

                                                if (document2.exists() && document2.getString("token") != null) {
                                                    Log.i("SOS", "sendNotification: token found");
                                                    sendNotification(document2.getString("token"), Prefs.getString(Constants.PREFS_USER_NAME, context.getString(R.string.app_name)), context.getString(R.string.sos_notification, mLocation));
                                                }
                                            }
                                        });
                            }
                        }
                    });
        }
    }

    @SuppressWarnings("deprecation")
    public static void sendNotification(String userToken, String title, String message) {
        new FirebaseUtil.SendNotificationTask(notificationApiService, userToken, title, message).execute();
    }

    private static void callEmergency(Context context) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        context.startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + Constants.EMERGENCY_NUMBER)));
        Log.i("SOS", "Calling Emergency");
    }

    private static void playSiren(Context context) {
        if (mediaPlayer.isPlaying()) {
            return;
        }

        if (audioManager == null) {
            audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        }

        try {
            AssetFileDescriptor afd = context.getAssets().openFd("police-operation-siren.mp3");
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
            mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            mediaPlayer.prepare();
            mediaPlayer.setVolume(1f, 1f);
            mediaPlayer.setLooping(true);
            mediaPlayer.start();
        } catch (IOException e) {
            Log.e("SOS", "playSiren error: " + e.getMessage(), e);
        }
    }

    public static void stopSiren() {
        try {
            mediaPlayer.stop();
            mediaPlayer.reset();
        } catch (Exception ignored) {
        }
    }

    private static void resetValues() {
        sentSMS = false;
        sentNotification = false;
        calledEmergency = false;
    }
}
