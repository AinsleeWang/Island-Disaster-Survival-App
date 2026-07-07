# CLAUDE.md — Island Disaster Survival Guide App

## 1. Project Overview

**Purpose:** A Taiwan-focused Android mobile application providing offline-capable disaster preparedness tools — shelter navigation, emergency contacts, medical info storage, and distress signaling.

**Target Users:** General public in Taiwan (currently data-scoped to Taipei and Tainan), particularly those preparing for earthquakes, floods, fires, and other regional disasters.

**Core Features:**
- Nearest shelter locator using bundled GeoJSON data from local government
- Offline step-by-step navigation to shelters using compass + step detection
- Personal medical card (blood type, allergies, medications, emergency contacts)
- Disaster supply inventory with expiration tracking
- Morse code audio distress signal generator from GPS coordinates
- Offline disaster response handbooks (downloadable guides per disaster type)
- Emergency contact quick-dial and SMS

---

## 2. Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material3 |
| Architecture | MVVM + Repository Pattern |
| Local DB | Room (SQLite), schema version 4 |
| DI | Hilt (Dagger) |
| Async | Kotlin Coroutines + Flow / StateFlow |
| Maps | Google Maps SDK (Maps Compose wrapper) |
| Cloud | Firebase Realtime Database + Analytics |
| Networking | OkHttp + Gson + Kotlinx Serialization |
| Image Loading | Coil |
| Sensors | Android SensorManager (Accelerometer, Magnetometer, Step Detector) |
| Audio | Android AudioTrack (Morse code synthesis) |
| Build | Gradle 8.5.0 (Kotlin DSL), compileSdk 35, minSdk 23 |

**Maps API Key:** stored in `local.properties` (not in repo) — required for map features.
**Firebase Config:** `app/google-services.json` (present in repo).

---

## 3. Project Structure

```
islanddisastersurvivalguideApp3/
├── app/src/main/java/com/example/islanddisastersurvivalguideapp/
│   ├── MainActivity.kt              # App entry point + root navigation (⚠️ oversized ~101KB)
│   ├── data/
│   │   ├── local/                   # Room DB, DAOs, TypeConverters, entity definitions
│   │   ├── model/                   # Domain models (ShelterInfo, SupplyItem, MedicalCard, etc.)
│   │   ├── parser/                  # GeoJSON parser for shelter data
│   │   └── repository/              # Data access abstraction for all features
│   ├── ui/
│   │   ├── screen/                  # One Composable per feature screen
│   │   └── theme/                   # Color, Typography, Theme definitions
│   ├── viewmodel/                   # MVVM ViewModels (one per feature)
│   ├── components/                  # Reusable UI components
│   ├── sensor/                      # NavigationSensorManager (compass + step fusion)
│   └── utils/                       # MorseCodePlayer, SaveStatus enum
├── app/src/main/assets/
│   ├── ShelterInTaipei.geojson      # Bundled shelter data — Taipei
│   ├── ShelterInTainan.geojson      # Bundled shelter data — Tainan
│   └── handbooks/                   # Offline disaster response guides
├── app/schemas/                     # Room DB migration schema exports
├── gradle/libs.versions.toml        # Centralized version catalog
└── app/google-services.json         # Firebase configuration
```

**Key responsibilities:**
- `ShelterViewModel` — shelter list, nearest shelter logic, waypoint/route generation
- `MedicalCardViewModel` — CRUD for personal medical info with validation
- `SupplyViewModel` — supply inventory CRUD with image handling
- `MorseAlarmViewModel` — location state for Morse coordinate conversion
- `NavigationSensorManager` — sensor fusion: accelerometer + magnetometer with low-pass filter (α=0.97)

---

## 4. Current Status

**Completed:**
- Core Compose navigation scaffold and all main screens
- Shelter loading from bundled GeoJSON + nearest shelter calculation (Haversine)
- Medical card CRUD (Room-backed, persisted)
- Frequent location bookmarking with geocoding
- Compass-based offline navigation screen with real-time heading
- Step-count-based distance estimation
- Morse code audio signal generation (partial — see Known Issues)
- Supply inventory CRUD with image support
- Route generation with waypoints at ~50m intervals

