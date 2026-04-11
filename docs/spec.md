# Product Specification

## Overview

A daily randomizer app that rolls items from user-defined categories
using weighted rarity, following a scenario template.
All data is local; JSON is the import/export format.

## Data Model

### Item

Represents something to randomize (a meal, activity, workout, etc.).

| Field | Type | Notes |
|---|---|---|
| `id` | `Long` | Auto-generated primary key |
| `name` | `String` | User-defined label |
| `category` | `String` | Free-text grouping (e.g. "Food", "Activity") |
| `rarity` | `Rarity` | Affects pick probability |
| `enabled` | `Boolean` | If `false`, excluded from rolls |
| `rolledCount` | `Int` | Times this item was selected |
| `completedCount` | `Int` | Times marked complete |

### Rarity

Enum with default weights that determine selection probability.

| Value | Default weight |
|---|---|
| `COMMON` | 10 |
| `UNCOMMON` | 6 |
| `RARE` | 3 |
| `LEGENDARY` | 1 |

Weights are configurable per-rarity in Settings (range 0–20).

### Scenario

A roll template that defines what to draw.

| Field | Type | Notes |
|---|---|---|
| `id` | `Long` | Auto-generated primary key |
| `name` | `String` | User-defined label |
| `slots` | `List<ScenarioSlot>` | Defines categories and counts |

### ScenarioSlot

One slot in a scenario template.

| Field | Type | Notes |
|---|---|---|
| `id` | `Long` | Auto-generated primary key |
| `category` | `String` | Must match an Item category |
| `count` | `Int` | How many items to draw (1–5) |

## Architecture

Single-module Android app (Kotlin, Jetpack Compose, Room, Material 3)
with three layers:

```text
ui/         — Compose screens + ViewModels
domain/     — data classes + weighted-random logic (pure Kotlin)
data/       — Room database, DAOs, repositories, JSON serialization
```

**State management:** ViewModel per screen, `StateFlow` exposed to Compose.
**Dependency injection:** Hilt (`@HiltViewModel`, `@Singleton` repositories).
**Persistence:** Room SQLite (v1, exported=false). DataStore Preferences for settings and roll state.
**Serialization:** `org.json` (Android SDK built-in) for import/export.

Four tabs: **Roll** | **Items** | **Scenarios** | **Settings**

## Roll Logic

### Weighted Random Selection

```text
weightedRandom(items, customWeights):
  totalWeight = sum of (customWeights[rarity] ?? rarity.defaultWeight) for each item
  if totalWeight <= 0: return items.random()
  random = randomInt(1..totalWeight)
  for each item:
    random -= weight(item)
    if random <= 0: return item
  return items.last()
```

### Roll All

1. Validate a scenario is selected.
2. If re-rolling: check `allowRerolls` enabled and `rerollsLeft > 0`.
3. For each slot, check that enough enabled items exist in the category.
   If not, show a warning dialog listing shortages and abort.
4. If re-rolling, consume one reroll.
5. Trigger shrink animation on existing cards (500ms delay if animations enabled).
6. Collect completed results from previous roll, grouped by category.
7. For each scenario slot:
   - Keep up to `slot.count` completed items from previous roll.
   - Pick remaining items via `weightedRandom` without replacement
     (each picked item is removed from the pool).
   - If fewer items available than needed, fill remaining slots with null.
8. Increment `rolledCount` on all newly picked items.
9. Shuffle the final result list.
10. Save results to DataStore.
11. Increment `rollGeneration` (triggers card animation resets).

### Single Reroll

1. Check `allowPartialRerolls` enabled and `rerollsLeft > 0`.
2. Consume one reroll.
3. Pick one new item from the same category via `weightedRandom`.
   (May re-pick the same item; does not exclude current pick.)
4. Increment `rolledCount` on the new item.
5. Replace the card at the given index.

### Completion

- **Complete:** sets `completed = true`, increments `completedCount` on the item.
- **Uncomplete:** sets `completed = false` (does not decrement `completedCount`).
- Completed items are preserved across full rerolls in their category slots.

### Reroll Limits

