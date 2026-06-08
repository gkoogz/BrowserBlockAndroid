# BrowserBlock

BrowserBlock is a minimal Android app that starts a one-hour block when its
launcher icon is tapped. While active, its Accessibility Service returns the
user to the Home screen when a supported browser or YouTube app opens.

Features:

- One-hour browser and YouTube/ReVanced blocking
- Persistent countdown notification
- Hourly reminder notification when the phone is unlocked
- Reboot recovery and exact block-end alarms
- Dynamic browser discovery plus broad package-name fallbacks

The app requires notification, exact-alarm, and Accessibility permissions.
Android still allows the user to disable or uninstall the app from system
settings.

## Build

Build with Android SDK 35 and JDK 17:

```powershell
gradle assembleDebug
```

The debug APK is written to `app/build/outputs/apk/debug/app-debug.apk`.
