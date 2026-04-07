# Wakerolls

Android app to randomly roll daily activities and meals from a customizable library with rarity-weighted selection.

## Screens

Swipe between four pages:

- **Roll** — Pick a scenario, tap "Roll the day" to get weighted-random items. Tap a card to reroll that slot.
- **Items** — Browse, add, edit, and delete items. Each has a name, category (free text), rarity, and enabled toggle.
- **Scenarios** — Define roll templates. Each scenario has named slots with a category and count (e.g. "2x Breakfast, 1x Activity").
- **Settings** — Toggle daily reminder notifications with configurable time.

## Rarity

Items have one of four rarities affecting roll probability:

| Rarity    | Weight | Color  |
|-----------|--------|--------|
| Common    | 6      | Gray   |
| Uncommon  | 3      | Green  |
| Rare      | 1      | Purple |
| Legendary | 1      | Orange |

When a scenario slot requests multiple items (count > 1), picks are made without replacement.

## Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose, Material 3, custom dark theme
- **Architecture:** MVVM (ViewModel + StateFlow + Repository)
- **Database:** Room (SQLite) with migration support
- **DI:** Hilt
- **Background:** WorkManager (daily notification)
- **Navigation:** HorizontalPager with bottom NavigationBar
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
  di/                 Hilt AppModule, DataSeeder
  ui/roll/            RollScreen, RollViewModel
  ui/library/         ItemsScreen, ScenariosScreen, edit dialogs
  ui/settings/        SettingsScreen, SettingsViewModel
  ui/navigation/      HorizontalPager NavGraph
  ui/theme/           Dark theme colors, typography
  worker/             DailyRollWorker (WorkManager)
```
