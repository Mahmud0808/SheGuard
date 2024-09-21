package com.android.sheguard.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Patterns;

import androidx.annotation.NonNull;

import com.android.sheguard.R;
import com.android.sheguard.ui.activity.MainActivity;
import com.android.sheguard.util.FirebaseUtil;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.regex.Matcher;

public class FireBaseMessageService extends FirebaseMessagingService {

    String title, body;

    @Override
    public void onNewToken(@NonNull String str) {
        super.onNewToken(str);
        FirebaseUtil.updateToken();
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        if (remoteMessage.getData().isEmpty()) {
            return;
        }

        title = remoteMessage.getData().get("title");
        body = remoteMessage.getData().get("body");

        String url = "";

        if (body != null) {
            Matcher webMatcher = Patterns.WEB_URL.matcher(body);
            if (webMatcher.find()) {
                url = webMatcher.group();
            }
        }

        Intent notificationIntent;

        if (!url.isEmpty()) {
            notificationIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        } else {
            notificationIntent = new Intent(this, MainActivity.class);
        }

        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 2, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        NotificationChannel channel = new NotificationChannel(getString(R.string.notification_channel_push), getString(R.string.notification_channel_push_desc), NotificationManager.IMPORTANCE_HIGH);
        channel.setDescription(getString(R.string.notification_channel_emergency_desc));
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(channel);

        Notification notification = new Notification.Builder(this, getString(R.string.notification_channel_emergency))
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(R.drawable.ic_launcher_notification)
                .setContentIntent(pendingIntent)
                .build();

        try {
            this.startForeground(1, notification);
        } catch (Exception ignored) {
        }
        notificationManager.notify(1, notification);
    }
}
