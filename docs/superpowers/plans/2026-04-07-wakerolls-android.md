# Wakerolls Android App Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a Kotlin/Jetpack Compose Android app that randomly rolls daily activities and meals from a user-defined library, with rarity-weighted selection.

**Architecture:** MVVM with ViewModel + StateFlow + Repository pattern. Room for persistence, Hilt for DI, WorkManager for daily reminders. Each screen has its own ViewModel injected via Hilt.

**Tech Stack:** Kotlin, Jetpack Compose, Room, Hilt, WorkManager, Navigation Compose, Kotlin Coroutines/Flow, MockK + Turbine for unit tests.

---

## File Map

```
wakerolls/
├── Makefile
├── build.gradle.kts                        root build script
├── settings.gradle.kts                     module settings + repo declarations
├── gradle.properties                       JVM args, Android flags
├── gradle/wrapper/
│   └── gradle-wrapper.properties           Gradle 8.7 + JDK 17 toolchain
└── app/
    ├── build.gradle.kts                    app module: deps, hilt plugin, compose flags
    └── src/
        ├── main/
        │   ├── AndroidManifest.xml
        │   └── java/com/wakerolls/
        │       ├── WakerollsApp.kt         @HiltAndroidApp Application subclass
        │       ├── MainActivity.kt         single activity; NavHost lives here
        │       ├── data/
        │       │   ├── db/
        │       │   │   ├── AppDatabase.kt  Room database; addMigrations; prepopulate
        │       │   │   ├── dao/ItemDao.kt  suspend funs + Flow queries
        │       │   │   └── entity/
        │       │   │       ├── ItemEntity.kt   @Entity: id, name, category, rarity, enabled
        │       │   │       └── Converters.kt   TypeConverter: Rarity enum ↔ String
        │       │   └── repository/
        │       │       └── ItemRepository.kt   wraps DAO; exposes Flow<List<Item>>
        │       ├── domain/
        │       │   └── model/
        │       │       ├── Item.kt         data class: id, name, category, rarity, enabled
        │       │       ├── Category.kt     enum: BREAKFAST, ACTIVITY
        │       │       └── Rarity.kt       enum: COMMON(weight=6), UNCOMMON(weight=3), RARE(weight=1)
        │       ├── di/
        │       │   └── AppModule.kt        @Module: provides AppDatabase, ItemDao, ItemRepository
        │       ├── ui/
        │       │   ├── theme/
        │       │   │   ├── Color.kt        dark palette constants
        │       │   │   ├── Type.kt         MaterialTheme typography
        │       │   │   └── Theme.kt        WakerollsTheme — always dark
        │       │   ├── navigation/
        │       │   │   └── NavGraph.kt     composable NavHost with Roll/Library/Settings routes
        │       │   ├── roll/
        │       │   │   ├── RollViewModel.kt    rollAll(), reroll(category); exposes UiState
        │       │   │   └── RollScreen.kt       cards + Roll button + bottom nav
        │       │   ├── library/
        │       │   │   ├── LibraryViewModel.kt reads all items grouped by category
        │       │   │   └── LibraryScreen.kt    grouped list; tap to toggle enabled
        │       │   └── settings/
        │       │       ├── SettingsViewModel.kt manages SharedPreferences via DataStore
        │       │       └── SettingsScreen.kt   notification time picker toggle
        │       └── worker/
        │           └── DailyRollWorker.kt  WorkManager worker; posts notification
        └── test/
            └── java/com/wakerolls/
                ├── domain/RarityWeightTest.kt
                ├── repository/ItemRepositoryTest.kt
                └── roll/RollViewModelTest.kt
```

---

## Task 1: Gradle Project Scaffold

**Files:**
- Create: `settings.gradle.kts`
- Create: `build.gradle.kts`
- Create: `gradle.properties`
- Create: `gradle/wrapper/gradle-wrapper.properties`
- Create: `Makefile`

- [ ] **Step 1: Create `settings.gradle.kts`**

```kotlin
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Wakerolls"
include(":app")
```

- [ ] **Step 2: Create root `build.gradle.kts`**

```kotlin
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
}
```

- [ ] **Step 3: Create `gradle/libs.versions.toml`** (version catalog)

```toml
[versions]
agp = "8.5.2"
kotlin = "2.0.21"
compose-bom = "2024.11.00"
hilt = "2.52"
room = "2.6.1"
lifecycle = "2.8.7"
navigation = "2.8.5"
work = "2.10.0"
datastore = "1.1.1"
ksp = "2.0.21-1.0.28"
mockk = "1.13.13"
turbine = "1.2.0"
coroutines = "1.9.0"

[libraries]
# Compose
compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "compose-bom" }
compose-ui = { group = "androidx.compose.ui", name = "ui" }
compose-ui-tooling-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
compose-material3 = { group = "androidx.compose.material3", name = "material3" }
compose-ui-tooling = { group = "androidx.compose.ui", name = "ui-tooling" }
activity-compose = { group = "androidx.activity", name = "activity-compose", version = "1.9.3" }
# Navigation
navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigation" }
# Lifecycle
lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycle" }
lifecycle-runtime-ktx = { group = "androidx.lifecycle", name = "lifecycle-runtime-ktx", version.ref = "lifecycle" }
# Hilt
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
hilt-compiler = { group = "com.google.dagger", name = "hilt-compiler", version.ref = "hilt" }
hilt-navigation-compose = { group = "androidx.hilt", name = "hilt-navigation-compose", version = "1.2.0" }
# Room
room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }
# WorkManager
work-runtime-ktx = { group = "androidx.work", name = "work-runtime-ktx", version.ref = "work" }
hilt-work = { group = "androidx.hilt", name = "hilt-work", version = "1.2.0" }
hilt-work-compiler = { group = "androidx.hilt", name = "hilt-compiler", version = "1.2.0" }
# DataStore
datastore-preferences = { group = "androidx.datastore", name = "datastore-preferences", version.ref = "datastore" }
# Coroutines
coroutines-core = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core", version.ref = "coroutines" }
coroutines-android = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-android", version.ref = "coroutines" }
# Test
junit = { group = "junit", name = "junit", version = "4.13.2" }
mockk = { group = "io.mockk", name = "mockk", version.ref = "mockk" }
turbine = { group = "app.cash.turbine", name = "turbine", version.ref = "turbine" }
coroutines-test = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-test", version.ref = "coroutines" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
```

