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
