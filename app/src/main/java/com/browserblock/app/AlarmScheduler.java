package com.browserblock.app;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.util.Calendar;

final class AlarmScheduler {
    private AlarmScheduler() {}

    static void scheduleBlockEnd(Context context, long endTime) {
        Intent intent = new Intent(context, AlarmReceiver.class)
            .setAction(AlarmReceiver.ACTION_BLOCK_END);
        PendingIntent pending = PendingIntent.getBroadcast(
            context, 21, intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager manager = context.getSystemService(AlarmManager.class);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || manager.canScheduleExactAlarms()) {
            manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, endTime, pending);
        } else {
            manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, endTime, pending);
        }
    }

    static void scheduleNextHour(Context context) {
        Calendar next = Calendar.getInstance();
        next.set(Calendar.MINUTE, 0);
        next.set(Calendar.SECOND, 0);
        next.set(Calendar.MILLISECOND, 0);
        next.add(Calendar.HOUR_OF_DAY, 1);

        Intent intent = new Intent(context, AlarmReceiver.class)
            .setAction(AlarmReceiver.ACTION_REMINDER);
        PendingIntent pending = PendingIntent.getBroadcast(
            context, 20, intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager manager = context.getSystemService(AlarmManager.class);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || manager.canScheduleExactAlarms()) {
            manager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP, next.getTimeInMillis(), pending);
        } else {
            manager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP, next.getTimeInMillis(), pending);
        }
    }
}