- [ ] **Step 4: Create `gradle.properties`**

```properties
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
org.gradle.parallel=true
android.useAndroidX=true
kotlin.code.style=official
```

- [ ] **Step 5: Create `gradle/wrapper/gradle-wrapper.properties`**

```properties
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-8.7-bin.zip
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
```

- [ ] **Step 6: Download gradle wrapper**

```bash
cd /home/droserasprout/git/wakerolls
gradle wrapper --gradle-version 8.7
```

- [ ] **Step 7: Create `Makefile`**

```makefile
JAVA_HOME := /usr/lib/jvm/java-17-openjdk
ANDROID_HOME := $(HOME)/Android/Sdk
GRADLEW := JAVA_HOME=$(JAVA_HOME) ANDROID_HOME=$(ANDROID_HOME) ./gradlew

.PHONY: build install clean test lint

build:
	$(GRADLEW) assembleDebug

install:
	$(GRADLEW) installDebug

test:
	$(GRADLEW) testDebugUnitTest

lint:
	$(GRADLEW) lintDebug

clean:
	$(GRADLEW) clean
```

- [ ] **Step 8: Commit**

```bash
git add .
git commit -m "feat: gradle project scaffold"
```

---

## Task 2: App Module Build Script

**Files:**
- Create: `app/build.gradle.kts`
- Create: `app/src/main/AndroidManifest.xml`

- [ ] **Step 1: Create `app/build.gradle.kts`**

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.wakerolls"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.wakerolls"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.activity.compose)
    implementation(libs.navigation.compose)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.runtime.ktx)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    implementation(libs.work.runtime.ktx)
    implementation(libs.hilt.work)
    ksp(libs.hilt.work.compiler)

    implementation(libs.datastore.preferences)
    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)

    debugImplementation(libs.compose.ui.tooling)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(libs.coroutines.test)
}
```

- [ ] **Step 2: Create `app/src/main/AndroidManifest.xml`**

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
    <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />

    <application
        android:name=".WakerollsApp"
        android:allowBackup="true"
        android:label="Wakerolls"
        android:supportsRtl="true"
        android:theme="@style/Theme.Wakerolls">

        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:windowSoftInputMode="adjustResize">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <provider
            android:name="androidx.startup.InitializationProvider"
            android:authorities="${applicationId}.androidx-startup"
            android:exported="false"
            tools:node="merge">
            <meta-data
                android:name="androidx.work.WorkManagerInitializer"
                android:value="androidx.startup"
                tools:node="remove" />
        </provider>

    </application>
</manifest>
```

- [ ] **Step 3: Create `app/src/main/res/values/themes.xml`**

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <style name="Theme.Wakerolls" parent="android:Theme.Material.NoTitleBar" />
</resources>
```

- [ ] **Step 4: Create `app/src/main/res/values/strings.xml`**

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="app_name">Wakerolls</string>
    <string name="roll">Roll</string>
    <string name="library">Library</string>
    <string name="settings">Settings</string>
    <string name="roll_button">Roll the day</string>
    <string name="notification_channel_name">Daily Roll</string>
    <string name="notification_title">Your day is ready!</string>
    <string name="notification_text">Tap to see today\'s roll</string>
</resources>
```

- [ ] **Step 5: Verify the project compiles (no source yet)**

```bash
make build 2>&1 | tail -20
```
Expected: BUILD SUCCESSFUL (empty app compiles)

- [ ] **Step 6: Commit**

```bash
git add app/
git commit -m "feat: app module build config and manifest"
```

---

## Task 3: Domain Models

**Files:**
- Create: `app/src/main/java/com/wakerolls/domain/model/Rarity.kt`
- Create: `app/src/main/java/com/wakerolls/domain/model/Category.kt`
- Create: `app/src/main/java/com/wakerolls/domain/model/Item.kt`
- Create: `app/src/test/java/com/wakerolls/domain/RarityWeightTest.kt`

- [ ] **Step 1: Write the failing test**

Create `app/src/test/java/com/wakerolls/domain/RarityWeightTest.kt`:

```kotlin
package com.wakerolls.domain

import com.wakerolls.domain.model.Rarity
import org.junit.Test
import org.junit.Assert.*

class RarityWeightTest {

    @Test
    fun `weighted random picks proportionally`() {
        val items = listOf(
            "common1" to Rarity.COMMON,
            "common2" to Rarity.COMMON,
            "uncommon" to Rarity.UNCOMMON,
            "rare" to Rarity.RARE,
        )
        val counts = mutableMapOf<String, Int>().withDefault { 0 }
        repeat(1000) {
            val picked = Rarity.weightedRandom(items) { it.second }
            counts[picked.first] = counts.getValue(picked.first) + 1
        }
        // commons (weight 6 each) >> uncommon (3) >> rare (1)
        val commonTotal = counts.getValue("common1") + counts.getValue("common2")
        assertTrue("commons dominate", commonTotal > counts.getValue("uncommon"))
        assertTrue("uncommon > rare", counts.getValue("uncommon") > counts.getValue("rare"))
    }

    @Test
    fun `weightedRandom on single item always picks it`() {
        val items = listOf("only" to Rarity.RARE)
        repeat(50) {
            val picked = Rarity.weightedRandom(items) { it.second }
            assertEquals("only", picked.first)
        }
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
make test 2>&1 | grep -E "FAIL|error|not found|weightedRandom"
```
Expected: compilation error — `Rarity` not found