- `rerollsPerDay` setting controls the daily cap (0 = unlimited).
- Tracks usage via DataStore: `rerolls_date` (ISO date) + `rerolls_used` (count).
- Resets to 0 when the stored date differs from today.
- Both "reroll all" and "reroll single card" consume one reroll each.

### Result Persistence

Results are saved to DataStore as a semicolon-delimited string:

```text
label<TAB>category<TAB>itemId<TAB>completed;label<TAB>category<TAB>itemId<TAB>completed;...
```

- `itemId = -1` means an unfilled slot (null item).
- `completed` is `1` or `0`.

On app restart, results are restored if:
- The saved scenario still exists in the database.
- All referenced items still exist (entries with deleted items are dropped).

Results are restored without animation.

## Screens

### Welcome (first launch)

Shown once, gated by DataStore boolean `welcome_seen`.

- Title and tagline.
- Four concept cards explaining Items, Scenarios, Roll, and Settings.
- "Get started" button sets `welcome_seen = true` and navigates to main pager.

### Roll

- Scenario dropdown selector at top.
  - Auto-selects first scenario on launch.
  - Switching scenarios clears results.
- Reroll counter: "{N} reroll(s) left" (shown when rerolls enabled and not unlimited).
- Prompt text: "Tap roll to discover your day" (before first roll).
- Scrollable list of roll cards (one per slot result).
- Primary button:
  - "Roll the day" (before first roll, disabled if no scenario selected).
  - "Reroll all" / "No rerolls left" (after first roll).
- Insufficient items warning dialog if a slot can't be filled.

### Items

- Title + fold/unfold-all button + sort toggle (A-Z / Rarity).
- Items grouped by category (case-insensitive sort).
- Collapsible category headers with item count.
- Each item row: name, rarity badge, enable/disable switch. Tap to edit.
- FAB to add new item.
- **Item edit dialog:**
  - Name field (required).
  - Category field with autocomplete from existing categories (required).
  - Rarity selector (filter chips for each rarity).
  - Statistics section (existing items only): rolled count, completed count, reset button.
  - Buttons: Delete (existing only) | Cancel | Save.
- Delete confirmation dialog.

### Scenarios

- Title.
- List of scenario cards: name + slot list (category labels with optional ×N count badge).
- Tap card to edit. FAB to add.
- **Scenario edit dialog:**
  - Name field (required).
  - Dynamic slot rows: category dropdown (autocomplete) + count ±1 (range 1–5) + delete.
  - "+ Add slot" button.
  - Buttons: Delete (existing only) | Cancel | Save.
  - Validation: name non-blank, at least one slot, all slot categories non-blank.
- Delete confirmation dialog.

### Settings

Three sections:

**General:**

| Setting | Control | Default | Notes |
|---|---|---|---|
| Allow rerolls | Switch | on | Shows/hides reroll-related controls |
| Rerolls per day | ± counter | 3 | Range 0–10, 0 = unlimited (∞) |
| Allow partial rerolls | Switch | on | Individual card rerolls |
| Enable animations | Switch | on | Card scale-in/out animations |
| Daily reminder | Switch | off | Notification toggle |
| Reminder time | Time picker | 08:00 | Shown when reminder enabled |

**Weights:**

Per-rarity weight rows (COMMON through LEGENDARY). Each row:
- Reset-to-default button (shown when modified).
- Rarity name in rarity color.
- ± counter (range 0–20).

**Data:**

| Action | Button | Notes |
|---|---|---|
| Export | "Export JSON" | SAF file picker, writes `wakerolls.json` |
| Import | "Import JSON" | SAF file picker, replaces all data |
| Statistics | "Reset all" | Zeros all item rolled/completed counts |

Result dialog shows success/error message after import/export.

## Import / Export

### JSON

A single JSON file represents the full app state:

```json
{
  "items": [
    {
      "name": "Morning run",
      "category": "Activity",
      "rarity": "UNCOMMON",
      "enabled": true,
      "rolledCount": 5,
      "completedCount": 3
    }
  ],
  "scenarios": [
    {
      "name": "Full day",
      "slots": [
        { "category": "Food", "count": 3 },
        { "category": "Activity", "count": 1 }
      ]
    }
  ]
}
```