**In Progress / Incomplete:**
- `getPrecomputedRoutes()` in `ShelterViewModel` — returns `emptyList()`, not implemented
- Disaster handbook download flow — UI exists, actual download functionality [TBD]
- Firebase integration — declared but minimal active usage found
- `EmergencyTypeCard` onClick — `TODO("handle click")` placeholder

**Known Issues and Bugs:**

| Severity | Location | Issue |
|---|---|---|
| High | `MorseCodePlayer.kt` | Morse map incomplete — only digits 0–9, letters N and E defined; most alphabet missing |
| High | `MorseAlarmViewModel` | Location hardcoded to Kaohsiung test coordinates; not connected to actual GPS |
| Medium | `MainActivity.kt` | ~101KB single file; contains too many screens — needs splitting |
| Low | Date fields | Birth date stored as unvalidated string (`"YYYY-M-D"`) |
| Low | Tests | Only template test files exist; no real test coverage |

---

## 5. Remaining Tasks

**P0 — Bugs to fix before any release:**
1. ~~Fix duplicate `insert` call in `MedicalCardRepository`~~ (fixed 2026-07-06)
2. Complete Morse code alphabet mapping (A–Z + punctuation)
3. Connect `MorseAlarmViewModel` to real GPS location instead of hardcoded test value

**P1 — Core feature completion:**
4. Implement `getPrecomputedRoutes()` in `ShelterViewModel`
5. Complete disaster handbook download and offline storage
6. Wire up `EmergencyTypeCard` onClick action
7. ~~Fix wrong package name in `SupplyRepository`~~ (fixed 2026-07-06)

**P2 — Quality and maintainability:**
8. Split `MainActivity.kt` into per-screen files
9. Add input validation for date fields and blood type
10. Write basic unit tests for repository and ViewModel layers
11. Clarify Firebase usage — define what data is stored remotely [TBD]

---

## 6. Dev Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Install to connected device/emulator
./gradlew installDebug

# Run unit tests
./gradlew test

# Run instrumented tests (requires device/emulator)
./gradlew connectedAndroidTest

# Lint check
./gradlew lint

# Clean build artifacts
./gradlew clean

# Full clean build
./gradlew clean build
```

**Prerequisites:**
- `local.properties` must contain: `MAPS_API_KEY=<your_google_maps_api_key>`
- `app/google-services.json` must be present (already in repo)
- Android Studio or Java 17+ JDK installed
- JVM heap configured to 2048m (set in `gradle.properties`)

---

## 7. Coding Conventions

**Naming:**
- Classes / Composables: `PascalCase`
- Functions / variables: `camelCase`
- Constants: `UPPER_SNAKE_CASE`
- Private StateFlow backing fields: prefix with `_` (e.g., `_shelters` → `shelters`)
- DAO: `<Entity>Dao`, Repository: `<Entity>Repository`, ViewModel: `<Feature>ViewModel`
- Screen Composables: `<Feature>Screen()`, Components: `<Type>Card()` / `<Type>Button()`

**Code Style:**
- Prefer `val` over `var`
- Use sealed classes / enums for typed state (see `SaveStatus`)
- Reactive state via `StateFlow` + `collectAsState()` in Compose
- Use `withContext(Dispatchers.IO)` for all database and I/O operations
- All new comments and documentation must be written in **English**

**API / Data Format:**
- Shelter data: GeoJSON (WGS84 coordinates)
- Distance: Haversine formula, Earth radius 6371km
- Route waypoints: `RouteWaypoint` model, spaced ~50m apart
- DB migrations: export schema to `app/schemas/` on every version increment

---

## 8. Collaboration Rules

**What Claude should always do:**
- Read existing code before suggesting or making any changes
- State the plan in one sentence before executing non-trivial edits
- Mark unconfirmed information with `[TBD]` — never fabricate implementation details
- Preserve all existing comments and logic unless explicitly asked to remove them

**What Claude must not do without explicit instruction:**
- Delete any file, folder, or database entity
- Modify `google-services.json`, `local.properties`, or any credential/config file
- Refactor or split files (e.g., `MainActivity.kt`) unless specifically requested
- Add new dependencies to `libs.versions.toml` or `build.gradle.kts` without approval
- Push commits, create branches, or modify git history

**Scope discipline:**
- Fix only what is asked — do not clean up surrounding code opportunistically
- If a task grows in scope, pause and confirm before continuing
- One task per session; use `/clear` between unrelated tasks

<!-- Claude Code read/write test passed ✅ -->