- [ ] **Step 3: Create `app/src/main/java/com/wakerolls/domain/model/Rarity.kt`**

```kotlin
package com.wakerolls.domain.model

enum class Rarity(val weight: Int) {
    COMMON(6),
    UNCOMMON(3),
    RARE(1);

    companion object {
        fun <T> weightedRandom(items: List<T>, rarityOf: (T) -> Rarity): T {
            val totalWeight = items.sumOf { rarityOf(it).weight }
            var random = (1..totalWeight).random()
            for (item in items) {
                random -= rarityOf(item).weight
                if (random <= 0) return item
            }
            return items.last()
        }
    }
}
```

- [ ] **Step 4: Create `app/src/main/java/com/wakerolls/domain/model/Category.kt`**

```kotlin
package com.wakerolls.domain.model

enum class Category(val displayName: String) {
    BREAKFAST("Breakfast"),
    ACTIVITY("Activity")
}
```

- [ ] **Step 5: Create `app/src/main/java/com/wakerolls/domain/model/Item.kt`**

```kotlin
package com.wakerolls.domain.model

data class Item(
    val id: Long = 0,
    val name: String,
    val category: Category,
    val rarity: Rarity,
    val enabled: Boolean = true,
)
```

- [ ] **Step 6: Run test to verify it passes**

```bash
make test 2>&1 | grep -E "PASS|BUILD SUCCESSFUL|RarityWeightTest"
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 7: Commit**

```bash
git add app/src/
git commit -m "feat: domain models with weighted rarity"
```

---

## Task 4: Room Database Layer

**Files:**
- Create: `app/src/main/java/com/wakerolls/data/db/entity/ItemEntity.kt`
- Create: `app/src/main/java/com/wakerolls/data/db/entity/Converters.kt`
- Create: `app/src/main/java/com/wakerolls/data/db/dao/ItemDao.kt`
- Create: `app/src/main/java/com/wakerolls/data/db/AppDatabase.kt`

- [ ] **Step 1: Create `ItemEntity.kt`**

```kotlin
package com.wakerolls.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.wakerolls.domain.model.Category
import com.wakerolls.domain.model.Item
import com.wakerolls.domain.model.Rarity

@Entity(tableName = "items")
data class ItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: Category,
    val rarity: Rarity,
    val enabled: Boolean = true,
) {
    fun toDomain() = Item(id = id, name = name, category = category, rarity = rarity, enabled = enabled)

    companion object {
        fun fromDomain(item: Item) = ItemEntity(
            id = item.id,
            name = item.name,
            category = item.category,
            rarity = item.rarity,
            enabled = item.enabled,
        )
    }
}
```

- [ ] **Step 2: Create `Converters.kt`**

```kotlin
package com.wakerolls.data.db.entity

import androidx.room.TypeConverter
import com.wakerolls.domain.model.Category
import com.wakerolls.domain.model.Rarity

class Converters {
    @TypeConverter fun fromRarity(value: Rarity): String = value.name
    @TypeConverter fun toRarity(value: String): Rarity = Rarity.valueOf(value)
    @TypeConverter fun fromCategory(value: Category): String = value.name
    @TypeConverter fun toCategory(value: String): Category = Category.valueOf(value)
}
```

- [ ] **Step 3: Create `ItemDao.kt`**

```kotlin
package com.wakerolls.data.db.dao

import androidx.room.*
import com.wakerolls.data.db.entity.ItemEntity
import com.wakerolls.domain.model.Category
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {
    @Query("SELECT * FROM items ORDER BY category, name")
    fun observeAll(): Flow<List<ItemEntity>>

    @Query("SELECT * FROM items WHERE category = :category AND enabled = 1")
    fun observeEnabled(category: Category): Flow<List<ItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ItemEntity>)

    @Update
    suspend fun update(item: ItemEntity)

    @Delete
    suspend fun delete(item: ItemEntity)

    @Query("SELECT COUNT(*) FROM items")
    suspend fun count(): Int
}
```

- [ ] **Step 4: Create `AppDatabase.kt`**

```kotlin
package com.wakerolls.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.wakerolls.data.db.dao.ItemDao
import com.wakerolls.data.db.entity.Converters
import com.wakerolls.data.db.entity.ItemEntity