### Behavior

- **Export:** serializes all items and scenarios to pretty-printed JSON (2-space indent).
- **Import:** parses JSON, deletes all existing data, inserts imported data.
  Missing optional fields use defaults (`enabled = true`, counts = 0, `count = 1`).
  Unknown rarity values fall back to `COMMON`.
- IDs are not serialized; they are regenerated on import.
- Items and scenarios are imported independently — a file may contain only one section.

## Notifications

Daily reminder via WorkManager:

- **Worker:** `DailyRollWorker` (CoroutineWorker with Hilt injection).
- **Scheduling:** OneTimeWorkRequest with calculated delay to next occurrence of the configured hour:minute. Replaces previous work via `ExistingWorkPolicy.REPLACE`. Worker re-schedules itself for the next day after firing.
- **Notification:** channel "Daily Roll" (importance DEFAULT), title "Your day is ready!", text "Tap to see today's roll", opens MainActivity on tap, auto-cancel.
- **Channel:** created in `WakerollsApp.onCreate()` (API 26+).
- **Permission:** `POST_NOTIFICATIONS` declared in manifest.

## DataStore Preferences

| Key | Type | Default | Owner |
|---|---|---|---|
| `welcome_seen` | Boolean | false | NavGraph |
| `roll_scenario_id` | Long | — | RollViewModel |
| `roll_results` | String | "" | RollViewModel |
| `notif_enabled` | Boolean | false | SettingsViewModel |
| `notif_hour` | Int | 8 | SettingsViewModel |
| `notif_minute` | Int | 0 | SettingsViewModel |
| `rerolls_per_day` | Int | 3 | SettingsViewModel |
| `rerolls_used` | Int | 0 | RollViewModel |
| `rerolls_date` | String | "" | RollViewModel |
| `allow_rerolls` | Boolean | true | SettingsViewModel |
| `allow_partial_rerolls` | Boolean | true | SettingsViewModel |
| `enable_animations` | Boolean | true | SettingsViewModel |
| `weight_common` | Int | 10 | SettingsViewModel |
| `weight_uncommon` | Int | 6 | SettingsViewModel |
| `weight_rare` | Int | 3 | SettingsViewModel |
| `weight_legendary` | Int | 1 | SettingsViewModel |

## Database

Room database, version 1, `exportSchema = false`.

### Tables

**items:**

| Column | Type | Notes |
|---|---|---|
| id | INTEGER | PK, auto-generate |
| name | TEXT | |
| category | TEXT | |
| rarity | TEXT | TypeConverter (enum name) |
| enabled | INTEGER | Boolean |
| rolledCount | INTEGER | |
| completedCount | INTEGER | |

**scenarios:**

| Column | Type | Notes |
|---|---|---|
| id | INTEGER | PK, auto-generate |
| name | TEXT | |

**scenario_slots:**

| Column | Type | Notes |
|---|---|---|
| id | INTEGER | PK, auto-generate |
| scenarioId | INTEGER | FK → scenarios.id, CASCADE delete |
| category | TEXT | |
| count | INTEGER | |

### Key Queries

- `observeAll` (items): ordered by category, name.
- `observeEnabled(category)`: enabled items in a specific category.
- `observeCategories`: distinct category names, sorted.
- `observeAll` (scenarios): ordered by name, includes slots via `@Relation`.
- `incrementRolled(id)` / `incrementCompleted(id)`: atomic counter updates.
- `resetAllStats` / `resetStats(id)`: zero out counters.

## Build & CI

| Tool | Purpose |
|---|---|
| AGP 8.x | Android Gradle Plugin |
| Kotlin 2.x | Language |
| JDK 21 | Compile target |
| KSP | Annotation processing (Room, Hilt) |

`make build` builds the debug APK.
`make test` runs unit tests.
`make install` installs on connected device.

Makefile sets `JAVA_HOME` and `ANDROID_HOME` automatically.

## Out of Scope (v1)

- Light mode / dynamic theming
- Cloud sync
- Multiple rolls per day (separate morning/evening)
- Roll history / calendar view
- Sharing rolls with others
- Item images or icons
