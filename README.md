# Tuya Smart Plug Controller (Android - Jetpack Compose)

This is a complete, native Android application written in Kotlin and Jetpack Compose to control a local Tuya smart plug HTTP bridge server.

## Features
- **Title**: "My Smart Plug"
- **Large Status Indicator**: Displays "ON" in green or "OFF" in red
- **Two Large Action Buttons**: Side-by-side "TURN ON" and "TURN OFF"
- **Timer Countdown**:
  - "Turn OFF in 15 minutes" (starts 900s countdown with 1s updates, calls `/off` at zero)
  - "Turn ON in 15 minutes" (starts 900s countdown with 1s updates, calls `/on` at zero)
  - Live remaining time text: `Timer: 14:32`
  - "Cancel Timer" button to stop countdown and clear display
- **HTTP Server Integration**:
  - Target server: `http://192.168.66.6:5000`
  - Endpoints: `GET /on`, `GET /off`, `GET /status` (returns `{"status": "on"}` or `{"status": "off"}`)
  - Auto-fetches `/status` on launch and after every turn on/off action
- **Graceful Error Handling**:
  - Android Toast alerts on network failures and action events
  - Inline error banner when server is unreachable
- **Modern Android Stack**:
  - Jetpack Compose + Material 3
  - Kotlin Coroutines (`viewModelScope`, `Job`, `delay`)
  - Retrofit 2 + OkHttp 4 with Gson converter
  - `android:usesCleartextTraffic="true"` enabled for local plain HTTP LAN communication

## How to Run in Android Studio
1. Open Android Studio (Ladybug / Koala / Hedgehog or newer).
2. Select **File > Open...** and choose this `android` folder.
3. Allow Gradle to sync dependencies.
4. Connect an Android phone to the same Wi-Fi network as your server (`192.168.66.6`), or start an Android Emulator with network routing.
5. Click **Run > Run 'app'** (`Shift + F10`).