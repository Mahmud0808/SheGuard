package com.android.sheguard.api;

import android.content.Context;

import com.android.sheguard.R;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

public class MyFirebaseAuth {

    private static final String SCOPES = "https://www.googleapis.com/auth/firebase.messaging";

    public static String getAccessToken(Context context) throws IOException {
        InputStream serviceAccount = context.getResources().openRawResource(R.raw.service_account);
        GoogleCredentials googleCredentials = GoogleCredentials
                .fromStream(serviceAccount)
                .createScoped(Collections.singleton(SCOPES));
        googleCredentials.refreshIfExpired();
        AccessToken token = googleCredentials.getAccessToken();
        return token.getTokenValue();
    }
}
