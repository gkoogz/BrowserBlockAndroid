package com.browserblock.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public final class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationHelper.createChannels(context);
        AlarmScheduler.scheduleNextHour(context);
        if (BlockState.isActive(context)) {
            long endTime = BlockState.endTime(context);
            AlarmScheduler.scheduleBlockEnd(context, endTime);
            BlockNotificationService.start(context);
        } else {
            BlockState.clear(context);
            BlockNotificationService.stop(context);
        }
    }
}
