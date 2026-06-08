package com.browserblock.app;

import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public final class AlarmReceiver extends BroadcastReceiver {
    static final String ACTION_REMINDER = "com.browserblock.app.REMINDER";
    static final String ACTION_DISMISS = "com.browserblock.app.DISMISS";
    static final String ACTION_BLOCK_END = "com.browserblock.app.BLOCK_END";

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationHelper.createChannels(context);
        NotificationManager notifications = context.getSystemService(NotificationManager.class);

        if (ACTION_DISMISS.equals(intent.getAction())) {
            notifications.cancel(NotificationHelper.REMINDER_NOTIFICATION_ID);
            return;
        }

        if (ACTION_BLOCK_END.equals(intent.getAction())) {
            if (!BlockState.isActive(context)) {
                BlockState.clear(context);
                BlockNotificationService.stop(context);
                notifications.cancel(NotificationHelper.BLOCK_NOTIFICATION_ID);
            }
            return;
        }

        AlarmScheduler.scheduleNextHour(context);
        KeyguardManager keyguard = context.getSystemService(KeyguardManager.class);
        boolean locked = keyguard != null && keyguard.isKeyguardLocked();
        if (!locked && !BlockState.isActive(context)) {
            notifications.notify(
                NotificationHelper.REMINDER_NOTIFICATION_ID,
                NotificationHelper.reminder(context));
        }
    }
}
