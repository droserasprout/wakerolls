package com.wakerolls.di

import com.wakerolls.data.repository.ItemRepository
import com.wakerolls.domain.model.Category
import com.wakerolls.domain.model.Item
import com.wakerolls.domain.model.Rarity
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
