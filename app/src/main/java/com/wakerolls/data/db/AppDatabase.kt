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
