package com.wakerolls.data.repository

import com.wakerolls.data.db.dao.ItemDao
import com.wakerolls.data.db.entity.ItemEntity
import com.wakerolls.domain.model.Item
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ItemRepository @Inject constructor(private val dao: ItemDao) {

    fun observeAll(): Flow<List<Item>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    fun observeEnabled(category: String): Flow<List<Item>> =
        dao.observeEnabled(category).map { list -> list.map { it.toDomain() } }

    suspend fun getByIds(ids: List<Long>): Map<Long, Item> =
        dao.getByIds(ids).associate { it.toDomain().let { item -> item.id to item } }

    fun observeCategories(): Flow<List<String>> = dao.observeCategories()

    suspend fun save(item: Item) { dao.insert(ItemEntity.fromDomain(item)) }

    suspend fun update(item: Item) { dao.update(ItemEntity.fromDomain(item)) }

    suspend fun getAll(): List<Item> = dao.getAll().map { it.toDomain() }

    suspend fun delete(item: Item) { dao.delete(ItemEntity.fromDomain(item)) }

    suspend fun deleteAll() { dao.deleteAll() }

    suspend fun insertAll(items: List<Item>) { dao.insertAll(items.map { ItemEntity.fromDomain(it) }) }

    suspend fun seedIfEmpty(items: List<Item>) {
        if (dao.count() == 0) {
            dao.insertAll(items.map { ItemEntity.fromDomain(it) })
        }
    }
}