@Database(entities = [ItemEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao
}
```

- [ ] **Step 5: Verify compilation**

```bash
make build 2>&1 | tail -5
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```bash
git add app/src/
git commit -m "feat: Room database layer"
```

---

## Task 5: Repository + Unit Test

**Files:**
- Create: `app/src/main/java/com/wakerolls/data/repository/ItemRepository.kt`
- Create: `app/src/test/java/com/wakerolls/repository/ItemRepositoryTest.kt`

- [ ] **Step 1: Write failing test**

Create `app/src/test/java/com/wakerolls/repository/ItemRepositoryTest.kt`:

```kotlin
package com.wakerolls.repository

import com.wakerolls.data.db.dao.ItemDao
import com.wakerolls.data.db.entity.ItemEntity
import com.wakerolls.data.repository.ItemRepository
import com.wakerolls.domain.model.Category
import com.wakerolls.domain.model.Item
import com.wakerolls.domain.model.Rarity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import app.cash.turbine.test
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.Assert.*

class ItemRepositoryTest {

    private val dao = mockk<ItemDao>()
    private val repo = ItemRepository(dao)

    private val entity = ItemEntity(
        id = 1L,
        name = "Eggs",
        category = Category.BREAKFAST,
        rarity = Rarity.COMMON,
        enabled = true,
    )

    @Test
    fun `observeAll emits mapped domain items`() = runTest {
        every { dao.observeAll() } returns flowOf(listOf(entity))

        repo.observeAll().test {
            val items = awaitItem()
            assertEquals(1, items.size)
            assertEquals("Eggs", items[0].name)
            assertEquals(Category.BREAKFAST, items[0].category)
            awaitComplete()
        }
    }

    @Test
    fun `save calls dao insert`() = runTest {
        coEvery { dao.insert(any()) } returns 1L

        val item = Item(name = "Eggs", category = Category.BREAKFAST, rarity = Rarity.COMMON)
        repo.save(item)

        coVerify { dao.insert(ItemEntity.fromDomain(item)) }
    }

    @Test
    fun `update calls dao update`() = runTest {
        coEvery { dao.update(any()) } returns Unit

        val item = Item(id = 1L, name = "Eggs", category = Category.BREAKFAST, rarity = Rarity.COMMON)
        repo.update(item)

        coVerify { dao.update(ItemEntity.fromDomain(item)) }
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
make test 2>&1 | grep -E "error|ItemRepository"
```
Expected: compilation error — `ItemRepository` not found

- [ ] **Step 3: Create `ItemRepository.kt`**

```kotlin
package com.wakerolls.data.repository

import com.wakerolls.data.db.dao.ItemDao
import com.wakerolls.data.db.entity.ItemEntity
import com.wakerolls.domain.model.Category
import com.wakerolls.domain.model.Item
import com.wakerolls.domain.model.Rarity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ItemRepository @Inject constructor(private val dao: ItemDao) {

    fun observeAll(): Flow<List<Item>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    fun observeEnabled(category: Category): Flow<List<Item>> =
        dao.observeEnabled(category).map { list -> list.map { it.toDomain() } }

    suspend fun save(item: Item) { dao.insert(ItemEntity.fromDomain(item)) }

    suspend fun update(item: Item) { dao.update(ItemEntity.fromDomain(item)) }

    suspend fun delete(item: Item) { dao.delete(ItemEntity.fromDomain(item)) }

    suspend fun seedIfEmpty(items: List<Item>) {
        if (dao.count() == 0) {
            dao.insertAll(items.map { ItemEntity.fromDomain(it) })
        }
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

```bash
make test 2>&1 | grep -E "PASS|BUILD SUCCESSFUL"
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add app/src/
git commit -m "feat: ItemRepository with Flow mapping"
```

---

## Task 6: Hilt DI + Application Class

**Files:**
- Create: `app/src/main/java/com/wakerolls/WakerollsApp.kt`
- Create: `app/src/main/java/com/wakerolls/di/AppModule.kt`

- [ ] **Step 1: Create `WakerollsApp.kt`**

```kotlin
package com.wakerolls

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class WakerollsApp : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT,
            )
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "daily_roll"
    }
}
```

- [ ] **Step 2: Create `AppModule.kt`**

```kotlin
package com.wakerolls.di

import android.content.Context
import androidx.room.Room
import com.wakerolls.data.db.AppDatabase
import com.wakerolls.data.db.dao.ItemDao
import com.wakerolls.data.repository.ItemRepository
import com.wakerolls.domain.model.Category
import com.wakerolls.domain.model.Item
import com.wakerolls.domain.model.Rarity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "wakerolls.db").build()

    @Provides
    fun provideItemDao(db: AppDatabase): ItemDao = db.itemDao()

    @Provides @Singleton
    fun provideItemRepository(dao: ItemDao): ItemRepository {
        return ItemRepository(dao)
    }

    @Provides @Singleton
    fun provideAppScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
}
```

- [ ] **Step 3: Create `SeedModule.kt`** — seeds default data on first launch

```kotlin
package com.wakerolls.di

import com.wakerolls.data.repository.ItemRepository
import com.wakerolls.domain.model.Category
import com.wakerolls.domain.model.Item
import com.wakerolls.domain.model.Rarity
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataSeeder @Inject constructor(
    private val repository: ItemRepository,
    private val scope: CoroutineScope,
) {
    private val defaultItems = listOf(
        Item(name = "Eggs", category = Category.BREAKFAST, rarity = Rarity.COMMON),
        Item(name = "Porridge", category = Category.BREAKFAST, rarity = Rarity.UNCOMMON),
        Item(name = "Walk", category = Category.ACTIVITY, rarity = Rarity.COMMON),
        Item(name = "Run", category = Category.ACTIVITY, rarity = Rarity.RARE),
    )

    fun seedIfNeeded() {
        scope.launch { repository.seedIfEmpty(defaultItems) }
    }
}
```

Update `WakerollsApp.kt` to inject and call `DataSeeder`:

```kotlin
package com.wakerolls

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.wakerolls.di.DataSeeder
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class WakerollsApp : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var dataSeeder: DataSeeder

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        dataSeeder.seedIfNeeded()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT,
            )
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "daily_roll"
    }
}
```

- [ ] **Step 4: Verify compilation**

```bash
make build 2>&1 | tail -5
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add app/src/
git commit -m "feat: Hilt DI module, app class, data seeding"
```

---

## Task 7: Theme

**Files:**
- Create: `app/src/main/java/com/wakerolls/ui/theme/Color.kt`
- Create: `app/src/main/java/com/wakerolls/ui/theme/Type.kt`
- Create: `app/src/main/java/com/wakerolls/ui/theme/Theme.kt`

- [ ] **Step 1: Create `Color.kt`**

```kotlin
package com.wakerolls.ui.theme

import androidx.compose.ui.graphics.Color

// Backgrounds
val DarkBackground = Color(0xFF0D0D0F)
val DarkSurface = Color(0xFF1A1A1F)
val DarkSurfaceVariant = Color(0xFF242429)

