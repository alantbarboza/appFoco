# Implementation Plan - appFoco (Gamified Productivity Virtual Cat App)

appFoco is an Android productivity app with gamification where users take care of a virtual cat pet representing their discipline and progress. Fulfilling daily habits and tasks keeps the cat alive and makes it evolve up to level 100 (Adult Cat).

## User Review Required & Approved Corrections

> [!IMPORTANT]
> **Key Architecture & Design Corrections:**
> - **Centralized Business Rules (`PetEngine` / `AppRepository`):** Survival and progression logic is centralized. Max +1 level per day enforced using local device date (or virtual date when in Developer Mode). Same day cannot grant +1 twice.
> - **Death & History:** Upon death, pet resets to level 1 (filhote). History of past life is saved (highest level reached, highest streak, death count, death date, death reason, milestone history). No resurrection keeping previous level.
> - **Level Cap:** Level capped at 100 (Adult Cat). Level 101 not allowed.
> - **Energy:** Energy represents visual progress/state without complex micro-management (no feeding/water needs).
> - **Developer Mode & Virtual Date:** Developer Mode uses a virtual date simulation that does not change Android system time. Supports testing levels 1, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100; +1 level; advance 1/5/10/30/100 days; death; reset to 1; energy; habits; tasks; forbidden rules; streaks; immediate notifications; permissions; export/import; invalid JSON; data reset; fake data generation.
> - **Backup & Import:** Storage Access Framework used for export (.txt with JSON structure, saving to Downloads) and file picker for import.

## Proposed Changes

### 1. Project Setup & Dependencies
- Configure `build.gradle.kts` (app and project level) with Room, Compose, Navigation, WorkManager, Gson dependencies.

### 2. Data Layer (Database & Entities)
- **Entities:** `PetEntity`, `HabitEntity`, `HabitLogEntity`, `TaskEntity`, `ForbiddenRuleEntity`, `DeathRecordEntity`, `PetHistoryEntity`, `AppSettingsEntity`.
- **DAO & Database:** `AppDatabase` with DAOs and type converters.
- **Repository & Engine:** `AppRepository` & `PetEngine` handling centralized survival rules, daily progression (+1 cap), death/history tracking, and virtual date handling for Developer Mode.

### 3. Business Logic & ViewModels
- `PetViewModel`, `HabitViewModel`, `TaskViewModel`, `RulesViewModel`, `DeveloperViewModel`.

### 4. UI Layer (Jetpack Compose - Dark Theme)
- Theme & Colors (`Theme.kt`, `Color.kt`).
- Main navigation with 2 tabs: Pet Tab (`PetScreen`) and Settings Tab (`SettingsScreen`).
- Forbidden Rule Dialog.
- Developer Mode Screen (`DeveloperScreen`) with all requested simulation testing tools.

### 5. Notifications & Background Workers
- Notification Helper with **Cat Paw icon**.
- WorkManager `PetCheckWorker` for daily status checks.

### 6. Automated & Unit Tests
- Unit tests for +1 per day rule, max +1 cap per day, death reset to level 1, level 100 cap, phase changes, backup JSON export/import validation, and invalid JSON error handling.

## Verification Plan

### Automated Tests
- Run unit tests via Gradle: `./gradlew test`

### Manual Verification
- Deploy app to emulator/device.
- Verify Pet Tab displays level 1 filhote and checklists correctly.
- Enter Developer Mode, test advancing days, leveling up to adult (level 100), simulating death and resurrection, backup export/import.
