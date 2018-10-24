package com.shoppalteam.shoppal.misc;

import android.app.Application;

public class BaseApplication extends Application {
    LifecycleHandler lifecycleHandler;

    @Override
    public void onCreate() {
        super.onCreate();
        lifecycleHandler = new LifecycleHandler();
        registerActivityLifecycleCallbacks(lifecycleHandler);
    }
}