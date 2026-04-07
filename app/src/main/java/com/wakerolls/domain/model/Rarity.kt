package com.wakerolls.domain.model

enum class Rarity(val weight: Int) {
    COMMON(6),
    UNCOMMON(3),
    RARE(1),
    LEGENDARY(1);

    companion object {
        fun <T> weightedRandom(
            items: List<T>,
            customWeights: Map<Rarity, Int> = emptyMap(),
            rarityOf: (T) -> Rarity,
        ): T {
            val totalWeight = items.sumOf { customWeights[rarityOf(it)] ?: rarityOf(it).weight }
            if (totalWeight <= 0) return items.random()
            var random = (1..totalWeight).random()
            for (item in items) {
                random -= customWeights[rarityOf(item)] ?: rarityOf(item).weight
                if (random <= 0) return item
            }
            return items.last()
        }
    }
}
