package com.android.sheguard;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;

import com.google.android.material.color.DynamicColors;

public class SheGuard extends Application {

    @SuppressLint("StaticFieldLeak")
    public static Context context;

    public static Context getAppContext() {
        return context;
    }

    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        DynamicColors.applyToActivitiesIfAvailable(this);
    }
}