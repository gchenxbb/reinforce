package com.reinforce.apk;

import android.app.Application;
import android.content.res.Configuration;
import android.util.Log;

public class ReinForceApp extends Application {

    String TAG = "ReinForceApp";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "ReinForceApp onCreate");

    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
    }
}
