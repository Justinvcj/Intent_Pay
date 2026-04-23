# IntentPay
### Behavioral Friction-based UPI Expense Control App

IntentPay is a modern Android application built natively with Kotlin and Jetpack Compose. Its core philosophy is to intercept high-value spending patterns triggered via SMS, forcing the user into a mandatory "Reflection" phase where they must justify their expense before it is saved properly into their budget.

## Core Features
- **Auto SMS Detection**: Listens for incoming Bank SMS messages to parse UPI debits.
- **Reflection Overlay**: Stops you and asks "What was this for?" if you exceed your daily chunk limit.
- **Manual Launch**: Safe custom UPI flow bridging directly to your favorite UPI app via `upi://pay` intents.
- **No Cloud Tracker**: Fully offline local system. Data is stored via Room Database.
- **Modern UI**: Jetpack Compose powered dark mode interface.

## Project Structure
- `data/local`: Room Entities and DAO
- `data/parser`: SMS Regex logic and Broadcast Receiver
- `domain`: Core UseCases and Models
- `ui`: Jetpack Compose Screens (Dashboard, Reflection, Settings, Manual Payment)

## Setup & Build Instructions
1. Clone / Copy the `IntentPay` folder.
2. Open Android Studio -> Open Project -> Select `IntentPay`.
3. Give it a moment to sync Gradle dependencies (Compose BOM 2024, Room, and others).
4. Run the project on an emulator (API 26+) or a physical device.
5. On the physical device, ensure you grant the `SMS` permission either from settings or via adb (`adb shell pm grant com.intentpay android.permission.RECEIVE_SMS`).
6. Set your limits in the "Settings" tab on the app.
7. Any matching mock SMS you send to the emulator, or real SMS received on device, will trigger the ReflectionActivity if over the configured reflection limit (Default 200).

## Tech Stack
- Kotlin
- Jetpack Compose
- MVVM + Flow
- Room Database
- Coroutines
