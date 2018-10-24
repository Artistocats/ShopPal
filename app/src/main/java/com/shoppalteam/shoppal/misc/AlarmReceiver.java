package com.shoppalteam.shoppal.misc;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import com.shoppalteam.shoppal.R;
import com.shoppalteam.shoppal.activities.MainActivity;

import org.joda.time.DateTime;

public class AlarmReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "Notifications";
    private static final String CHANNEL_NAME = "Notifications";

    @Override
    public void onReceive(Context context, Intent intent) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        if(sharedPreferences.getBoolean("notifications_enable", false)) {

            DateTime depletionDateTime=Utils.stringToDateTime(sharedPreferences.getString("depletionDateTime", null));

            if (depletionDateTime!=null) {
                DateTime now = Utils.stringToDateTime(Utils.getNow());

                if (depletionDateTime.isBefore(now)) {
                    boolean sounds = sharedPreferences.getBoolean("notifications_sounds", false);

                    Intent notificationIntent = new Intent(context, MainActivity.class);

                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                    stackBuilder.addParentStack(MainActivity.class);
                    stackBuilder.addNextIntent(notificationIntent);

                    PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

                    String title = context.getString(R.string.notification_title);
                    String text = context.getString(R.string.notification_text);
                    String ticker = context.getString(R.string.notification_ticker);

                    NotificationCompat.Builder builder =
                            new NotificationCompat.Builder(context, CHANNEL_ID)
                                    .setContentTitle(title)
                                    .setContentText(text)
                                    .setTicker(ticker)
                                    .setAutoCancel(true)
                                    .setSmallIcon(R.drawable.notification)
                                    .setContentIntent(pendingIntent);

                    NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                    if (sounds) {
                        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                        builder.setSound(soundUri);
                    }

                    if (notificationManager.getNotificationChannel(CHANNEL_ID) == null)
                        notificationManager.createNotificationChannel(new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH));

                    Notification notification = builder.build();
                    notification.flags |= Notification.FLAG_ONLY_ALERT_ONCE;
                    notificationManager.notify(0, notification);
                }
            }
        }
    }
}