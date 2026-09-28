# Walkthrough - appFoco (Gamified Productivity Virtual Cat App)

appFoco has been successfully created as an Android productivity app featuring a virtual cat pet representing user discipline and progress.

## Features Implemented

1. **Centralized Business Rules & Engine (`PetEngine` & `AppRepository`)**:
   - Pet starts at Level 1 (Filhote).
   - Maximum +1 level per day enforced centrally; the same day cannot generate +1 twice.
   - Level cap at Level 100 (Adult Cat); level 101 is restricted.
   - Death logic: when pet dies (inactivity or forbidden rule), it resets to level 1 and becomes a puppy again. Past life history (highest level, highest streak, death count, milestones) is safely preserved.
   - Virtual date support for Developer Mode without altering system time.

2. **Data Layer (Room Database)**:
   - Entities for Pet, Habits, Habit Logs, Tasks, Forbidden Rules, Death Records, Milestones, and App Settings.
   - Robust DAO and Room Database implementation.

3. **ViewModels**:
   - `PetViewModel`, `HabitViewModel`, `TaskViewModel`, `RulesViewModel`, and `DeveloperViewModel` managing all UI state and business interactions.

4. **Jetpack Compose UI (Dark Productivity Theme)**:
   - **Pet Tab (`PetScreen`)**: Center cat display responding to level/phases, energy status, streak count, today's habits and tasks checklists.
   - **Settings Tab (`SettingsScreen`)**: History/stats, permissions manager, about info, and secret 7-tap entry to Developer Mode.
   - **Forbidden Rule Dialog**: Immediate confirmation dialog ("Você realmente quer matar seu pet?") before triggering death.
   - **Developer Mode Screen (`DeveloperScreen`)**: Complete testing dashboard (levels 1 to 100, advance 1/5/10/30/100 days, death testing, resurrection, energy controls, fake test data generator, and reset).

5. **Backup & Import (`BackupManager`)**:
   - Export to Downloads folder in `.txt` containing structured JSON data.
   - Import with error handling for invalid JSON or incorrect structure.

6. **Automated Unit Tests (`PetEngineTest`)**:
   - Tested rules: starting level 1, max +1 per day, level 100 cap, cat phases, and survival checks.

## How to Run & Test
1. Open the project in Android Studio.
2. Run the app on an emulator or physical device.
3. Use the Pet screen to check off habits and tasks to level up your cat.
4. Go to **Settings**, tap the About card 7 times to unlock **Modo Desenvolvedor**, and test all evolution levels, time simulation, death, and backups instantly.
