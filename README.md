# Wakerolls

Android app to randomly roll daily activities and meals from a customizable library with rarity-weighted selection.

## Screens

Swipe between four pages (first launch shows a welcome screen):

- **Roll** — Pick a scenario, tap "Roll the day" to get weighted-random items. Cards animate in with a scale effect. Reroll individual cards via the refresh icon, or reroll all with the button. Inner glow on cards indicates rarity. Results persist between app restarts.
- **Items** — Browse, add, edit, and delete items. Each has a name, category (free text), rarity, and enabled toggle. Categories are foldable with a fold/unfold all button. Sort by A-Z or rarity (categories always sorted A-Z).
- **Scenarios** — Define roll templates. Each scenario has named slots with a category and count (e.g. "2x Breakfast, 1x Activity"). A warning shows when a category has fewer items than requested.
- **Settings** — Configure rerolls (allow/disallow, per-day limit, partial rerolls), rarity weights, animations, daily reminders, and JSON import/export.

## Rarity

Items have one of four rarities affecting roll probability:

| Rarity    | Default Weight | Color  |
|-----------|----------------|--------|
| Common    | 10             | Gray   |
| Uncommon  | 6              | Green  |
| Rare      | 3              | Purple |
| Legendary | 1              | Orange |

Weights are configurable in Settings. When a scenario slot requests multiple items (count > 1), picks are made without replacement.

## Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose, Material 3, custom dark theme
- **Architecture:** MVVM (ViewModel + StateFlow + Repository)
- **Database:** Room (SQLite), destructive migration (pre-release)
- **Persistence:** DataStore Preferences (settings, roll state)
- **DI:** Hilt
- **Background:** WorkManager (daily notification)
- **Navigation:** HorizontalPager with bottom NavigationBar
- **CI:** GitHub Actions (test, lint, build)
- **Min SDK:** 26 (Android 8.0)

## Requirements

- JDK 17 (`/usr/lib/jvm/java-17-openjdk`)
- Android SDK (`~/Android/Sdk`)

## Build

```
make build      # assembleDebug
make test       # unit tests
make install    # install on connected device/emulator
make clean      # clean build artifacts
make lint       # run lint checks
```

## Project Structure

```
app/src/main/java/com/wakerolls/
  domain/model/       Item, Rarity, Scenario/ScenarioSlot
  data/db/            Room database, DAOs, entities, converters
  data/repository/    ItemRepository, ScenarioRepository
  data/               ImportExportManager (JSON)
  di/                 Hilt AppModule, DataSeeder
  ui/roll/            RollScreen, RollViewModel
  ui/library/         ItemsScreen, ScenariosScreen, edit dialogs
  ui/settings/        SettingsScreen, SettingsViewModel
  ui/welcome/         WelcomeScreen (first launch)
  ui/navigation/      HorizontalPager NavGraph
  ui/theme/           Dark theme colors, typography
  worker/             DailyRollWorker (WorkManager)
```
