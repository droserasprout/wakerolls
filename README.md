# Wakerolls

Android app that randomly rolls your day — meals, activities, workouts, anything — from a customizable library with rarity-weighted selection.

## Features

- **Items** with categories and four rarity tiers (Common, Uncommon, Rare, Legendary)
- **Scenarios** that define what to roll (e.g. "2x Breakfast, 1x Activity")
- **Weighted random** selection with configurable rarity weights
- **Rerolls** — full or per-card, with a daily limit
- **Daily reminders** via notification
- **JSON import/export** for backup and sharing

## Build

Requires JDK 21 and Android SDK.

```
make build      # debug APK
make test       # unit tests
make install    # install on device
```
