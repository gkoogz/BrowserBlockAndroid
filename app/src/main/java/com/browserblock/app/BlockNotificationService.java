package com.browserblock.app;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

public final class BlockNotificationService extends Service {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable refreshNotification = new Runnable() {
        @Override
        public void run() {
            if (!BlockState.isActive(BlockNotificationService.this)) {
                return;
            }

            long endTime = BlockState.endTime(BlockNotificationService.this);
            getSystemService(android.app.NotificationManager.class)
                .notify(
                    NotificationHelper.BLOCK_NOTIFICATION_ID,
                    NotificationHelper.activeBlock(BlockNotificationService.this, endTime));
            handler.postDelayed(this, 1_000L);
        }
    };
    private final Runnable finishBlock = () -> {
        if (!BlockState.isActive(this)) {
            BlockState.clear(this);
            stopForeground(STOP_FOREGROUND_REMOVE);
            stopSelf();
        }
    };

    static void start(Context context) {
        Intent intent = new Intent(context, BlockNotificationService.class);
        context.startForegroundService(intent);
    }

    static void stop(Context context) {
        context.stopService(new Intent(context, BlockNotificationService.class));
        context.getSystemService(android.app.NotificationManager.class)
            .cancel(NotificationHelper.BLOCK_NOTIFICATION_ID);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        NotificationHelper.createChannels(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!BlockState.isActive(this)) {
            stopForeground(STOP_FOREGROUND_REMOVE);
            stopSelf();
            return START_NOT_STICKY;
        }

        long endTime = BlockState.endTime(this);
        AlarmScheduler.scheduleBlockEnd(this, endTime);
        startForeground(
            NotificationHelper.BLOCK_NOTIFICATION_ID,
            NotificationHelper.activeBlock(this, endTime));
        handler.removeCallbacks(refreshNotification);
        handler.postDelayed(refreshNotification, 1_000L);
        handler.removeCallbacks(finishBlock);
        handler.postDelayed(
            finishBlock, Math.max(1L, endTime - System.currentTimeMillis()));
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacks(refreshNotification);
        handler.removeCallbacks(finishBlock);
        if (!BlockState.isActive(this)) {
            stopForeground(STOP_FOREGROUND_REMOVE);
            getSystemService(android.app.NotificationManager.class)
                .cancel(NotificationHelper.BLOCK_NOTIFICATION_ID);
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
