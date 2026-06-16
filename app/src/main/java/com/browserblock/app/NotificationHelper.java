package com.browserblock.app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.util.SparseArray;

final class NotificationHelper {
    static final String BLOCK_CHANNEL = "active_block";
    static final String REMINDER_CHANNEL = "hourly_reminders";
    static final int BLOCK_NOTIFICATION_ID = 1;
    static final int REMINDER_NOTIFICATION_ID = 2;
    private static final SparseArray<Icon> COUNTDOWN_ICONS = new SparseArray<>();

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
        int remainingMinutes = roundedMinutesRemaining(remaining);
        Notification.Builder builder = new Notification.Builder(context, BLOCK_CHANNEL)
            .setSmallIcon(countdownIcon(remainingMinutes))
            .setColor(Color.RED)
            .setContentTitle("Browsers blocked")
            .setContentText(formatRemaining(remaining))
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setTimeoutAfter(remaining)
            .setCategory(Notification.CATEGORY_SERVICE)
            .setVisibility(Notification.VISIBILITY_PUBLIC);
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

    private static int roundedMinutesRemaining(long remainingMillis) {
        long minutes = (remainingMillis + 59_999L) / 60_000L;
        return (int)Math.max(1L, Math.min(99L, minutes));
    }

    private static String formatRemaining(long remainingMillis) {
        long totalSeconds = Math.max(0L, (remainingMillis + 999L) / 1_000L);
        long hours = totalSeconds / 3_600L;
        long minutes = (totalSeconds % 3_600L) / 60L;
        long seconds = totalSeconds % 60L;
        if (hours > 0L) {
            return String.format("%d:%02d:%02d remaining", hours, minutes, seconds);
        }
        return String.format("%d:%02d remaining", minutes, seconds);
    }

    private static Icon countdownIcon(int minute) {
        synchronized (COUNTDOWN_ICONS) {
            Icon cached = COUNTDOWN_ICONS.get(minute);
            if (cached != null) {
                return cached;
            }

            Icon icon = createCountdownIcon(minute);
            COUNTDOWN_ICONS.put(minute, icon);
            return icon;
        }
    }

    private static Icon createCountdownIcon(int minute) {
        String text = Integer.toString(minute);
        int size = 96;
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
        paint.setColor(Color.WHITE);
        paint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));
        paint.setTextAlign(Paint.Align.CENTER);

        float textSize = text.length() > 1 ? 58F : 70F;
        paint.setTextSize(textSize);
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);
        while ((bounds.width() > size - 10 || bounds.height() > size - 10) && textSize > 18F) {
            textSize -= 2F;
            paint.setTextSize(textSize);
            paint.getTextBounds(text, 0, text.length(), bounds);
        }

        float baseline = (size - bounds.top - bounds.bottom) / 2F;
        canvas.drawText(text, size / 2F, baseline, paint);
        return Icon.createWithBitmap(bitmap);
    }
}
