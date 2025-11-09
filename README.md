# CyberShield-X

Fully offline Android security app with real-time malware detection, privacy monitoring, and device protection.

## Quick Setup

### 1. Build & Run

Open in Android Studio and click Run, or:
```bash
./gradlew assembleDebug
```

### 2. Grant Permissions

- Settings → Apps → CyberShield-X → Permissions → Allow all
- Settings → Apps → Special access → Usage access → Enable

That's it! No backend needed.

## Features

### 🛡️ Core Security
✅ **AI-Powered Threat Prediction** - Intelligent malware detection using ML models  
✅ **Real-time App Scanning** - Scans all installed apps via PackageManager  
✅ **Malware Detection & Banned App Alerts** - Identifies harmful apps instantly  

### 🔍 Web Protection
✅ **Phishing URL Scanner** - Protects against fraudulent websites  
✅ **Real-time Link Analysis** - Checks suspicious URLs before visiting  

### 🔒 Privacy & Security
✅ **Privacy Guard** - Monitors app permissions and background activities  
✅ **Hardware Resource Control** - Restricts camera, mic, GPS access  
✅ **Biometric App Locker** - Fingerprint/face unlock for sensitive apps  
✅ **Encrypted Storage** - Room + SQLCipher encrypted database  

### 📱 Device Protection
✅ **Remote Wipe** - Secure data erasure in case of theft  
✅ **SIM Change Alerts** - Instant notifications on SIM tampering  
✅ **Background Scanning** - WorkManager for continuous protection  

### 👨‍👩‍👧 Parental Controls
✅ **Screen Time Management** - Daily usage limits  
✅ **App Usage Tracking** - Monitor app activity  
✅ **Content Filtering** - Safe browsing for kids  

### 💾 Data Management
✅ **Encrypted Backup** - End-to-end encrypted data storage  
✅ **100% Offline** - No internet required for core features

## Tech Stack

- Kotlin + Jetpack Compose + Material 3
- Room + SQLCipher (encrypted local DB)
- WorkManager (background jobs)
- BiometricPrompt (fingerprint/face unlock)
- Local threat analyzer (no AI backend needed)

## How Threat Detection Works

The app analyzes apps locally based on:
- Number of dangerous permissions
- Suspicious app names/keywords
- Internet + sensitive data combinations
- Non-system apps with excessive permissions

No internet connection or backend server required!

## Troubleshooting

**App crashes**
- Check logcat for errors
- Ensure all permissions are granted
- Clear app data and reinstall

**Biometric not working**
- Enable fingerprint/face unlock in device settings
- Test on real device (not emulator)

**No apps showing**
- Grant "Query all packages" permission
- Restart the app
