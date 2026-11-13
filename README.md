# 📱 BillTrack - Android Bill Tracker & Payment Deadline Manager

> **"Everything in the app is visible and under the cover"**

BillTrack is a modern Android application built with Kotlin and Jetpack Compose for managing bill payment deadlines, advance notifications, multi-currency financial tools, and local/cloud storage.

---

## 🚀 Publishing & Downloading on GitHub

### 1. Push Code from AI Studio to GitHub
1. Open the **Settings / Menu** icon in the Google AI Studio editor.
2. Select **Push to GitHub** and choose your repository.

### 2. Automatic APK Build on GitHub (Actions & Releases)
This repository includes `.github/workflows/build-apk.yml`, which automatically builds an installable Android `.apk` whenever code is pushed to `main` or `master`:
1. Open your repository on GitHub and click the **Actions** tab.
2. Open the latest **Build Android APK** run.
3. Download **`BillTrack-APK`** from the **Artifacts** section.
4. *(Optional)* To publish a public download link on the **Releases** page, create a version tag (e.g., `v1.2.0`) on GitHub and `app-debug.apk` will be attached automatically.

### 3. Download Source Code or APK from AI Studio
* In Google AI Studio, open the **Settings / Menu** icon and select **"Export Project as ZIP"** or **"Generate APK / AAB"**.

---

## 📥 In-App File Download Center
Inside the **BillTrack** app under the **Settings** tab:
* **Download CSV**: Saves all cloud and local bill records directly to your device `Downloads` folder.
* **Backup JSON**: Saves a JSON backup summary directly to your device `Downloads` folder.
* **Open on Google Play Store**: Opens the app's Play Store listing.

---

## ⚙️ Build Locally

```bash
# Decode debug keystore (if not present)
base64 -d debug.keystore.base64 > debug.keystore

# Build the Debug APK
gradle assembleDebug

# Or build the Google Play App Bundle (AAB)
gradle bundleRelease
```

The generated Debug APK is located at:
`app/build/outputs/apk/debug/app-debug.apk`

---

## ✨ Core Features

* 📅 **Interactive Calendar View:** Monthly view with color-coded due date indicators.
* 🔔 **Customizable Payment Reminders:** Set notifications 1, 3, 5, or 7 days in advance.
* 🌐 **10 App Languages:** Including English, Arabic, Spanish, French, German, Hindi, Chinese, Portuguese, Japanese, and Russian.
* 💱 **Multi-Currency Support:** USD ($), EUR (€), GBP (£), JPY (¥), AED, SAR, EGP.
* 💾 **Dual Storage Architecture:**
  * **Cloud Sync:** Firebase Cloud Firestore + Google Sign-In.
  * **Local Storage:** On-device SQLite Room database for Guest mode.
* 👤 **Flexible Auth:** Google Sign-In and instant Guest mode access.
* 📊 **Financial Tools:** Debt payoff calculator, bill splitter, biometric security, and direct Downloads CSV/JSON export.

---

© 2026 BillTrack v1.2.0 (Build 76). Everything in the app is visible and under the cover.
