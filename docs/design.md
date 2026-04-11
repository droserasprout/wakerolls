# Design System

## Colors

### Theme (Dark only)

| Token | Hex | Usage |
|-------|-----|-------|
| Background | #0D0D0F | Screen background (near-black) |
| Surface | #1A1A1F | Cards, rows, dialogs, nav bar |
| SurfaceVariant | #242429 | Dropdown menus, autocomplete popups |
| Primary (AccentGold) | #FFD166 | Buttons, switches, active states, save actions |
| OnPrimary | #0D0D0F | Text on gold buttons |
| Secondary (AccentTeal) | #06D6A0 | Completion check icon, "Add slot" action |
| Tertiary (AccentCoral) | #EF476F | Delete buttons, destructive actions |
| TextPrimary | #F2F2F7 | Main text |
| TextSecondary | #8E8E93 | Subtitles, labels, disabled text |

### Rarity

| Name | Hex | Glow level | Usage |
|------|-----|------------|-------|
| RarityCommon | #8E8E93 | 0 (none) | Common badge, default |
| RarityUncommon | #30D158 | 1 | Uncommon badge, card glow |
| RarityRare | #BF5AF2 | 2 | Rare badge, card glow |
| RarityLegendary | #FF9F0A | 3 | Legendary badge, card glow |

## Typography

Custom `Typography` instance (`WakerollsTypography`), all styles set color directly:

| Style | Weight | Size | Letter Spacing | Color | Used for |
|-------|--------|------|----------------|-------|----------|
| headlineLarge | Bold | 32sp | -0.5sp | TextPrimary | Page titles ("Today's Roll", "Items", etc.) |
| headlineMedium | SemiBold | 24sp | — | TextPrimary | Item names on roll cards, dialog titles |
| titleLarge | SemiBold | 20sp | — | TextPrimary | Counter +/− symbols |
| titleMedium | Medium | 16sp | — | TextPrimary | Settings row titles, item names in lists |
| titleSmall | — | default | — | — | Settings section headers (M3 default) |
| bodyLarge | Regular | 16sp | — | TextPrimary | Welcome descriptions, tagline |
| bodyMedium | Regular | 14sp | — | TextSecondary | Settings subtitles, reroll counter |
| bodySmall | — | default | — | — | Statistics labels (M3 default) |
| labelSmall | Medium | 11sp | 0.5sp | TextSecondary | Category labels, rarity badges, sort button |

## Shapes

No `Shape.kt` — radii applied inline via `RoundedCornerShape`:

| Radius | Usage |
|--------|-------|
| 6dp | Rarity badges, slot count badges |
| 12dp | Item rows, settings rows, weight rows |
| 16dp | Buttons, welcome concept cards |
| 20dp | Roll cards, scenario cards |
| 24dp | Dialogs (ItemEditDialog, ScenarioEditDialog) |

## Shared Components

### NavigationBar (Material 3)

- Material3 `NavigationBar`, containerColor = Surface
- 4 pages: Roll (Star), Items (List), Scenarios (PlayArrow), Settings (Settings)
- Labels: sentence case ("Roll", "Items", "Scenarios", "Settings")
- Synced with `HorizontalPager` via `animateScrollToPage`

### RollCard

- Height: 120dp, fillMaxWidth
- Background: Surface, corner radius 20dp
- Border: 1dp gradient from rarityColor 60%→10% alpha
- Glow: edge overlay via `drawWithContent`, 20dp inset, alpha = glowLevel × 0.12
- Internal padding: 20dp
- Top row: category label (uppercase, labelSmall, 2sp tracking) + RarityBadge
- Bottom row: item name (headlineMedium) + action icons (36dp button, 20dp icon)
- Completed state: TextSecondary text, no glow

### AnimatedRollCard

- Wraps RollCard with scale animation via `Animatable`
- Intro: staggered 100ms delay per card, 450ms scale 0→1, FastOutSlowInEasing
- Outro: 350ms scale 1→0 on `isExiting`
- Respects `enableAnimations` setting; skips animation when disabled

### RarityBadge

- Surface pill: RoundedCornerShape(6dp), rarityColor at 15% alpha
- Text: labelSmall, full rarityColor
- Padding: horizontal 8dp, vertical 3dp

### ScenarioDropdown

- `ExposedDropdownMenuBox` with custom styling
- Anchor: Row with titleMedium text + ArrowDropDown icon (20dp)
- Menu container: SurfaceVariant
- Item text: TextPrimary

### CategoryHeader

- Row: collapse icon (18dp, KeyboardArrowRight/Down) + category (uppercase, labelSmall, 2sp tracking) + count (labelSmall, 50% alpha)
- Padding: vertical 4dp
- Clickable to toggle collapse

### LibraryItemRow

