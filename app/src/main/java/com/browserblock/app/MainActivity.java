package com.browserblock.app;

import android.Manifest;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.accessibility.AccessibilityManager;

import java.util.List;

public final class MainActivity extends Activity {
    static final String ACTION_START_BLOCK = "com.browserblock.app.START_BLOCK";
    private static final int NOTIFICATION_PERMISSION_REQUEST = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        boolean ready = isReady();
        if (ready) {
            setTheme(R.style.ReadyLaunchTheme);
        }
        super.onCreate(savedInstanceState);
        NotificationHelper.createChannels(this);
        AlarmScheduler.scheduleNextHour(this);

        if (ready) {
            startBlockAndExit();
        } else {
            showSetup();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isReady() && findViewById(android.R.id.content) != null) {
            startBlockAndExit();
        }
    }

    private void startBlockAndExit() {
        long endTime = BlockState.start(this);
        AlarmScheduler.scheduleBlockEnd(this, endTime);
        NotificationManager notifications = getSystemService(NotificationManager.class);
        notifications.cancel(NotificationHelper.REMINDER_NOTIFICATION_ID);
        notifications.notify(
            NotificationHelper.BLOCK_NOTIFICATION_ID,
            NotificationHelper.activeBlock(this, endTime));
        BlockNotificationService.start(this);
        BrowserBlockService.activateAndGoHome();
        Intent home = new Intent(Intent.ACTION_MAIN)
            .addCategory(Intent.CATEGORY_HOME)
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(home);
        finishAndRemoveTask();
    }

    private boolean isReady() {
        return notificationsAllowed() && accessibilityEnabled() && exactAlarmsAllowed();
    }

    private boolean notificationsAllowed() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
            || checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED;
    }

    private boolean exactAlarmsAllowed() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.S
            || getSystemService(AlarmManager.class).canScheduleExactAlarms();
    }

    private boolean accessibilityEnabled() {
        AccessibilityManager manager =
            (AccessibilityManager) getSystemService(Context.ACCESSIBILITY_SERVICE);
        List<AccessibilityServiceInfo> services =
            manager.getEnabledAccessibilityServiceList(
                AccessibilityServiceInfo.FEEDBACK_ALL_MASK);
        ComponentName expected = new ComponentName(this, BrowserBlockService.class);
        for (AccessibilityServiceInfo service : services) {
            ComponentName enabled = ComponentName.unflattenFromString(service.getId());
            if (expected.equals(enabled)) {
                return true;
            }
        }
        return false;
    }

    private void showSetup() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(48, 48, 48, 48);
        layout.setBackgroundColor(Color.WHITE);

        TextView explanation = new TextView(this);
        explanation.setText(
            "One-time setup\n\nBrowserBlock needs notification, exact alarm, and "
                + "Accessibility access. Android will still allow the app to be disabled "
                + "or uninstalled from system settings.");
        explanation.setTextColor(Color.BLACK);
        explanation.setTextSize(18);
        explanation.setGravity(Gravity.CENTER);
        layout.addView(explanation, new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        if (!notificationsAllowed() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            addButton(layout, "Allow notifications", view ->
                requestPermissions(
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    NOTIFICATION_PERMISSION_REQUEST));
        }

        if (!exactAlarmsAllowed() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            addButton(layout, "Allow exact hourly reminders", view -> {
                Intent intent = new Intent(
                    Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                    Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            });
        }

        if (!accessibilityEnabled()) {
            addButton(layout, "Enable browser blocking", view ->
                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));
        }

        setContentView(layout);
    }

    private void addButton(
            LinearLayout layout, String label, android.view.View.OnClickListener listener) {
        Button button = new Button(this);
        button.setText(label);
        button.setOnClickListener(listener);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = 24;
        layout.addView(button, params);
    }
}
