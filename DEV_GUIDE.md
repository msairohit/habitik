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

### Option A: Modern (Android 11+) — No USB Cable Needed

This method uses dynamic ports shown in your phone's settings.

1.  **Enable Wireless Debugging**: Settings → Developer Options → **Wireless Debugging** (toggle ON).
2.  **Pair (First time only)**:
    - Tap **"Pair device with pairing code"**.
    - Note the **IP address & Port** (e.g., `192.168.1.8:44303`) and the **6-digit pairing code**.
    - In your terminal, run:
      ```powershell
      adb pair 192.168.1.8:44303  # Use the port from the Pairing dialog
      ```
    - Enter the code when prompted.
3.  **Connect**:
    - Look at the main **Wireless Debugging** screen (not the pairing dialog) for the **"IP address & Port"**. This port is usually different from the pairing port.
    - Run:
      ```powershell
      adb connect 192.168.1.8:44159  # Use the port from the main screen
      ```
4.  **Verify**: `adb devices` should list the device.

---

### Option B: Classic — USB Cable Required Once

Use this for older Android versions or if Option A is failing.

1.  **Prepare**: Enable **USB Debugging** in Developer Options.
2.  **Toggle TCP Mode**: Connect via USB and run:
    ```powershell
    adb tcpip 5555
    ```
3.  **Find IP**: On phone: Settings → Wi-Fi → [Your Network] → IP Address.
4.  **Connect**: Unplug USB and run:
    ```powershell
    adb connect 192.168.1.8:5555
    ```

---

### Using with `dev.ps1`

Once connected via ADB, you can use the `-Wireless` flag (though most commands will work automatically if only one device is connected):

```powershell
.\dev.ps1 -Wireless 192.168.1.8:44159
```

> [!TIP]
> If you reboot your phone or change Wi-Fi, you usually only need to repeat the **Connect** step (Step 3 in Option A). Pairing is usually remembered until you "Forget" the PC in the phone's settings.

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
adb connect 192.168.1.8:44159
```

---

### App not launching after install

```powershell
adb shell am force-stop com.habitik
adb shell am start -n com.habitik/.MainActivity
```
