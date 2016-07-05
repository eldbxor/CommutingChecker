package com.example.taek.commutingchecker.utils;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.example.taek.commutingchecker.R;
import com.example.taek.commutingchecker.services.BLEScanService;
import com.example.taek.commutingchecker.ui.MainActivity;

/**
 * Created by Taek on 2016-05-17.
 */
public class GenerateNotification {
    public static void generateNotification(Context context, String ticker, String contentTitle, String contentText){
        android.support.v4.app.NotificationCompat.Builder mBuilder = new android.support.v4.app.NotificationCompat.Builder(context)
                .setTicker(ticker)
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setSmallIcon(R.drawable.commuting)
                .setWhen(System.currentTimeMillis());

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        String[] events = contentText.split(",");
        inboxStyle.setBigContentTitle("details:");
        for(int j = 0; j < events.length; j++){
            inboxStyle.addLine(events[j]);
        }
        mBuilder.setStyle(inboxStyle);

        Intent notifyIntent = new Intent(context, MainActivity.class);

        notifyIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent notifyPendingIntent =
                PendingIntent.getActivity(
                        context,
                        0,
                        notifyIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(BLEScanService.NOTIFICATION_ID, mBuilder.build());
    }
}
