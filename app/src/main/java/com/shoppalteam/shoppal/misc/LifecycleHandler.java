package com.shoppalteam.shoppal.misc;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;


public class LifecycleHandler implements Application.ActivityLifecycleCallbacks {
    private static int created;
    private static int destroyed;
    private static int resumed;
    private static int paused;

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        ++created;
    }

    @Override
    public void onActivityStarted(Activity activity) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        ++destroyed;
    }

    @Override
    public void onActivityResumed(Activity activity) {
        ++resumed;
    }

    @Override
    public void onActivityPaused(Activity activity) {
        ++paused;
    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

    }

    public static boolean isApplicationInForeground() {
        return resumed > paused;
    }

    public static boolean isApplicationAlive() {
        return created > destroyed;
    }
}
