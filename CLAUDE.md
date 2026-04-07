## Build

Run `make build`, `make test`, `make install` from the project root. The Makefile sets `JAVA_HOME` and `ANDROID_HOME` automatically.

## Architecture

MVVM with Jetpack Compose. ViewModels expose `StateFlow`, composables collect with `collectAsStateWithLifecycle()`. Room for persistence, Hilt for DI.

Navigation uses `HorizontalPager` (4 pages: Roll, Items, Scenarios, Settings) — not Jetpack Navigation. Screens are composable functions without padding parameters; the pager scaffold handles padding.

Items and Scenarios share a single `LibraryViewModel` injected via `hiltViewModel()` — Hilt scopes it per-activity, so both pages see the same state.

## Key conventions

- Category is a free-text `String`, not an enum. Autocomplete suggestions come from `ItemDao.observeCategories()`.
- Rarity is an enum: COMMON, UNCOMMON, RARE, LEGENDARY. Stored in Room via TypeConverter (name string).
- Dialog state lives in ViewModel as nullable fields (`editingItem: Item?`). null = closed, non-null = open.
- Delete flows through a confirmation dialog: `onDeleteClick` sets `showDeleteConfirm`, `onConfirmDelete` executes.
- Room DB is at version 2. Migration 1→2 adds scenarios/scenario_slots tables. Use explicit migrations for future schema changes.

## Style

- Use typing annotations
- Keep commit messages brief
