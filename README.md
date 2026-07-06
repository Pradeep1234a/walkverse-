# WalkVerse AI 🚶‍♂️🌟

WalkVerse AI is a premium, next-generation offline Android step tracker built using **Kotlin**, **Jetpack Compose**, **Material 3**, and custom **shadcn-inspired components**. It motivates users to stay active by turning physical movement into a game.

## 🚀 Key Features

* **Step Tracking Ring**: A circular progress indicator modeled after shadcn-ui designs, tracking daily steps, distance, active calories, and duration.
* **Virtual Pet Companion**: Feed, play, and level up your virtual buddy (Cat, Dog, Dragon, or Robot) through walking steps.
* **Walk Garden**: Spend gems to buy seeds (Rose, Sunflower, Tulip, Bonsai) and grow them as you walk. Harvest fully bloomed plants for gem rewards.
* **Story Mode**: Complete walking steps to advance through text-based walking adventures.
* **Trophy Room**: Unlock achievements and badges for milestone steps.
* **Custom Themes**: Sleek styling with custom color schemes (Zinc, Slate, Rose, Emerald, Orange) supporting both dark and light modes.
* **Offline-First & Private**: Store all records locally via Room Database and Preferences DataStore. No external login, tracking, or cloud databases.
* **Health Connect**: Synchronize walk data with Android's native Google Health Connect platform.

## 🛠️ Architecture & Stack

* **MVVM Architecture** combined with **Clean Architecture** principles.
* **Room Database** for high-performance offline persistence.
* **Preferences DataStore** for user configurations and preferences.
* **WorkManager** to handle background step updates periodically.
* **Health Connect Client** for secure health sync.

## 👷 CI/CD Workflow

The project contains a GitHub Actions workflow `.github/workflows/build-apk.yml` that:
1. Triggers on pushes to the `main` or `master` branches.
2. Auto-increments the application `versionCode` in `app/build.gradle.kts` and pushes the change back to the repository.
3. Builds both the **normal (debug) APK** and **release APK**.
4. Publishes a GitHub Release with the compiled APKs attached.
