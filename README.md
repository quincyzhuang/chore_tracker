# ChoreTracker

A local-first Android app for tracking household chores across four cadences: daily, weekly, biweekly, and monthly.

## Features

- **Overview** — See which chores still need to be done for the current day, week, biweek, and month
- **Chores** — Check off chores as you complete them; each check-in is timestamped
- **Manage** — Add or delete chores, organised by category
- **History** — Browse past completions filtered by 7 days, 30 days, or all time, with undo support
- **Auto-cleanup** — Completions older than 3 months are automatically removed
- **100 % local** — All data lives in a Room (SQLite) database on-device; no network or cloud dependency

## Project structure

```
ChoreTracker/
├── build.gradle.kts              # Root build config
├── settings.gradle.kts           # Project settings
├── gradle.properties             # Gradle JVM / AndroidX settings
├── gradlew.bat                   # Gradle wrapper (Windows)
├── .gitignore
├── gradle/
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
└── app/
    ├── build.gradle.kts          # App module build config
    ├── proguard-rules.pro
    └── src/main/
        ├── AndroidManifest.xml
        ├── res/                  # Drawables, strings, colours, theme XML
        └── java/com/choretracker/app/
            ├── ChoreTrackerApp.kt          # Application class
            ├── MainActivity.kt              # Entry point, bottom navigation
            ├── data/
            │   ├── Chore.kt                # Room entities
            │   ├── ChoreDao.kt             # Data-access object
            │   ├── ChoreDatabase.kt        # Room database singleton
            │   └── ChoreRepository.kt      # Repository layer
            ├── viewmodel/
            │   └── ChoreViewModel.kt       # ViewModel + period logic
            └── ui/
                ├── navigation/
                │   ├── BottomNavItem.kt    # Tab definitions
                │   └── NavGraph.kt         # Compose navigation graph
                ├── screens/
                │   ├── OverviewScreen.kt
                │   ├── ChoresScreen.kt
                │   ├── ManageChoresScreen.kt
                │   └── HistoryScreen.kt
                └── theme/
                    ├── Color.kt
                    ├── Theme.kt
                    └── Type.kt
```

## Requirements

- **Android Studio** Hedgehog (2023.1.1) or later
- **JDK** 17+
- **Android SDK** 34 (included with Android Studio)

## Building

### With Android Studio

1. Open Android Studio
2. **File → Open** → select `C:\ChoreTracker` (or wherever you cloned the repo)
3. Wait for Gradle sync to finish
4. **Build → Build Bundle(s) / APK(s) → Build APK(s)**
5. The APK is written to `app/build/outputs/apk/debug/`

### From the command line

```bash
# On Windows
gradlew.bat assembleDebug

# On macOS / Linux
./gradlew assembleDebug
```

The debug APK will be at `app/build/outputs/apk/debug/app-debug.apk`.

## Tech stack

| Layer | Library |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Navigation | Navigation Compose |
| Database | Room (SQLite) |
| Architecture | ViewModel + Repository pattern |
| Build | Gradle KTS + AGP 8.2.2 |
