package com.android.sheguard.util;

import com.android.sheguard.common.Constants;
import com.android.sheguard.model.NotificationTokenModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

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
}
