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
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
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

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("FieldCanBeLocal")
public class SosService extends Service implements SensorEventListener {

    String myLocation = "";
    private boolean isRunning = false;
    private final SmsManager manager = SmsManager.getDefault();
    private FusedLocationProviderClient fusedLocationClient;
    static final MediaPlayer mediaPlayer = new MediaPlayer();
    public static final int MIN_TIME_BETWEEN_SHAKES = 1000;
    private final Float shakeThreshold = 10.2f;
    private SensorManager sensorManager = null;
    private AudioManager audioManager = null;
    private long lastShakeTime = 0;

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
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        if (sensorManager != null) {
            Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener((SensorEventListener) this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction() != null) {
            if (intent.getAction().equalsIgnoreCase("STOP")) {
                if (isRunning) {
                    this.stopForeground(true);
                    this.stopSelf();
                    stopSiren();
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
                        .setSmallIcon(R.drawable.ic_launcher_notification)
                        .setContentIntent(pendingIntent)
                        .setOngoing(true)
                        .build();

                this.startForeground(1, notification);
                notificationManager.notify(1, notification);
                isRunning = true;
                return START_NOT_STICKY;
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            long curTime = System.currentTimeMillis();
            if ((curTime - lastShakeTime) > MIN_TIME_BETWEEN_SHAKES) {

                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];

                double acceleration = Math.sqrt(Math.pow(x, 2) +
                        Math.pow(y, 2) +
                        Math.pow(z, 2)) - SensorManager.GRAVITY_EARTH;

                if (acceleration > shakeThreshold) {
                    lastShakeTime = curTime;
                    deviceShaken();
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // do nothing
    }

    private void deviceShaken() {
        if (!Prefs.getBoolean(Constants.SETTINGS_SHAKE_DETECTION, false)) {
            stopSiren();
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
        if (mediaPlayer.isPlaying()) {
            return;
        }

        try {
            AssetFileDescriptor afd = getAssets().openFd("police-operation-siren.mp3");
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
            mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
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
}