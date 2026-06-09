<div align="center">
  <h1>💸 Intent Pay: Expense Management App</h1>
  <p>A native, privacy-focused offline Android application designed to intercept bank SMS alerts, classify transactions locally, and curb impulse spending.</p>

  <!-- TECH STACK BADGES -->
  <a href="https://kotlinlang.org/"><img src="https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin"></a>
  <a href="https://developer.android.com/jetpack/compose"><img src="https://img.shields.io/badge/Jetpack_Compose-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Jetpack Compose"></a>
  <a href="https://developer.android.com/training/data-storage/room"><img src="https://img.shields.io/badge/Room_DB-003B57?style=for-the-badge&logo=sqlite&logoColor=white" alt="Room DB"></a>
</div>

<br/>

## 📖 Overview
Existing budget management applications often rely on delayed cloud-based bank synchronizations, failing to intercept transactions in real-time. **Intent Pay** shifts this paradigm by operating completely offline. 

By leveraging native Android `BroadcastReceivers`, the app instantly parses incoming bank SMS alerts and utilizes a local Room DB classification engine to track expenses and trigger behavioral interventions to reduce impulse purchases.

## ✨ Key Features
- **📱 100% Offline Architecture:** Processes all financial data locally on the device, ensuring absolute user privacy and zero reliance on external cloud servers.
- **📩 Real-Time SMS Parsing:** Utilizes Android Broadcast Receivers to instantly detect and parse incoming bank transaction alerts.
- **🧠 Rule-Based Classification:** Implements an intelligent local classification engine via Room DB to accurately categorize various financial transactions.
- **🛑 Behavioral "Reflection" Overlay:** A unique friction mechanism that triggers immediately upon detecting a high-value transaction, enforcing a mandatory pause to help users reconsider their spending.
- **📊 Interactive Analytics Dashboard:** Provides users with clear visual insights, categorized spending breakdowns, and long-term financial habit tracking.

## 🏗️ Architecture Workflow
```text
[ Incoming Bank SMS ] --> (BroadcastReceiver) --> [ Local SMS Parser ]
                                                          |
                                                          V
[ Reflection UI Overlay ] <--- (High Value Rule) <--- [ Classification Engine ]
                                                          |
                                                          V
[ Jetpack Compose Dashboard ] <-------------------- [ Room Database ]
```

## 🚀 Getting Started

### Prerequisites
- Android Studio (Flamingo or later)
- Android SDK (API 24+)
- Kotlin 1.8+

### Installation
1. Clone the repository:
   ```bash
   git clone https://github.com/Justinvcj/Intent_Pay.git
   ```
2. Open the project in **Android Studio**.
3. Allow Gradle to sync dependencies.
4. Run the app on an Android Emulator or a physical device.

> **Note:** To test the core functionality, you will need to grant SMS reading permissions when prompted by the app, and you can use Android Studio's extended controls to simulate an incoming bank SMS.

## 📄 License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
