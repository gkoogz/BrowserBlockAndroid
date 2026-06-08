package com.browserblock.app;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.view.accessibility.AccessibilityEvent;

import java.util.Set;

public final class BrowserBlockService extends AccessibilityService {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Set<String> browserPackages;
    private final Runnable endBlock = () -> {
        if (!BlockState.isActive(this)) {
            BlockState.clear(this);
            stopForeground(STOP_FOREGROUND_REMOVE);
        }
    };

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        browserPackages = BrowserDetector.installedBrowsers(this);
        NotificationHelper.createChannels(this);
        AlarmScheduler.scheduleNextHour(this);
        refreshBlockNotification();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (!BlockState.isActive(this)) {
            return;
        }

        CharSequence packageName = event.getPackageName();
        if (packageName != null && browserPackages.contains(packageName.toString())) {
            performGlobalAction(GLOBAL_ACTION_HOME);
        }
    }

    @Override
    public void onInterrupt() {
        // No spoken or haptic feedback is produced.
    }

    @Override
    public boolean onUnbind(Intent intent) {
        handler.removeCallbacks(endBlock);
        return super.onUnbind(intent);
    }

    static void activateAndGoHome() {
        BrowserBlockService service = InstanceHolder.instance;
        if (service != null) {
            service.performGlobalAction(GLOBAL_ACTION_HOME);
        }
    }

    private void refreshBlockNotification() {
        if (!BlockState.isActive(this)) {
            return;
        }
        long end = BlockState.endTime(this);
        BlockNotificationService.start(this);
        handler.removeCallbacks(endBlock);
        handler.postDelayed(endBlock, Math.max(1L, end - System.currentTimeMillis()));
    }

    @Override
    public void onCreate() {
        super.onCreate();
        InstanceHolder.instance = this;
    }

    @Override
    public void onDestroy() {
        if (InstanceHolder.instance == this) {
            InstanceHolder.instance = null;
        }
        handler.removeCallbacks(endBlock);
        super.onDestroy();
    }

    private static final class InstanceHolder {
        private static BrowserBlockService instance;
    }
}
