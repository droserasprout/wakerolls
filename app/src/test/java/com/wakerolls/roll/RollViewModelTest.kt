package com.wakerolls.roll

import com.wakerolls.data.repository.ItemRepository
import com.wakerolls.data.repository.ScenarioRepository
import com.wakerolls.domain.model.Category
import com.wakerolls.domain.model.Item
import com.wakerolls.domain.model.Rarity
import com.wakerolls.domain.model.Scenario
import com.wakerolls.domain.model.ScenarioSlot
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
    private val itemRepository = mockk<ItemRepository>()
    private val scenarioRepository = mockk<ScenarioRepository>()

    private val breakfast = Item(1L, "Eggs", Category.BREAKFAST, Rarity.COMMON)
    private val porridge = Item(3L, "Porridge", Category.BREAKFAST, Rarity.UNCOMMON)
    private val activity = Item(2L, "Walk", Category.ACTIVITY, Rarity.COMMON)

    private val dayScenario = Scenario(
        id = 1L,
        name = "Day",
        slots = listOf(
            ScenarioSlot(category = Category.BREAKFAST, count = 1),
            ScenarioSlot(category = Category.ACTIVITY, count = 1),
        ),
    )

    private val bigScenario = Scenario(
        id = 2L,
        name = "Big Day",
        slots = listOf(
            ScenarioSlot(category = Category.BREAKFAST, count = 2),
            ScenarioSlot(category = Category.ACTIVITY, count = 1),
        ),
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { itemRepository.observeEnabled(Category.BREAKFAST) } returns flowOf(listOf(breakfast, porridge))
        every { itemRepository.observeEnabled(Category.ACTIVITY) } returns flowOf(listOf(activity))
        every { scenarioRepository.observeAll() } returns flowOf(listOf(dayScenario, bigScenario))
    }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun `initial state auto-selects first scenario`() = runTest {
        val vm = RollViewModel(itemRepository, scenarioRepository)
        assertEquals(1L, vm.uiState.value.selectedScenarioId)
        assertEquals(2, vm.uiState.value.scenarios.size)
        assertTrue(vm.uiState.value.results.isEmpty())
    }

    @Test
    fun `rollAll produces results matching scenario slots`() = runTest {
        val vm = RollViewModel(itemRepository, scenarioRepository)
        vm.rollAll()
        val results = vm.uiState.value.results
        assertEquals(2, results.size) // 1 breakfast + 1 activity
        assertEquals(Category.BREAKFAST, results[0].category)
        assertEquals(Category.ACTIVITY, results[1].category)
        assertNotNull(results[0].item)
        assertNotNull(results[1].item)
    }

    @Test
    fun `rollAll with count 2 produces numbered results`() = runTest {
        val vm = RollViewModel(itemRepository, scenarioRepository)
        vm.selectScenario(2L) // Big Day: 2 breakfast + 1 activity
        vm.rollAll()
        val results = vm.uiState.value.results
        assertEquals(3, results.size) // 2 breakfast + 1 activity
        assertEquals("Breakfast #1", results[0].label)
        assertEquals("Breakfast #2", results[1].label)
        assertEquals("Activity", results[2].label)
        // Breakfast picks should be different items (without replacement)
        assertNotEquals(results[0].item?.id, results[1].item?.id)
    }

    @Test
    fun `reroll replaces only targeted result`() = runTest {
        val vm = RollViewModel(itemRepository, scenarioRepository)
        vm.rollAll()
        val original = vm.uiState.value.results.toList()
        vm.reroll(0) // reroll first result
        val updated = vm.uiState.value.results
        // Second result unchanged
        assertEquals(original[1].item, updated[1].item)
    }

    @Test
    fun `selectScenario clears results`() = runTest {
        val vm = RollViewModel(itemRepository, scenarioRepository)
        vm.rollAll()
        assertTrue(vm.uiState.value.results.isNotEmpty())
        vm.selectScenario(2L)
        assertTrue(vm.uiState.value.results.isEmpty())
        assertEquals(2L, vm.uiState.value.selectedScenarioId)
    }
}
