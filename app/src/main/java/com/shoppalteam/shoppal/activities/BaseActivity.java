package com.shoppalteam.shoppal.activities;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.shoppalteam.shoppal.R;
import com.shoppalteam.shoppal.misc.LifecycleHandler;
import com.shoppalteam.shoppal.misc.Utils;

import org.joda.time.DateTime;

import java.util.Calendar;

public abstract class BaseActivity extends AppCompatActivity {
    private final String NOTIFICATIONS_TAG = "NOTIFICATIONS";

    private static boolean alarmSet=false;

    private static AlarmManager alarmManager;
    private static Intent notificationIntent;
    private static PendingIntent broadcast;

    SharedPreferences sharedPreferences;

    private static final String mainActivityName = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Notifications (only one initialization)
        if(!alarmSet) {
            alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            notificationIntent = new Intent("android.media.action.DISPLAY_NOTIFICATION");
            notificationIntent.addCategory("android.intent.category.DEFAULT");
            broadcast = PendingIntent.getBroadcast(this, 100, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            alarmSet=true;
        }

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (LifecycleHandler.isApplicationInForeground()) {
            //Cancel pending notifications
            alarmManager.cancel(broadcast);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());

            //Clear notifications from bar
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancelAll();

            Log.d(NOTIFICATIONS_TAG, "Notifications Deactivated & Cleared (" + calendar.getTime() + ")");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!LifecycleHandler.isApplicationInForeground())
            setAlarm();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(!LifecycleHandler.isApplicationAlive())
            setAlarm();
    }

    public void createToolbar() {
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        if(!this.getClass().getSimpleName().equals(mainActivityName))
            try {getSupportActionBar().setDisplayHomeAsUpEnabled(true);}catch (Exception e){}
    }

    private void setAlarm() {
        if(sharedPreferences.getBoolean("notifications_enable", false)) {
            DateTime depletionDateTime= Utils.stringToDateTime(sharedPreferences.getString("depletionDateTime", null));

            if(depletionDateTime!=null) {
                broadcast = PendingIntent.getBroadcast(this, 100, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());

                int intervalMillis=Integer.parseInt(sharedPreferences.getString("notification_frequency","3600000"));
                calendar.add(Calendar.MILLISECOND,intervalMillis);
                alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                        intervalMillis, broadcast);

//                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
//                        10000, broadcast);    //DEBUGGING - Notification spamming

                Log.d(NOTIFICATIONS_TAG, "Notifications Activated (" + calendar.getTime() +")");
            }
        }
    }
}
