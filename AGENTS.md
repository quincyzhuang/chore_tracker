# ChoreTracker — Agent Instructions

## Build commands

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Clean build
./gradlew clean assembleDebug
```

## Project conventions

- **Language:** Kotlin
- **UI:** Jetpack Compose + Material 3 (no XML layouts)
- **Architecture:** ViewModel + Repository + Room DAO
- **Navigation:** Navigation Compose with bottom nav bar (4 tabs)
- **Database:** Room with KSP annotation processor
- **State:** `LiveData`/`Flow` collected as state in Composables

## Code style

- No comments in source files unless the logic is non-obvious
- Use `collectAsState(initial = emptyList())` for Flow observation
- Composable functions use `@OptIn(ExperimentalMaterial3Api::class)` where needed
- UI screens live in `ui/screens/`, data layer in `data/`, ViewModel in `viewmodel/`

## Chore categories

Defined as constants in `ChoreViewModel`:
- `CATEGORY_DAILY`, `CATEGORY_WEEKLY`, `CATEGORY_BIWEEKLY`, `CATEGORY_MONTHLY`
- Category display names via `categoryDisplayName()`

## Periodic logic

Period boundaries are computed in `ChoreViewModel`:
- **Daily:** midnight‑to‑midnight
- **Weekly:** Monday 00:00 – Sunday 23:59
- **Biweekly:** aligns with even/odd ISO weeks
- **Monthly:** calendar month boundaries

## README updates

When adding a new feature, update the **Features** section in `README.md` to reflect the change.

## Versioning

The app version is defined as `APP_VERSION` in `ChoreViewModel`'s companion object.
- **Small feature adds** → increment the patch number by 0.1 (e.g. `v0.1` → `v0.2`)
- **Major feature additions** → promote to the next integer (e.g. `v0.2` → `v1.0`)
