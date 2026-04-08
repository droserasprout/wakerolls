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

    @Test(expected = IllegalArgumentException::class)
    fun `weightedRandom on empty list throws`() {
        Rarity.weightedRandom(emptyList<Pair<String, Rarity>>()) { it.second }
    }

    @Test
    fun `custom weights override defaults`() {
        val items = listOf(
            "common" to Rarity.COMMON,
            "legendary" to Rarity.LEGENDARY,
        )
        // Give legendary weight 100, common weight 0
        val weights = mapOf(Rarity.COMMON to 0, Rarity.LEGENDARY to 100)
        repeat(50) {
            val picked = Rarity.weightedRandom(items, weights) { it.second }
            assertEquals("legendary", picked.first)
        }
    }

    @Test
    fun `all zero weights falls back to random`() {
        val items = listOf("a" to Rarity.COMMON, "b" to Rarity.RARE)
        val weights = mapOf(Rarity.COMMON to 0, Rarity.RARE to 0, Rarity.UNCOMMON to 0, Rarity.LEGENDARY to 0)
        // Should not crash, picks randomly
        repeat(50) {
            val picked = Rarity.weightedRandom(items, weights) { it.second }
            assertTrue(picked.first in listOf("a", "b"))
        }
    }

    @Test
    fun `partial custom weights fall back to defaults`() {
        val items = listOf(
            "common" to Rarity.COMMON,
            "rare" to Rarity.RARE,
        )
        // Only override COMMON, RARE uses default (3)
        val weights = mapOf(Rarity.COMMON to 0)
        repeat(50) {
            val picked = Rarity.weightedRandom(items, weights) { it.second }
            assertEquals("rare", picked.first)
        }
    }
}
