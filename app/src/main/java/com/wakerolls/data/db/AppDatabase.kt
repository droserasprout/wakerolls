package com.wakerolls.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.wakerolls.data.db.dao.ItemDao
import com.wakerolls.data.db.dao.ScenarioDao
import com.wakerolls.data.db.entity.Converters
import com.wakerolls.data.db.entity.ItemEntity
import com.wakerolls.data.db.entity.ScenarioEntity
import com.wakerolls.data.db.entity.ScenarioSlotEntity

@Database(entities = [ItemEntity::class, ScenarioEntity::class, ScenarioSlotEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao
    abstract fun scenarioDao(): ScenarioDao
}
