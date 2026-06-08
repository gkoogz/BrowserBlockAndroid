package com.browserblock.app;

import android.content.Context;
import android.content.SharedPreferences;

final class BlockState {
    static final long BLOCK_DURATION_MS = 60L * 60L * 1000L;
    private static final String PREFS = "browser_block";
    private static final String END_TIME = "end_time";

    private BlockState() {}

    static long endTime(Context context) {
        return prefs(context).getLong(END_TIME, 0L);
    }

    static boolean isActive(Context context) {
        return endTime(context) > System.currentTimeMillis();
    }

    static long start(Context context) {
        long end = System.currentTimeMillis() + BLOCK_DURATION_MS;
        prefs(context).edit().putLong(END_TIME, end).apply();
        return end;
    }

    static void clear(Context context) {
        prefs(context).edit().remove(END_TIME).apply();
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }
}
