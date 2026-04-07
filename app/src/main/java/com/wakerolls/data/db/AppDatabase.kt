package com.wakerolls.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.wakerolls.data.db.dao.ItemDao
import com.wakerolls.data.db.dao.ScenarioDao
import com.wakerolls.data.db.entity.Converters
import com.wakerolls.data.db.entity.ItemEntity
import com.wakerolls.data.db.entity.ScenarioEntity
import com.wakerolls.data.db.entity.ScenarioSlotEntity

@Database(entities = [ItemEntity::class, ScenarioEntity::class, ScenarioSlotEntity::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao
    abstract fun scenarioDao(): ScenarioDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `scenarios` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `scenario_slots` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `scenarioId` INTEGER NOT NULL, `category` TEXT NOT NULL, `count` INTEGER NOT NULL, FOREIGN KEY(`scenarioId`) REFERENCES `scenarios`(`id`) ON DELETE CASCADE)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_scenario_slots_scenarioId` ON `scenario_slots` (`scenarioId`)")
            }
        }
    }
}
