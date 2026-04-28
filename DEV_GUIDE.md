# Habitik Developer Guide

A quick-reference for building, running, debugging, and troubleshooting the Habitik Android app.

---

## `dev.ps1` — Dev Helper Script

All commands are run from the project root (`C:\ap\habitik`).

### Build + Install + Launch + Stream Logs (default)
```powershell
.\dev.ps1
```
Builds the app, installs it on the connected device, launches `MainActivity`, and streams filtered logcat output. Best for a one-shot dev cycle.

---

### Watch Mode — Auto-rebuild on file change
```powershell
.\dev.ps1 -Watch
```
Polls `./app/src` every 2 seconds. When a file change is detected, it automatically rebuilds and reinstalls the app. Does **not** stream logs in watch mode (to keep the terminal clean).

---

### Log-only Mode — Stream logcat without building
```powershell
.\dev.ps1 -LogOnly
```
Skips build and launch. Just attaches to logcat and filters output to `com.habitik` logs. Useful when the app is already running.

---

### Wireless Mode — Connect via ADB over Wi-Fi
```powershell
.\dev.ps1 -Wireless 192.168.1.42
```
Connects to the device at the given IP on port `5555` before proceeding with the normal build + launch flow. See [Wireless ADB Setup](#wireless-adb-setup) below for prerequisites.

---

## Gradle Commands

Run directly from the project root when you need more control.

| Command | What it does |
|---|---|
| `.\gradlew assembleDebug` | Compiles the debug APK (no install) |
| `.\gradlew installDebug` | Compiles + installs debug APK on connected device |
| `.\gradlew clean` | Deletes the `build/` output directory |
| `.\gradlew --stop` | Stops all running Gradle daemons |
| `.\gradlew assembleDebug --no-daemon` | Builds without a background daemon (slower, but avoids daemon-related bugs) |
| `.\gradlew assembleDebug --stacktrace` | Full stack trace on failure |
| `.\gradlew assembleDebug --info` | Verbose build log |

---

## ADB Commands

| Command | What it does |
|---|---|
| `adb devices` | List all connected devices/emulators |
| `adb logcat \*:V \| Select-String com.habitik` | Stream filtered logs for this app |
| `adb shell am start -n com.habitik/.MainActivity` | Launch the app manually |
| `adb install app\build\outputs\apk\debug\app-debug.apk` | Manually push an APK |
| `adb shell am force-stop com.habitik` | Force-stop the running app |

---

## Wireless ADB Setup

Enables cable-free development over Wi-Fi. Your phone and PC must be on the **same network**.

### Step 1 — Initial USB connection (one-time setup)

1. Enable **Developer Options** on your phone:
   - Settings → About Phone → tap **Build Number** 7 times
2. Enable **USB Debugging** inside Developer Options
3. Connect phone via USB and confirm the RSA key prompt on the phone
4. Verify it's recognized:
   ```powershell
   adb devices
   # Should list your device with status "device"
   ```

### Step 2 — Switch to TCP/IP mode

```powershell
adb tcpip 5555
```

This tells the ADB daemon on the phone to listen on port 5555. You'll see: `restarting in TCP mode port: 5555`.

### Step 3 — Find the phone's IP address

On your phone: Settings → Wi-Fi → tap your network → IP Address  
Or run:
```powershell
adb shell ip route | Select-String "src"
# The address after "src" is your phone's IP
```

### Step 4 — Connect wirelessly

```powershell
adb connect 192.168.1.42:5555
# Replace with your actual phone IP
```

Expected output: `connected to 192.168.1.42:5555`

You can now unplug the USB cable. Verify:
```powershell
adb devices
# Should still list the device
```

### Step 5 — Use with dev.ps1

```powershell
.\dev.ps1 -Wireless 192.168.1.42
```

### Reconnecting after reboot / Wi-Fi change

You only need to redo **Steps 2–4** (the USB cable for Step 2 is needed only briefly to run `adb tcpip 5555`). On Android 11+, you can also use **Wireless Debugging** in Developer Options to pair without a USB cable at all (see below).

### Android 11+ — Fully wireless pairing (no USB needed after first time)

1. Go to Developer Options → **Wireless Debugging** → toggle ON
2. Tap **Pair device with pairing code**
3. Note the IP:port and 6-digit code shown
4. Run:
   ```powershell
   adb pair 192.168.1.42:12345
   # Enter the 6-digit code when prompted
   ```
5. Then connect normally:
   ```powershell
   adb connect 192.168.1.42:5555
   ```

---

## Troubleshooting

### KSP build error: `Storage for id-to-file.tab is already registered`

The Gradle daemon is holding a stale in-memory file lock. Cache deletion alone won't help.

**Fix:**
```powershell
.\gradlew --stop                         # Kill all daemons (releases file locks)
Remove-Item -Recurse -Force app\build    # Clear stale cache on disk
.\gradlew assembleDebug --no-daemon      # Fresh build
```

Once it succeeds, you can go back to `.\dev.ps1 -Watch` normally.

---

### No devices found

```powershell
adb kill-server
adb start-server
adb devices
```

If wireless connection dropped, reconnect:
```powershell
adb connect 192.168.1.42:5555
```

---

### App not launching after install

```powershell
adb shell am force-stop com.habitik
adb shell am start -n com.habitik/.MainActivity
```