// Accent
val AccentGold = Color(0xFFFFD166)
val AccentTeal = Color(0xFF06D6A0)
val AccentCoral = Color(0xFFEF476F)

// Text
val TextPrimary = Color(0xFFF2F2F7)
val TextSecondary = Color(0xFF8E8E93)

// Rarity colours
val RarityCommon = Color(0xFF8E8E93)
val RarityUncommon = Color(0xFF30D158)
val RarityRare = Color(0xFFBF5AF2)
```

- [ ] **Step 2: Create `Type.kt`**

```kotlin
package com.wakerolls.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val WakerollsTypography = Typography(
    headlineLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        letterSpacing = (-0.5).sp,
        color = TextPrimary,
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        color = TextPrimary,
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        color = TextPrimary,
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        color = TextPrimary,
    ),
    bodyLarge = TextStyle(
        fontSize = 16.sp,
        color = TextPrimary,
    ),
    bodyMedium = TextStyle(
        fontSize = 14.sp,
        color = TextSecondary,
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        letterSpacing = 0.5.sp,
        color = TextSecondary,
    ),
)
```

- [ ] **Step 3: Create `Theme.kt`**

```kotlin
package com.wakerolls.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = AccentGold,
    onPrimary = DarkBackground,
    secondary = AccentTeal,
    onSecondary = DarkBackground,
    tertiary = AccentCoral,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
)

@Composable
fun WakerollsTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = WakerollsTypography,
        content = content,
    )
}
```

- [ ] **Step 4: Verify compilation**

```bash
make build 2>&1 | tail -5
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/wakerolls/ui/theme/
git commit -m "feat: custom dark theme"
```

---

## Task 8: Navigation + MainActivity

**Files:**
- Create: `app/src/main/java/com/wakerolls/ui/navigation/NavGraph.kt`
- Create: `app/src/main/java/com/wakerolls/MainActivity.kt`

- [ ] **Step 1: Create `NavGraph.kt`**

```kotlin
package com.wakerolls.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.wakerolls.ui.library.LibraryScreen
import com.wakerolls.ui.roll.RollScreen
import com.wakerolls.ui.settings.SettingsScreen

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Roll : Screen("roll", "Roll", Icons.Filled.Casino)
    object Library : Screen("library", "Library", Icons.Filled.LibraryBooks)
    object Settings : Screen("settings", "Settings", Icons.Filled.Settings)
}

val bottomNavItems = listOf(Screen.Roll, Screen.Library, Screen.Settings)

@Composable
fun WakerollsNavGraph() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = androidx.compose.ui.graphics.Color(0xFF1A1A1F),
            ) {
                bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(navController = navController, startDestination = Screen.Roll.route) {
            composable(Screen.Roll.route) { RollScreen(innerPadding) }
            composable(Screen.Library.route) { LibraryScreen(innerPadding) }
            composable(Screen.Settings.route) { SettingsScreen(innerPadding) }
        }
    }
}
```

- [ ] **Step 2: Create `MainActivity.kt`**

```kotlin
package com.wakerolls

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.wakerolls.ui.navigation.WakerollsNavGraph
import com.wakerolls.ui.theme.WakerollsTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WakerollsTheme {
                WakerollsNavGraph()
            }
        }
    }
}
```

Note: `RollScreen`, `LibraryScreen`, `SettingsScreen` are stubs — add them now as empty composables so this compiles:

Create `app/src/main/java/com/wakerolls/ui/roll/RollScreen.kt` (stub):
```kotlin
package com.wakerolls.ui.roll

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun RollScreen(padding: PaddingValues) {
    Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
        Text("Roll")
    }
}
```

Create `app/src/main/java/com/wakerolls/ui/library/LibraryScreen.kt` (stub):
```kotlin
package com.wakerolls.ui.library

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun LibraryScreen(padding: PaddingValues) {
    Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
        Text("Library")
    }
}
```

Create `app/src/main/java/com/wakerolls/ui/settings/SettingsScreen.kt` (stub):
```kotlin
package com.wakerolls.ui.settings

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun SettingsScreen(padding: PaddingValues) {
    Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
        Text("Settings")
    }
}
```

- [ ] **Step 3: Verify compilation**

```bash
make build 2>&1 | tail -5
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add app/src/
git commit -m "feat: navigation scaffold and MainActivity"
```

---

## Task 9: Roll ViewModel + Screen

**Files:**
- Create: `app/src/main/java/com/wakerolls/ui/roll/RollViewModel.kt`
- Modify: `app/src/main/java/com/wakerolls/ui/roll/RollScreen.kt`
- Create: `app/src/test/java/com/wakerolls/roll/RollViewModelTest.kt`

- [ ] **Step 1: Write failing ViewModel test**

Create `app/src/test/java/com/wakerolls/roll/RollViewModelTest.kt`:

```kotlin
package com.wakerolls.roll

