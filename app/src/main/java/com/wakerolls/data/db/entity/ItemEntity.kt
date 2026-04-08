package com.wakerolls.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.wakerolls.domain.model.Item
import com.wakerolls.domain.model.Rarity

@Entity(tableName = "items")
data class ItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String,
    val rarity: Rarity,
    val enabled: Boolean = true,
    val rolledCount: Int = 0,
    val completedCount: Int = 0,
) {
    fun toDomain() = Item(id = id, name = name, category = category, rarity = rarity, enabled = enabled, rolledCount = rolledCount, completedCount = completedCount)

    companion object {
        fun fromDomain(item: Item) = ItemEntity(
            id = item.id,
            name = item.name,
            category = item.category,
            rarity = item.rarity,
            enabled = item.enabled,
            rolledCount = item.rolledCount,
            completedCount = item.completedCount,
        )
    }
}