- Row: RoundedCornerShape(12dp), Surface background
- Padding: horizontal 16dp, vertical 12dp
- Content: item name (titleMedium) + RarityBadge + Switch
- Disabled state: TextSecondary name color

### ScenarioCard

- Box: RoundedCornerShape(20dp), Surface background
- Border: 1dp gradient AccentGold 40%→5% alpha
- Padding: 20dp
- Name: headlineMedium
- Slots: category (uppercase labelSmall, 2sp tracking) + optional count badge (×N in gold, 6dp pill)
- Slot row vertical padding: 3dp

### SettingsRow

- Row: RoundedCornerShape(12dp), Surface background
- Padding: horizontal 16dp, vertical 14dp
- Content: title (titleMedium) + subtitle (bodyMedium) + control (Switch/TextButton)

### SectionHeader (Settings)

- Spacer(24dp) + Text (labelSmall, TextSecondary) + Spacer(12dp)

### goldSwitchColors

- Checked thumb: AccentGold
- Checked track: AccentGold at 40% alpha
- Unchecked: M3 defaults

### ConceptCard (Welcome)

- Surface: RoundedCornerShape(16dp), Surface color
- Padding: 20dp
- AnnotatedString: bold TextPrimary title + TextSecondary description, bodyLarge

### FloatingActionButton

- containerColor: AccentGold, contentColor: Background
- Icon: Icons.Filled.Add
- Padding from edge: 20dp
- Used on Items and Scenarios screens

### Full Edit Dialogs (Item / Scenario)

- `Dialog` with `usePlatformDefaultWidth = false`
- Surface: 92% width, RoundedCornerShape(24dp), Surface color
- Vertical padding on Surface: 24dp
- Internal Column: 24dp padding, spacedBy 16dp, verticalScroll
- Title: headlineMedium
- OutlinedTextField: focusedBorder = AccentGold, unfocused = TextSecondary 50%, cursor = AccentGold
- Category field: autocomplete via ExposedDropdownMenuBox, container = SurfaceVariant
- Rarity selector: FlowRow of FilterChips, spacedBy 8dp
  - Selected: rarityColor 15% alpha bg, 50% alpha border
  - Unselected: transparent bg, 30% alpha border
- Buttons row: Delete (AccentCoral, left) | Cancel + Save (AccentGold, right)

### SlotRow (Scenario dialog)

- Row: category dropdown (weight 1) + −/count/+ controls (36dp buttons) + delete icon (18dp, AccentCoral)
- Count display: titleMedium, AccentGold, 28dp width, centered

### AlertDialog (confirmations, warnings)

- containerColor: Surface
- Title: TextPrimary
- Text: TextSecondary
- Confirm: AccentGold for neutral, AccentCoral for delete
- Dismiss: default (Cancel)

### TimePickerDialog

- AlertDialog wrapper around Material3 TimePicker
- containerColor: Surface
- Buttons: OK (AccentGold), Cancel (default)

## Screen Layouts

### Welcome

```
Column(fillMaxSize, bg=Background, padding: h=24, verticalScroll)
  Spacer(48)
  Title: headlineLarge, AccentGold
  Spacer(8)
  Tagline: bodyLarge, TextSecondary
  Spacer(32)
  ConceptCard × 4 (Items, Scenarios, Roll, Settings), spacedBy 16
  Spacer(weight=1)
  Spacer(24)
  Button "Get started" (fillMaxWidth, h=56, gold, 16dp corners)
  Spacer(32)
```

### Roll

```
Column(fillMaxSize, bg=Background, padding: h=20, centerH)
  Spacer(24)
  Title: "Today's Roll", headlineLarge
  Spacer(8)
  Subtitle: reroll count (AccentTeal) or prompt (bodyMedium)
  ScenarioDropdown (if scenarios exist), top margin 16
  Spacer(24)
  Column(weight=1, verticalScroll, spacedBy=16)
    AnimatedRollCard × N (keyed by rollGeneration + index)
  Spacer(16)
  Button "Roll the day" / "Reroll all" (fillMaxWidth, h=56, gold, 16dp corners)
  Spacer(24)
AlertDialog: insufficient items warning (if triggered)
```

### Items

```
Box(fillMaxSize, bg=Background)
  Column
    Spacer(24)
    Row(padding: h=20)
      Title: "Items", headlineLarge, weight=1
      Fold/unfold all IconButton (AccentGold)
      Sort TextButton: "A-Z" / "Rarity" (AccentGold, labelSmall)
    Spacer(8)
    LazyColumn(contentPadding: h=20 v=8)
      per category:
        CategoryHeader + Spacer(8)
        LibraryItemRow items (spacedBy 8), key=id
        Spacer(12)
  FAB(BottomEnd, padding=20)
ItemEditDialog (when editingItem != null)
AlertDialog: delete confirmation
```

### Scenarios