import app.cash.turbine.test
import com.wakerolls.data.repository.ItemRepository
import com.wakerolls.domain.model.Category
import com.wakerolls.domain.model.Item
import com.wakerolls.domain.model.Rarity
import com.wakerolls.ui.roll.RollViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class RollViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val repository = mockk<ItemRepository>()

    private val breakfast = Item(1L, "Eggs", Category.BREAKFAST, Rarity.COMMON)
    private val activity = Item(2L, "Walk", Category.ACTIVITY, Rarity.COMMON)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { repository.observeEnabled(Category.BREAKFAST) } returns flowOf(listOf(breakfast))
        every { repository.observeEnabled(Category.ACTIVITY) } returns flowOf(listOf(activity))
    }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun `initial state has no rolled items`() = runTest {
        val vm = RollViewModel(repository)
        assertNull(vm.uiState.value.breakfast)
        assertNull(vm.uiState.value.activity)
    }

    @Test
    fun `rollAll picks one item per category`() = runTest {
        val vm = RollViewModel(repository)
        vm.rollAll()
        assertEquals("Eggs", vm.uiState.value.breakfast?.name)
        assertEquals("Walk", vm.uiState.value.activity?.name)
    }

    @Test
    fun `reroll replaces only that category`() = runTest {
        val vm = RollViewModel(repository)
        vm.rollAll()
        val originalActivity = vm.uiState.value.activity
        vm.reroll(Category.BREAKFAST)
        // activity unchanged
        assertEquals(originalActivity, vm.uiState.value.activity)
        // breakfast re-picked (only one option so same)
        assertEquals("Eggs", vm.uiState.value.breakfast?.name)
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
make test 2>&1 | grep -E "error|RollViewModel"
```
Expected: compilation error

- [ ] **Step 3: Create `RollViewModel.kt`**

```kotlin
package com.wakerolls.ui.roll

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wakerolls.data.repository.ItemRepository
import com.wakerolls.domain.model.Category
import com.wakerolls.domain.model.Item
import com.wakerolls.domain.model.Rarity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RollUiState(
    val breakfast: Item? = null,
    val activity: Item? = null,
    val isRolling: Boolean = false,
)

@HiltViewModel
class RollViewModel @Inject constructor(
    private val repository: ItemRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RollUiState())
    val uiState: StateFlow<RollUiState> = _uiState.asStateFlow()

    fun rollAll() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRolling = true)
            val breakfast = pickFrom(Category.BREAKFAST)
            val activity = pickFrom(Category.ACTIVITY)
            _uiState.value = RollUiState(breakfast = breakfast, activity = activity)
        }
    }

    fun reroll(category: Category) {
        viewModelScope.launch {
            val picked = pickFrom(category)
            _uiState.value = when (category) {
                Category.BREAKFAST -> _uiState.value.copy(breakfast = picked)
                Category.ACTIVITY -> _uiState.value.copy(activity = picked)
            }
        }
    }

    private suspend fun pickFrom(category: Category): Item? {
        val items = repository.observeEnabled(category).first()
        if (items.isEmpty()) return null
        return Rarity.weightedRandom(items) { it.rarity }
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

```bash
make test 2>&1 | grep -E "PASS|BUILD SUCCESSFUL"
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Replace stub `RollScreen.kt` with full implementation**

```kotlin
package com.wakerolls.ui.roll

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wakerolls.domain.model.Item
import com.wakerolls.domain.model.Rarity
import com.wakerolls.ui.theme.*

@Composable
fun RollScreen(padding: PaddingValues, viewModel: RollViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(padding)
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(32.dp))
        Text(
            text = "Today's Roll",
            style = MaterialTheme.typography.headlineLarge,
            color = TextPrimary,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Tap roll to discover your day",
            style = MaterialTheme.typography.bodyMedium,
        )

        Spacer(Modifier.height(40.dp))

        RollCard(label = "Breakfast", item = state.breakfast)
        Spacer(Modifier.height(16.dp))
        RollCard(label = "Activity", item = state.activity)

        Spacer(Modifier.weight(1f))

        Button(
            onClick = { viewModel.rollAll() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentGold),
            shape = RoundedCornerShape(16.dp),
        ) {
            Text(
                text = "Roll the day",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = DarkBackground,
            )
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
fun RollCard(label: String, item: Item?) {
    val rarityColor = item?.rarity?.color() ?: TextSecondary

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(DarkSurface)
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(listOf(rarityColor.copy(alpha = 0.6f), rarityColor.copy(alpha = 0.1f))),
                shape = RoundedCornerShape(20.dp),
            ),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = 2.sp,
                color = TextSecondary,
            )
            if (item != null) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary,
                )
                RarityBadge(item.rarity)
            } else {
                Text(
                    text = "—",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextSecondary,
                )
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun RarityBadge(rarity: Rarity) {
    val color = rarity.color()
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.15f),
    ) {
        Text(
            text = rarity.name,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
        )
    }
}

fun Rarity.color(): Color = when (this) {
    Rarity.COMMON -> RarityCommon
    Rarity.UNCOMMON -> RarityUncommon
    Rarity.RARE -> RarityRare
}
```

- [ ] **Step 6: Verify build**

```bash
make build 2>&1 | tail -5
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 7: Commit**

```bash
git add app/src/
git commit -m "feat: Roll screen with rarity cards"
```

---

## Task 10: Library ViewModel + Screen

**Files:**
- Create: `app/src/main/java/com/wakerolls/ui/library/LibraryViewModel.kt`
- Modify: `app/src/main/java/com/wakerolls/ui/library/LibraryScreen.kt`

- [ ] **Step 1: Create `LibraryViewModel.kt`**

```kotlin
package com.wakerolls.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wakerolls.data.repository.ItemRepository
import com.wakerolls.domain.model.Category
import com.wakerolls.domain.model.Item
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LibraryUiState(
    val grouped: Map<Category, List<Item>> = emptyMap(),
)

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val repository: ItemRepository,
) : ViewModel() {

    val uiState: StateFlow<LibraryUiState> = repository.observeAll()
        .map { items -> LibraryUiState(grouped = items.groupBy { it.category }) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LibraryUiState())

    fun toggleEnabled(item: Item) {
        viewModelScope.launch {
            repository.update(item.copy(enabled = !item.enabled))
        }
    }
}
```

- [ ] **Step 2: Replace stub `LibraryScreen.kt` with full implementation**

