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
import android.net.Uri;
import android.os.IBinder;
import android.telephony.SmsManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.android.sheguard.R;
import com.android.sheguard.api.NotificationAPI;
import com.android.sheguard.common.Constants;
import com.android.sheguard.config.Prefs;
import com.android.sheguard.model.ContactModel;
import com.android.sheguard.model.NotificationDataModel;
import com.android.sheguard.model.NotificationSenderModel;
import com.android.sheguard.ui.activity.HomeActivity;
import com.android.sheguard.util.NotificationClient;
import com.android.sheguard.util.NotificationResponse;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.gms.tasks.OnTokenCanceledListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@SuppressWarnings("FieldCanBeLocal")
public class SosService extends Service implements SensorEventListener {

    public String mLocation = "";
    public static boolean isRunning = false;
    private final SmsManager manager = SmsManager.getDefault();
    private FusedLocationProviderClient fusedLocationClient = null;
    static final MediaPlayer mediaPlayer = new MediaPlayer();
    public static final int MIN_TIME_BETWEEN_SHAKES = 1000;
    private final Float shakeThreshold = 10.2f;
    private SensorManager sensorManager = null;
    private AudioManager audioManager = null;
    private long lastShakeTime = 0;
    private boolean sentSMS = false;
    private boolean sentNotification = false;
    private boolean calledEmergency = false;
    private final String emergencyNumber = "999";
    private static NotificationAPI notificationApiService = null;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (fusedLocationClient == null) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        }

        updateLocation();

        if (notificationApiService == null) {
            notificationApiService = NotificationClient.getClient("https://fcm.googleapis.com/").create(NotificationAPI.class);
        }

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
                    resetValues();
                }
            } else {
                Intent notificationIntent = new Intent(this, HomeActivity.class);
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

                updateLocation();

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

        activateSosMode();
    }

    public void activateSosMode() {
        ArrayList<ContactModel> contacts = new ArrayList<>();
        Gson gson = new Gson();
        String jsonContacts = Prefs.getString(Constants.CONTACTS_LIST, "");

        if (!jsonContacts.isEmpty()) {
            Type type = new TypeToken<List<ContactModel>>() {
            }.getType();
            contacts.addAll(gson.fromJson(jsonContacts, type));

            if (Prefs.getBoolean(Constants.SETTINGS_SEND_SMS, true) && !sentSMS) {
                sendSMS(contacts);
                sentSMS = true;
            }

            if (Prefs.getBoolean(Constants.SETTINGS_SEND_NOTIFICATION, true) && !sentNotification) {
                sendNotification(contacts);
                sentNotification = true;
            }

            if (Prefs.getBoolean(Constants.SETTINGS_CALL_EMERGENCY_SERVICE, false) && !calledEmergency) {
                callEmergency();
                calledEmergency = true;
            }
        }

        if (Prefs.getBoolean(Constants.SETTINGS_PLAY_SIREN, false)) {
            playSiren();
        } else {
            stopSiren();
        }
    }

    public void updateLocation() {
        if (fusedLocationClient == null ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, new CancellationToken() {
                    @NonNull
                    @Override
                    public CancellationToken onCanceledRequested(@NonNull OnTokenCanceledListener listener) {
                        return new CancellationTokenSource().getToken();
                    }

                    @Override
                    public boolean isCancellationRequested() {
                        return false;
                    }
                })
                .addOnSuccessListener(location -> {
                    if (location == null) {
                        mLocation = "Unable to Find Location :(";
                    } else {
                        double altitude = location.getAltitude();
                        double longitude = location.getLongitude();
                        mLocation = "https://maps.google.com/maps?q=loc:" + altitude + "," + longitude;
                    }
                });

    }

    private void sendSMS(ContactModel contact) {
        manager.sendTextMessage(contact.getPhone(), null, getString(R.string.sos_message, contact.getName(), mLocation), null, null);
    }

    public void sendSMS(ArrayList<ContactModel> contacts) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        for (ContactModel contact : contacts) {
            sendSMS(contact);
        }
    }

    private void sendNotification(ArrayList<ContactModel> contacts) {
        for (ContactModel contact : contacts) {
            FirebaseFirestore.getInstance()
                    .collection(Constants.FIRESTORE_COLLECTION_PHONE2UID)
                    .document(contact.getPhone())
                    .get()
                    .addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            DocumentSnapshot document1 = task1.getResult();

                            if (document1.exists() && document1.getString("uid") != null) {

                                FirebaseFirestore.getInstance()
                                        .collection(Constants.FIRESTORE_COLLECTION_TOKENS)
                                        .document(Objects.requireNonNull(document1.getString("uid")))
                                        .get()
                                        .addOnCompleteListener(task2 -> {
                                            if (task2.isSuccessful()) {
                                                DocumentSnapshot document2 = task2.getResult();

                                                if (document2.exists() && document2.getString("token") != null) {
                                                    sendNotification(document2.getString("token"), Prefs.getString(Constants.PREFS_USER_NAME, getString(R.string.app_name)), getString(R.string.sos_notification, mLocation));
                                                }
                                            }
                                        });
                            }
                        }
                    });
        }
    }

    public static void sendNotification(String userToken, String title, String message) {
        NotificationDataModel data = new NotificationDataModel(title, message);
        NotificationSenderModel sender = new NotificationSenderModel(data, userToken);

        notificationApiService.sendNotification(sender).enqueue(new Callback<NotificationResponse>() {
            @Override
            public void onResponse(@NonNull Call<NotificationResponse> call, @NonNull Response<NotificationResponse> response) {
                // do nothing
            }

            @Override
            public void onFailure(@NonNull Call<NotificationResponse> call, @NonNull Throwable t) {
                // do nothing
            }
        });
    }

    public void callEmergency() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + emergencyNumber)));
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

    private void resetValues() {
        isRunning = false;
        sentSMS = false;
        sentNotification = false;
        calledEmergency = false;
    }
}