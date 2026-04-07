package com.wakerolls.roll

import app.cash.turbine.test
import com.wakerolls.data.repository.ItemRepository
import com.wakerolls.domain.model.Category
import com.wakerolls.domain.model.Item
import com.wakerolls.domain.model.Rarity
import com.wakerolls.ui.roll.RollViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class RollViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val repository = mockk<ItemRepository>()

    private val breakfast = Item(1L, "Eggs", Category.BREAKFAST, Rarity.COMMON)
    private val activity = Item(2L, "Walk", Category.ACTIVITY, Rarity.COMMON)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { repository.observeEnabled(Category.BREAKFAST) } returns flowOf(listOf(breakfast))
        every { repository.observeEnabled(Category.ACTIVITY) } returns flowOf(listOf(activity))
    }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun `initial state has no rolled items`() = runTest {
        val vm = RollViewModel(repository)
        assertNull(vm.uiState.value.breakfast)
        assertNull(vm.uiState.value.activity)
    }

    @Test
    fun `rollAll picks one item per category`() = runTest {
        val vm = RollViewModel(repository)
        vm.rollAll()
        assertEquals("Eggs", vm.uiState.value.breakfast?.name)
        assertEquals("Walk", vm.uiState.value.activity?.name)
    }

    @Test
    fun `reroll replaces only that category`() = runTest {
        val vm = RollViewModel(repository)
        vm.rollAll()
        val originalActivity = vm.uiState.value.activity
        vm.reroll(Category.BREAKFAST)
        assertEquals(originalActivity, vm.uiState.value.activity)
        assertEquals("Eggs", vm.uiState.value.breakfast?.name)
    }
}
