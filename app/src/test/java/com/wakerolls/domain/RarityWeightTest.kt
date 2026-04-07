package com.wakerolls.domain

import com.wakerolls.domain.model.Rarity
import org.junit.Test
import org.junit.Assert.*

class RarityWeightTest {

    @Test
    fun `weighted random picks proportionally`() {
        val items = listOf(
            "common1" to Rarity.COMMON,
            "common2" to Rarity.COMMON,
            "uncommon" to Rarity.UNCOMMON,
            "rare" to Rarity.RARE,
        )
        val counts = mutableMapOf<String, Int>().withDefault { 0 }
        repeat(1000) {
            val picked = Rarity.weightedRandom(items) { it.second }
            counts[picked.first] = counts.getValue(picked.first) + 1
        }
        // commons (weight 6 each) >> uncommon (3) >> rare (1)
        val commonTotal = counts.getValue("common1") + counts.getValue("common2")
        assertTrue("commons dominate", commonTotal > counts.getValue("uncommon"))
        assertTrue("uncommon > rare", counts.getValue("uncommon") > counts.getValue("rare"))
    }

    @Test
    fun `weightedRandom on single item always picks it`() {
        val items = listOf("only" to Rarity.RARE)
        repeat(50) {
            val picked = Rarity.weightedRandom(items) { it.second }
            assertEquals("only", picked.first)
        }
    }
}
