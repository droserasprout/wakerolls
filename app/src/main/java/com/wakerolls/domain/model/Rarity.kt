package com.wakerolls.domain.model

enum class Rarity(val weight: Int) {
    COMMON(6),
    UNCOMMON(3),
    RARE(1),
    LEGENDARY(1);

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