```
Box(fillMaxSize, bg=Background)
  Column
    Spacer(24)
    Title: "Scenarios", headlineLarge, padding: h=20
    Spacer(8)
    LazyColumn(contentPadding: h=20 v=8)
      ScenarioCard items (spacedBy 16), key=id
  FAB(BottomEnd, padding=20)
ScenarioEditDialog (when editingScenario != null)
AlertDialog: delete confirmation
```

### Settings

```
Column(fillMaxSize, bg=Background, padding: h=20, verticalScroll)
  Spacer(24)
  Title: "Settings", headlineLarge
  SectionHeader "General"
    SettingsRow: "Allow rerolls" + Switch
    (if rerolls on):
      Spacer(12)
      SettingsRow: "Rerolls per day" + −/count/+ (∞ when 0)
      Spacer(12)
      SettingsRow: "Allow partial rerolls" + Switch
    Spacer(12)
    SettingsRow: "Enable animations" + Switch
    Spacer(12)
    SettingsRow: "Daily reminder" + Switch
    (if notifications on):
      Spacer(12)
      SettingsRow: "Reminder time" + Change TextButton
  SectionHeader "Weights"
    Rarity weight rows × 4 (12dp corners, Surface bg, padding: h=16 v=4)
      Optional reset button + rarity name (rarityColor) + −/value/+ controls
      spacedBy 8
  SectionHeader "Data"
    Row: "Export" + "Export JSON" TextButton (AccentGold)
    Spacer(8)
    Row: "Import" + "Import JSON" TextButton (AccentGold)
    Spacer(8)
    Row: "Statistics" + "Reset all" TextButton (AccentCoral)
  Spacer(24)
TimePickerDialog (when showTimePicker)
AlertDialog: import/export result message
```

## Label Casing

| Context | Casing | Examples |
|---------|--------|---------|
| Page titles | Sentence case | "Today's Roll", "Items", "Settings" |
| Section headers | Sentence case | "General", "Weights", "Data" |
| Category labels | UPPERCASE | "FOOD", "ACTIVITY" |
| Rarity names | UPPERCASE | "COMMON", "RARE" (enum `.name`) |
| Sort options | Sentence / mixed | "A-Z", "Rarity" |
| Dialog titles | Sentence case | "Add Item", "Edit Scenario" |
| Dialog buttons | Sentence case | "Save", "Cancel", "Delete" |
| Settings items | Sentence case | "Allow rerolls", "Daily reminder" |
| Buttons | Sentence case | "Get started", "Roll the day", "Reroll all" |
| Nav bar labels | Sentence case | "Roll", "Items", "Scenarios", "Settings" |
| Slot count | Symbol prefix | "×2", "×3" |

## Icons

All from `androidx.compose.material.icons`:

| Icon | Size | Usage |
|------|------|-------|
| Icons.Filled.Star | default | Roll nav tab |
| Icons.Filled.List | default | Items nav tab |
| Icons.Filled.PlayArrow | default | Scenarios nav tab |
| Icons.Filled.Settings | default | Settings nav tab |
| Icons.Filled.Add | default | FAB on Items / Scenarios |
| Icons.Filled.Check | 20dp | Complete action on roll card |
| Icons.Filled.Close | 20dp | Undo complete, remove slot |
| Icons.Filled.Refresh | 20dp | Reroll action on roll card |
| Icons.Filled.ArrowDropDown | 20dp | Scenario dropdown indicator |
| Icons.Default.KeyboardArrowDown | default | Category expand, unfold all |
| Icons.Default.KeyboardArrowRight | default | Category collapse, fold all |

## Common Patterns

| Pattern | Value |
|---------|-------|
| Screen horizontal padding | 20dp (Roll, Items, Scenarios, Settings) |
| Welcome horizontal padding | 24dp |
| Screen top spacing | 24dp (all main screens), 48dp (Welcome) |
| Section spacing | 24dp (SectionHeader top spacer) |
| Item spacing | 8dp (between list items) |
| Card spacing | 16dp (between roll cards, scenario cards, concept cards) |
| Dialog width | 92% of screen |
| Dialog corner radius | 24dp |
| Dialog internal padding | 24dp |
| Dialog field spacing | 16dp (spacedBy) |
| Card internal padding | 20dp (RollCard, ScenarioCard, ConceptCard) |
| Row internal padding | h=16dp, v=14dp (SettingsRow, weight rows, data rows), h=16dp v=12dp (LibraryItemRow) |
| Button height | 56dp (primary actions) |
| Button corner radius | 16dp |
| IconButton size | 36dp (card actions), 32dp (counters, slot controls) |
| Icon size | 20dp (action icons), 18dp (category icons, slot delete) |
| FAB edge padding | 20dp |
| AlertDialog container | Surface |
| Gradient border | 1dp, linearGradient accentColor 60%→10% or 40%→5% |
| Glow inset | 20dp from each edge |
| Glow alpha | glowLevel × 0.12 per edge |