```kotlin
package com.wakerolls.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wakerolls.domain.model.Category
import com.wakerolls.domain.model.Item
import com.wakerolls.ui.roll.RarityBadge
import com.wakerolls.ui.roll.color
import com.wakerolls.ui.theme.*

@Composable
fun LibraryScreen(padding: PaddingValues, viewModel: LibraryViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(padding),
    ) {
        Spacer(Modifier.height(24.dp))
        Text(
            text = "Library",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(horizontal = 20.dp),
        )
        Spacer(Modifier.height(16.dp))
        LazyColumn(contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)) {
            state.grouped.forEach { (category, items) ->
                item {
                    CategoryHeader(category)
                    Spacer(Modifier.height(8.dp))
                }
                items(items, key = { it.id }) { item ->
                    LibraryItemRow(item = item, onToggle = { viewModel.toggleEnabled(item) })
                    Spacer(Modifier.height(8.dp))
                }
                item { Spacer(Modifier.height(12.dp)) }
            }
        }
    }
}

@Composable
private fun CategoryHeader(category: Category) {
    Text(
        text = category.displayName.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        letterSpacing = 2.sp,
        color = TextSecondary,
        modifier = Modifier.padding(vertical = 4.dp),
    )
}

@Composable
private fun LibraryItemRow(item: Item, onToggle: () -> Unit) {
    val rarityColor = item.rarity.color()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurface)
            .clickable { onToggle() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.titleMedium,
                color = if (item.enabled) TextPrimary else TextSecondary,
            )
            Spacer(Modifier.height(4.dp))
            RarityBadge(item.rarity)
        }
        Switch(
            checked = item.enabled,
            onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = AccentGold,
                checkedTrackColor = AccentGold.copy(alpha = 0.4f),
            ),
        )
    }
}
```

- [ ] **Step 3: Verify build**

```bash
make build 2>&1 | tail -5
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add app/src/
git commit -m "feat: Library screen with grouped items and toggle"
```

---

## Task 11: Settings ViewModel + Screen + DataStore

**Files:**
- Create: `app/src/main/java/com/wakerolls/ui/settings/SettingsViewModel.kt`
- Modify: `app/src/main/java/com/wakerolls/ui/settings/SettingsScreen.kt`
- Modify: `app/src/main/java/com/wakerolls/di/AppModule.kt`

- [ ] **Step 1: Add DataStore provision to `AppModule.kt`**

Add this import and provider to the existing `AppModule` object:

```kotlin
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

// Inside AppModule object:
@Provides @Singleton
fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> = context.dataStore
```

Full updated `AppModule.kt`:

```kotlin
package com.wakerolls.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.wakerolls.data.db.AppDatabase
import com.wakerolls.data.db.dao.ItemDao
import com.wakerolls.data.repository.ItemRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "wakerolls.db").build()

    @Provides
    fun provideItemDao(db: AppDatabase): ItemDao = db.itemDao()

    @Provides @Singleton
    fun provideItemRepository(dao: ItemDao): ItemRepository = ItemRepository(dao)

    @Provides @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> = context.dataStore

    @Provides @Singleton
    fun provideAppScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
}
```

- [ ] **Step 2: Create `SettingsViewModel.kt`**

```kotlin
package com.wakerolls.ui.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val notificationsEnabled: Boolean = false,
    val notificationHour: Int = 8,
    val notificationMinute: Int = 0,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : ViewModel() {

    companion object {
        val KEY_NOTIF_ENABLED = booleanPreferencesKey("notif_enabled")
        val KEY_NOTIF_HOUR = intPreferencesKey("notif_hour")
        val KEY_NOTIF_MINUTE = intPreferencesKey("notif_minute")
    }

    val uiState: StateFlow<SettingsUiState> = dataStore.data
        .map { prefs ->
            SettingsUiState(
                notificationsEnabled = prefs[KEY_NOTIF_ENABLED] ?: false,
                notificationHour = prefs[KEY_NOTIF_HOUR] ?: 8,
                notificationMinute = prefs[KEY_NOTIF_MINUTE] ?: 0,
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.edit { it[KEY_NOTIF_ENABLED] = enabled }
        }
    }

    fun setNotificationTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            dataStore.edit {
                it[KEY_NOTIF_HOUR] = hour
                it[KEY_NOTIF_MINUTE] = minute
            }
        }
    }
}
```

- [ ] **Step 3: Replace stub `SettingsScreen.kt` with full implementation**

```kotlin
package com.wakerolls.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wakerolls.ui.theme.*

@Composable
fun SettingsScreen(padding: PaddingValues, viewModel: SettingsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showTimePicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(padding)
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(24.dp))
        Text("Settings", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(24.dp))

        // Notification toggle row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(DarkSurface)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text("Daily reminder", style = MaterialTheme.typography.titleMedium)
                Text("Get notified to roll your day", style = MaterialTheme.typography.bodyMedium)
            }
            Switch(
                checked = state.notificationsEnabled,
                onCheckedChange = { viewModel.setNotificationsEnabled(it) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = AccentGold,
                    checkedTrackColor = AccentGold.copy(alpha = 0.4f),
                ),
            )
        }

        if (state.notificationsEnabled) {
            Spacer(Modifier.height(12.dp))
            // Time picker row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurface)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Reminder time", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "%02d:%02d".format(state.notificationHour, state.notificationMinute),
                        style = MaterialTheme.typography.bodyMedium,
                        color = AccentGold,
                    )
                }
                TextButton(onClick = { showTimePicker = true }) {
                    Text("Change", color = AccentGold)
                }
            }
        }

        if (showTimePicker) {
            TimePickerDialog(
                initialHour = state.notificationHour,
                initialMinute = state.notificationMinute,
                onConfirm = { h, m ->
                    viewModel.setNotificationTime(h, m)
                    showTimePicker = false
                },
                onDismiss = { showTimePicker = false },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onConfirm: (Int, Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val state = rememberTimePickerState(initialHour = initialHour, initialMinute = initialMinute)
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onConfirm(state.hour, state.minute) }) {
                Text("OK", color = AccentGold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        text = { TimePicker(state = state) },
        containerColor = DarkSurface,
    )
}
```

