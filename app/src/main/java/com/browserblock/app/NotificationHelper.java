package com.browserblock.app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;

final class NotificationHelper {
    static final String BLOCK_CHANNEL = "active_block";
    static final String REMINDER_CHANNEL = "hourly_reminders";
    static final int BLOCK_NOTIFICATION_ID = 1;
    static final int REMINDER_NOTIFICATION_ID = 2;

    private NotificationHelper() {}

    static void createChannels(Context context) {
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        NotificationChannel block = new NotificationChannel(
            BLOCK_CHANNEL, "Active browser block", NotificationManager.IMPORTANCE_LOW);
        block.setDescription("Permanent countdown while a browser block is active");
        block.setShowBadge(false);
        block.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

        NotificationChannel reminder = new NotificationChannel(
            REMINDER_CHANNEL, "Hourly block prompt", NotificationManager.IMPORTANCE_HIGH);
        reminder.setDescription("Asks on the hour whether to block browsers");
        reminder.enableVibration(true);

        manager.createNotificationChannel(block);
        manager.createNotificationChannel(reminder);
    }

    static Notification activeBlock(Context context, long endTime) {
        long remaining = Math.max(1L, endTime - System.currentTimeMillis());
        Notification.Builder builder = new Notification.Builder(context, BLOCK_CHANNEL)
            .setSmallIcon(com.browserblock.app.R.drawable.notification_icon)
            .setColor(Color.RED)
            .setContentTitle("Browsers blocked")
            .setContentText("The block ends when the countdown reaches zero.")
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setShowWhen(true)
            .setWhen(endTime)
            .setUsesChronometer(true)
            .setTimeoutAfter(remaining)
            .setCategory(Notification.CATEGORY_SERVICE)
            .setVisibility(Notification.VISIBILITY_PUBLIC);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            builder.setChronometerCountDown(true);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            builder.setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_IMMEDIATE);
        }
        return builder.build();
    }

    static Notification reminder(Context context) {
        Intent blockIntent = new Intent(context, MainActivity.class)
            .setAction(MainActivity.ACTION_START_BLOCK)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent block = PendingIntent.getActivity(
            context, 10, blockIntent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent dismissIntent = new Intent(context, AlarmReceiver.class)
            .setAction(AlarmReceiver.ACTION_DISMISS);
        PendingIntent dismiss = PendingIntent.getBroadcast(
            context, 11, dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        return new Notification.Builder(context, REMINDER_CHANNEL)
            .setSmallIcon(com.browserblock.app.R.drawable.notification_icon)
            .setColor(Color.RED)
            .setContentTitle("Block browsers for one hour?")
            .setContentText("This prompt disappears in 60 seconds.")
            .setAutoCancel(true)
            .setTimeoutAfter(60_000L)
            .setCategory(Notification.CATEGORY_REMINDER)
            .setVisibility(Notification.VISIBILITY_PRIVATE)
            .addAction(new Notification.Action.Builder(null, "Block", block).build())
            .addAction(new Notification.Action.Builder(null, "Dismiss", dismiss).build())
            .build();
    }
}
