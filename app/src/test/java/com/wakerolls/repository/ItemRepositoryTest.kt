package com.wakerolls.repository

import com.wakerolls.data.db.dao.ItemDao
import com.wakerolls.data.db.entity.ItemEntity
import com.wakerolls.data.repository.ItemRepository
import com.wakerolls.domain.model.Item
import com.wakerolls.domain.model.Rarity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import app.cash.turbine.test
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.Assert.*

class ItemRepositoryTest {

    private val dao = mockk<ItemDao>()
    private val repo = ItemRepository(dao)

    private val entity = ItemEntity(
        id = 1L,
        name = "Eggs",
        category = "Breakfast",
        rarity = Rarity.COMMON,
        enabled = true,
        rolledCount = 0,
        completedCount = 0,
    )

    @Test
    fun `observeAll emits mapped domain items`() = runTest {
        every { dao.observeAll() } returns flowOf(listOf(entity))

        repo.observeAll().test {
            val items = awaitItem()
            assertEquals(1, items.size)
            assertEquals("Eggs", items[0].name)
            assertEquals("Breakfast", items[0].category)
            awaitComplete()
        }
    }

    @Test
    fun `save calls dao insert`() = runTest {
        coEvery { dao.insert(any()) } returns 1L

        val item = Item(name = "Eggs", category = "Breakfast", rarity = Rarity.COMMON)
        repo.save(item)

        coVerify { dao.insert(ItemEntity.fromDomain(item)) }
    }

    @Test
    fun `update calls dao update`() = runTest {
        coEvery { dao.update(any()) } returns Unit

        val item = Item(id = 1L, name = "Eggs", category = "Breakfast", rarity = Rarity.COMMON)
        repo.update(item)

        coVerify { dao.update(ItemEntity.fromDomain(item)) }
    }
}
