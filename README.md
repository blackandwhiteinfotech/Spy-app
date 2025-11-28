# MDM Android App

A consent-based Mobile Device Management application built with Kotlin.

## Features

- ✅ Remote screen viewing (MediaProjection API)
- ✅ Remote file manager (upload/download/delete)
- ✅ Remote command execution
- ✅ Real-time GPS location tracking
- ✅ Remote app installation/uninstallation
- ✅ Block/allow selected apps
- ✅ Network usage monitoring
- ✅ Remote lock & wipe
- ✅ Secure HTTPS API backend
- ✅ Firebase Realtime Database support

## Build

### Automatic (GitHub Actions)
Push to `main` or `master` branch and the APK will be built automatically.
Download from the **Actions** tab → Select workflow run → **Artifacts**.

### Manual (Android Studio)
1. Open project in Android Studio
2. Add `google-services.json` from Firebase Console
3. Build → Build APK

## Configuration

1. Get `google-services.json` from [Firebase Console](https://console.firebase.google.com)
2. Place it in the `app/` folder
3. Update server URL in `APIClient.kt`

## License

For authorized MDM use only. Requires explicit user consent.
