package com.android.sheguard.util;

import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;

import com.android.sheguard.SheGuard;
import com.android.sheguard.api.MyFirebaseAuth;
import com.android.sheguard.api.NotificationAPI;
import com.android.sheguard.common.Constants;
import com.android.sheguard.model.NotificationDataModel;
import com.android.sheguard.model.NotificationSenderModel;
import com.android.sheguard.model.NotificationTokenModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FirebaseUtil {

    static FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    static FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

    public static void updateToken() {
        if (firebaseUser != null) {
            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String token = task.getResult();
                    saveTokenInDatabase(token);
                }
            });
        }
    }

    private static void saveTokenInDatabase(String token) {
        NotificationTokenModel refreshToken = new NotificationTokenModel(token);
        CollectionReference Tokens = FirebaseFirestore.getInstance().collection(Constants.FIRESTORE_COLLECTION_TOKENS);
        Tokens.document(firebaseUser.getUid()).set(refreshToken);
    }

    @SuppressWarnings("deprecation")
    public static class SendNotificationTask extends AsyncTask<Void, Void, String> {

        private final String userToken;
        private final String title;
        private final String message;
        private final NotificationAPI notificationApiService;

        public SendNotificationTask(NotificationAPI notificationApiService, String userToken, String title, String message) {
            this.userToken = userToken;
            this.title = title;
            this.message = message;
            this.notificationApiService = notificationApiService;
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                return MyFirebaseAuth.getAccessToken(SheGuard.getAppContext());
            } catch (IOException e) {
                Log.e("SendNotificationTask", "Error getting access token: ", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(String accessToken) {
            if (accessToken != null) {
                String authHeader = "Bearer " + accessToken;

                NotificationDataModel data = new NotificationDataModel(title, message);
                NotificationSenderModel sender = new NotificationSenderModel(userToken, data);

                notificationApiService.sendNotification(authHeader, sender).enqueue(new Callback<NotificationResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<NotificationResponse> call, @NonNull Response<NotificationResponse> response) {
                        Log.i("SendNotificationTask", "sendNotification onResponse: " + response.message());
                    }

                    @Override
                    public void onFailure(@NonNull Call<NotificationResponse> call, @NonNull Throwable t) {
                        Log.e("SendNotificationTask", "sendNotification onFailure: " + t.getMessage(), t);
                    }
                });
            } else {
                Log.e("SendNotificationTask", "Failed to get access token.");
            }
        }
    }
}
