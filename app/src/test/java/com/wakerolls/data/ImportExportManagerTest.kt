package com.wakerolls.data

import com.wakerolls.data.repository.ItemRepository
import com.wakerolls.data.repository.ScenarioRepository
import com.wakerolls.domain.model.Item
import com.wakerolls.domain.model.Rarity
import com.wakerolls.domain.model.Scenario
import com.wakerolls.domain.model.ScenarioSlot
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.json.JSONObject
import org.junit.Test
import org.junit.Assert.*

class ImportExportManagerTest {

    private val itemRepository = mockk<ItemRepository>(relaxUnitFun = true)
    private val scenarioRepository = mockk<ScenarioRepository>(relaxUnitFun = true)
    private val manager = ImportExportManager(itemRepository, scenarioRepository)

    @Test
    fun `export produces valid JSON with items and scenarios`() = runTest {
        coEvery { itemRepository.getAll() } returns listOf(
            Item(1L, "Eggs", "Breakfast", Rarity.COMMON, enabled = true, rolledCount = 5, completedCount = 2),
        )
        coEvery { scenarioRepository.getAll() } returns listOf(
            Scenario(1L, "Day", listOf(ScenarioSlot(1L, "Breakfast", 2))),
        )

        val json = manager.exportToJson()
        val root = JSONObject(json)

        assertTrue(root.has("items"))
        assertTrue(root.has("scenarios"))

        val items = root.getJSONArray("items")
        assertEquals(1, items.length())
        val item = items.getJSONObject(0)
        assertEquals("Eggs", item.getString("name"))
        assertEquals("Breakfast", item.getString("category"))
        assertEquals("COMMON", item.getString("rarity"))
        assertEquals(true, item.getBoolean("enabled"))
        assertEquals(5, item.getInt("rolledCount"))
        assertEquals(2, item.getInt("completedCount"))

        val scenarios = root.getJSONArray("scenarios")
        assertEquals(1, scenarios.length())
        val scenario = scenarios.getJSONObject(0)
        assertEquals("Day", scenario.getString("name"))
        val slots = scenario.getJSONArray("slots")
        assertEquals(1, slots.length())
        assertEquals("Breakfast", slots.getJSONObject(0).getString("category"))
        assertEquals(2, slots.getJSONObject(0).getInt("count"))
    }

    @Test
    fun `import from valid JSON creates items and scenarios`() = runTest {
        coEvery { scenarioRepository.save(any()) } returns 1L
        val json = """
        {
            "items": [
                {"name": "Eggs", "category": "Breakfast", "rarity": "COMMON"}
            ],
            "scenarios": [
                {"name": "Day", "slots": [{"category": "Breakfast", "count": 1}]}
            ]
        }
        """.trimIndent()

        manager.importFromJson(json)

        val itemsSlot = slot<List<Item>>()
        coVerify { itemRepository.deleteAll() }
        coVerify { itemRepository.insertAll(capture(itemsSlot)) }
        assertEquals(1, itemsSlot.captured.size)
        assertEquals("Eggs", itemsSlot.captured[0].name)
        assertEquals(Rarity.COMMON, itemsSlot.captured[0].rarity)

        coVerify { scenarioRepository.deleteAll() }
        coVerify { scenarioRepository.save(any()) }
    }

    @Test
    fun `import with missing fields uses defaults`() = runTest {
        val json = """
        {
            "items": [
                {"name": "Test", "category": "Cat"}
            ]
        }
        """.trimIndent()

        manager.importFromJson(json)

        val itemsSlot = slot<List<Item>>()
        coVerify { itemRepository.insertAll(capture(itemsSlot)) }
        val item = itemsSlot.captured[0]
        assertEquals(Rarity.COMMON, item.rarity) // missing rarity defaults to COMMON
        assertTrue(item.enabled) // missing enabled defaults to true
        assertEquals(0, item.rolledCount)
        assertEquals(0, item.completedCount)
    }

    @Test
    fun `import with unknown rarity falls back to COMMON`() = runTest {
        val json = """
        {
            "items": [
                {"name": "Test", "category": "Cat", "rarity": "MYTHICAL"}
            ]
        }
        """.trimIndent()

        manager.importFromJson(json)

        val itemsSlot = slot<List<Item>>()
        coVerify { itemRepository.insertAll(capture(itemsSlot)) }
        assertEquals(Rarity.COMMON, itemsSlot.captured[0].rarity)
    }

    @Test
    fun `import items only does not touch scenarios`() = runTest {
        val json = """{"items": [{"name": "A", "category": "B", "rarity": "RARE"}]}"""

        manager.importFromJson(json)

        coVerify { itemRepository.deleteAll() }
        coVerify { itemRepository.insertAll(any()) }
        coVerify(exactly = 0) { scenarioRepository.deleteAll() }
    }
}
