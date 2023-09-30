package com.android.sheguard.service;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.android.sheguard.R;
import com.android.sheguard.common.Constants;
import com.android.sheguard.config.Prefs;
import com.android.sheguard.ui.activity.MainActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.seismic.ShakeDetector;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class SosService extends Service implements ShakeDetector.Listener {

    String myLocation = "";
    private boolean isRunning = false;
    private SensorManager sensorManager = null;
    private final ShakeDetector shakeDetector = new ShakeDetector(this);
    private final SmsManager manager = SmsManager.getDefault();
    private FusedLocationProviderClient fusedLocationClient;
    static final MediaPlayer mediaPlayer = new MediaPlayer();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        updateLocation();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        double altitude = location.getAltitude();
                        double longitude = location.getLongitude();
                        myLocation = "https://maps.google.com/maps?q=loc:" + altitude + "," + longitude;
                    } else if (myLocation.isEmpty()) {
                        myLocation = "Unable to Find Location :(";
                    }
                });

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction() != null) {
            if (intent.getAction().equalsIgnoreCase("STOP")) {
                if (isRunning) {
                    this.stopForeground(true);
                    this.stopSelf();
                    stopSiren();
                    shakeDetector.stop();
                    isRunning = false;
                }
            } else {
                Intent notificationIntent = new Intent(this, MainActivity.class);
                notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

                NotificationChannel channel = new NotificationChannel(getString(R.string.notification_channel_emergency), getString(R.string.notification_channel_emergency), NotificationManager.IMPORTANCE_DEFAULT);
                channel.setDescription(getString(R.string.notification_channel_emergency_desc));

                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.createNotificationChannel(channel);

                Notification notification = new Notification.Builder(this, getString(R.string.notification_channel_emergency))
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(getString(R.string.notification_emergency_mode, getString(R.string.app_name)))
                        .setSmallIcon(R.mipmap.ic_launcher_round)
                        .setContentIntent(pendingIntent)
                        .setOngoing(true)
                        .build();

                this.startForeground(1, notification);
                notificationManager.notify(1, notification);
                shakeDetector.start(sensorManager);
                isRunning = true;
                return START_NOT_STICKY;
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    public void updateLocation() {
        try {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        } catch (Exception ignored) {
        }
    }

    public void sendSMS(ArrayList<String> contacts) {
        for (String contact : contacts) {
            manager.sendTextMessage(contact, null, "I'm in Trouble!\nMy Location:\n" + myLocation, null, null);
        }
    }

    public void playSiren() {
        try {
            AssetFileDescriptor afd = getAssets().openFd("police-operation-siren.mp3");
            mediaPlayer.setDataSource(afd.getFileDescriptor());
            mediaPlayer.prepare();
            mediaPlayer.setVolume(1f, 1f);
            mediaPlayer.setLooping(true);
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void stopSiren() {
        try {
            mediaPlayer.stop();
            mediaPlayer.reset();
        } catch (Exception ignored) {
        }
    }

    @Override
    public void hearShake() {
        if (Prefs.getBoolean(Constants.SETTINGS_SHAKE_DETECTION, false)) {
            return;
        }

        ArrayList<String> contacts = new ArrayList<>();
        Gson gson = new Gson();
        String jsonContacts = Prefs.getString(Constants.CONTACTS_LIST, "");

        if (!jsonContacts.isEmpty()) {
            Type type = new TypeToken<List<String>>() {
            }.getType();
            contacts.addAll(gson.fromJson(jsonContacts, type));

            if (Prefs.getBoolean(Constants.SETTINGS_SEND_SMS, true)) {
                sendSMS(contacts);
            }
        }

        if (Prefs.getBoolean(Constants.SETTINGS_PLAY_SIREN, false)) {
            playSiren();
        } else {
            stopSiren();
        }

        Log.i("ShakeDetector", "Shake Detected");
    }
}