package com.wakerolls.di

import com.wakerolls.data.repository.ItemRepository
import com.wakerolls.data.repository.ScenarioRepository
import com.wakerolls.domain.model.Category
import com.wakerolls.domain.model.Item
import com.wakerolls.domain.model.Rarity
import com.wakerolls.domain.model.Scenario
import com.wakerolls.domain.model.ScenarioSlot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataSeeder @Inject constructor(
    private val repository: ItemRepository,
    private val scenarioRepository: ScenarioRepository,
    private val scope: CoroutineScope,
) {
    private val defaultItems = listOf(
        Item(name = "Eggs", category = Category.BREAKFAST, rarity = Rarity.COMMON),
        Item(name = "Porridge", category = Category.BREAKFAST, rarity = Rarity.UNCOMMON),
        Item(name = "Walk", category = Category.ACTIVITY, rarity = Rarity.COMMON),
        Item(name = "Run", category = Category.ACTIVITY, rarity = Rarity.RARE),
    )

    private val defaultScenarios = listOf(
        Scenario(
            name = "Day",
            slots = listOf(
                ScenarioSlot(category = Category.BREAKFAST, count = 1),
                ScenarioSlot(category = Category.ACTIVITY, count = 1),
            ),
        ),
    )

    fun seedIfNeeded() {
        scope.launch { repository.seedIfEmpty(defaultItems) }
        scope.launch { scenarioRepository.seedIfEmpty(defaultScenarios) }
    }
}
