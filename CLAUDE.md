## Build

Run `make build`, `make test`, `make install` from the project root. The Makefile sets `JAVA_HOME` and `ANDROID_HOME` automatically.

## Architecture

MVVM with Jetpack Compose. ViewModels expose `StateFlow`, composables collect with `collectAsStateWithLifecycle()`. Room for persistence, Hilt for DI.

Navigation uses `HorizontalPager` (4 pages: Roll, Items, Scenarios, Settings) — not Jetpack Navigation. First launch shows a `WelcomeScreen` gated by a DataStore boolean. Screens are composable functions without padding parameters; the pager scaffold handles padding.

Items and Scenarios share a single `LibraryViewModel` injected via `hiltViewModel()` — Hilt scopes it per-activity, so both pages see the same state.

Roll results persist in DataStore (encoded as tab/semicolon-delimited string with item IDs). Restored on app launch without animation.

## Key conventions

- Category is a free-text `String`, not an enum. Autocomplete suggestions come from `ItemDao.observeCategories()`.
- Rarity is an enum: COMMON, UNCOMMON, RARE, LEGENDARY. Stored in Room via TypeConverter (name string). Default weights 10-6-3-1, configurable via Settings.
- Dialog state lives in ViewModel as nullable fields (`editingItem: Item?`). null = closed, non-null = open.
- Delete flows through a confirmation dialog: `onDeleteClick` sets `showDeleteConfirm`, `onConfirmDelete` executes.
- Room DB is at version 1 with `fallbackToDestructiveMigration()`. Pre-release — no migration needed yet.
- Settings stored in DataStore Preferences, keys defined in `SettingsViewModel.Companion`.
- Card animations (scale in/out) controlled by `enableAnimations` setting, ignoring system accessibility flag.
- Import/export uses `org.json` (Android SDK built-in), no external JSON library. `ImportExportManager` handles serialization.

## Style

- Use typing annotations
- Keep commit messages brief