- [ ] **Step 4: Verify build**

```bash
make build 2>&1 | tail -5
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add app/src/
git commit -m "feat: Settings screen with DataStore notifications toggle"
```

---

## Task 12: WorkManager Daily Roll Notification

**Files:**
- Create: `app/src/main/java/com/wakerolls/worker/DailyRollWorker.kt`
- Modify: `app/src/main/java/com/wakerolls/ui/settings/SettingsViewModel.kt`

- [ ] **Step 1: Create `DailyRollWorker.kt`**

```kotlin
package com.wakerolls.worker

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.wakerolls.MainActivity
import com.wakerolls.R
import com.wakerolls.WakerollsApp
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class DailyRollWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(applicationContext, WakerollsApp.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_today)
            .setContentTitle(applicationContext.getString(R.string.notification_title))
            .setContentText(applicationContext.getString(R.string.notification_text))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        applicationContext.getSystemService(NotificationManager::class.java)
            .notify(1, notification)

        return Result.success()
    }

    companion object {
        const val WORK_NAME = "daily_roll_notification"

        fun scheduleDaily(context: Context, hour: Int, minute: Int) {
            val now = java.util.Calendar.getInstance()
            val target = java.util.Calendar.getInstance().apply {
                set(java.util.Calendar.HOUR_OF_DAY, hour)
                set(java.util.Calendar.MINUTE, minute)
                set(java.util.Calendar.SECOND, 0)
                if (before(now)) add(java.util.Calendar.DAY_OF_YEAR, 1)
            }
            val delay = target.timeInMillis - now.timeInMillis

            val request = OneTimeWorkRequestBuilder<DailyRollWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                request,
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
```

- [ ] **Step 2: Update `SettingsViewModel.kt` to schedule/cancel worker on toggle**

Replace the existing `setNotificationsEnabled` and `setNotificationTime` methods and add context injection:

```kotlin
package com.wakerolls.ui.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wakerolls.worker.DailyRollWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val notificationsEnabled: Boolean = false,
    val notificationHour: Int = 8,
    val notificationMinute: Int = 0,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    companion object {
        val KEY_NOTIF_ENABLED = booleanPreferencesKey("notif_enabled")
        val KEY_NOTIF_HOUR = intPreferencesKey("notif_hour")
        val KEY_NOTIF_MINUTE = intPreferencesKey("notif_minute")
    }

    val uiState: StateFlow<SettingsUiState> = dataStore.data
        .map { prefs ->
            SettingsUiState(
                notificationsEnabled = prefs[KEY_NOTIF_ENABLED] ?: false,
                notificationHour = prefs[KEY_NOTIF_HOUR] ?: 8,
                notificationMinute = prefs[KEY_NOTIF_MINUTE] ?: 0,
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.edit { it[KEY_NOTIF_ENABLED] = enabled }
            val current = uiState.value
            if (enabled) {
                DailyRollWorker.scheduleDaily(context, current.notificationHour, current.notificationMinute)
            } else {
                DailyRollWorker.cancel(context)
            }
        }
    }

    fun setNotificationTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            dataStore.edit {
                it[KEY_NOTIF_HOUR] = hour
                it[KEY_NOTIF_MINUTE] = minute
            }
            if (uiState.value.notificationsEnabled) {
                DailyRollWorker.scheduleDaily(context, hour, minute)
            }
        }
    }
}
```

- [ ] **Step 3: Verify build**

```bash
make build 2>&1 | tail -5
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Run all tests**

```bash
make test 2>&1 | tail -20
```
Expected: BUILD SUCCESSFUL, all tests pass

- [ ] **Step 5: Commit**

```bash
git add app/src/
git commit -m "feat: WorkManager daily roll notification"
```

---

## Task 13: Install & Smoke Test

- [ ] **Step 1: Connect device or start emulator, then install**

```bash
make install 2>&1 | tail -10
```
Expected: `BUILD SUCCESSFUL` and app installed

- [ ] **Step 2: Verify app launches**

Open app on device. Confirm:
- Dark background, bottom nav shows Roll / Library / Settings
- Roll screen shows "Today's Roll" heading and "Roll the day" button
- Tapping "Roll the day" shows Breakfast and Activity cards with names and rarity badges
- Library screen shows two categories (Breakfast, Activity) with items and toggle switches
- Settings screen shows daily reminder toggle

- [ ] **Step 3: Final commit**

```bash
git add .
git commit -m "feat: complete Wakerolls app v1.0"
```

---

## Self-Review Checklist

### Spec Coverage

| Requirement | Task |
|---|---|
| Android app "Wakerolls" | Task 1-2 |
| Random activities/meals | Task 9 (RollViewModel.rollAll) |
| Screens: Roll, Library, Settings | Tasks 8-11 |
| Library: breakfast (eggs, porridge), activity (walk, run) | Task 6 (DataSeeder) |
| Rarity field on items | Task 3 (Rarity enum) |
| Roll screen: cards | Task 9 (RollCard composable) |
| Kotlin | All tasks |
| Jetpack Compose + dark theme | Tasks 7-11 |
| MVVM + ViewModel + StateFlow + Repository | Tasks 5, 9, 10, 11 |
| Room (SQLite) | Task 4 |
| Hilt | Tasks 6, 9, 10, 11 |
| WorkManager | Task 12 |
| Min SDK 26 | Task 2 |
| JDK 17 | Task 1 (Makefile, build.gradle JAVA_HOME) |
| Android SDK at ~/Android/Sdk | Task 1 (Makefile) |
| `make install` | Task 1 (Makefile) |

All requirements covered. No gaps found.
