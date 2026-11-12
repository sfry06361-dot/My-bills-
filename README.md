# 📱 BillTrack - Android Bill Tracker & Payment Deadline Manager

> **"Everything in the app is visible and under the cover"**

BillTrack is a modern Android application built with Kotlin and Jetpack Compose for managing bill payment deadlines, advance notifications, multi-currency financial tools, and local/cloud storage.

---

## 📥 How to Download the App & Files

### 1. Download Source Code as a ZIP File (from GitHub)
1. Scroll to the top of this GitHub repository page.
2. Click the green **`Code`** button.
3. Select **`Download ZIP`**.

### 2. Download Pre-Built APK / Project from AI Studio
1. Open your project panel in **Google AI Studio**.
2. Click the **Settings / Menu** icon in the top right corner.
3. Click **"Export Project as ZIP"** or **"Build & Download APK"**.

### 3. Download Files Directly Inside the App
When running the app on your phone or emulator:
1. Open the **BillTrack** app.
2. Navigate to the **Settings** tab.
3. Under **In-App File & App Download Center**, tap:
   - **Download App (APK)**: Downloads/shares the installer package (`BillTrack_v1.2.0_App.apk`).
   - **Download Data (CSV)**: Exports all your bills as a CSV document file.

---

## ⚙️ How to Build the APK File Locally

If you cloned or downloaded this repository, you can build the APK file using Gradle:

```bash
# Clone the repository
git clone https://github.com/sfry06361-dot/your-repo.git
cd your-repo

# Build the Debug APK
gradle assembleDebug
```

After compilation, the APK file will be located at:
`app/build/outputs/apk/debug/app-debug.apk`

---

## ✨ Core Features

* 📅 **Interactive Calendar View:** Monthly view with color-coded due date indicators.
* 🔔 **Customizable Payment Reminders:** Set notifications 1, 3, 5, or 7 days in advance.
* 🌐 **10 App Languages:** Including English, Arabic (Arpic), Spanish, French, German, Hindi, Chinese, Portuguese, Japanese, and Russian.
* 💱 **Multi-Currency Support:** USD ($), EUR (€), GBP (£), JPY (¥), AED, SAR, EGP.
* 💾 **Dual Storage Architecture:**
  * **Cloud Sync:** Firebase Cloud Firestore + Google Sign-In.
  * **Local Storage:** On-device SQLite Room database for Guest mode.
* 👤 **Flexible Auth:** Google Sign-In and instant Guest mode access.
* 📊 **Financial Tools:** Debt payoff calculator, bill splitter, biometric security, and CSV file exporter.

---

## 🛠 Tech Stack

* **Language:** Kotlin
* **UI:** Jetpack Compose (Material 3)
* **Architecture:** MVVM + StateFlow + Clean Architecture
* **Database:** Firebase Cloud Firestore & Android Room SQLite
* **Authentication:** Google Identity Services (Credential Manager)

---

© 2026 BillTrack v1.2.0 (Build 76). Everything in the app is visible and under the cover.
