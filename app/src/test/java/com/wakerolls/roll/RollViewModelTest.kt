package com.wakerolls.roll

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.wakerolls.data.repository.ItemRepository
import com.wakerolls.data.repository.ScenarioRepository
import com.wakerolls.domain.model.Item
import com.wakerolls.domain.model.Rarity
import com.wakerolls.domain.model.Scenario
import com.wakerolls.domain.model.ScenarioSlot
import com.wakerolls.ui.roll.RollViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*
import org.junit.rules.TemporaryFolder

@OptIn(ExperimentalCoroutinesApi::class)
class RollViewModelTest {

    @get:Rule val tmpFolder = TemporaryFolder()

    private val testScope = TestScope(UnconfinedTestDispatcher() + Job())
    private val itemRepository = mockk<ItemRepository>(relaxUnitFun = true)
    private val scenarioRepository = mockk<ScenarioRepository>()
    private lateinit var dataStore: DataStore<Preferences>

    private val breakfast = Item(1L, "Eggs", "Breakfast", Rarity.COMMON)
    private val porridge = Item(3L, "Porridge", "Breakfast", Rarity.UNCOMMON)
    private val activity = Item(2L, "Walk", "Activity", Rarity.COMMON)

    private val dayScenario = Scenario(
        id = 1L,
        name = "Day",
        slots = listOf(
            ScenarioSlot(category = "Breakfast", count = 1),
            ScenarioSlot(category = "Activity", count = 1),
        ),
    )

    private val bigScenario = Scenario(
        id = 2L,
        name = "Big Day",
        slots = listOf(
            ScenarioSlot(category = "Breakfast", count = 2),
            ScenarioSlot(category = "Activity", count = 1),
        ),
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        dataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
        ) { tmpFolder.newFile("test_prefs_${System.nanoTime()}.preferences_pb") }
        every { itemRepository.observeEnabled("Breakfast") } returns flowOf(listOf(breakfast, porridge))
        every { itemRepository.observeEnabled("Activity") } returns flowOf(listOf(activity))
        every { scenarioRepository.observeAll() } returns flowOf(listOf(dayScenario, bigScenario))
    }

    @After
    fun tearDown() {
        testScope.cancel()
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state auto-selects first scenario`() = runTest {
        val vm = RollViewModel(itemRepository, scenarioRepository, dataStore)
        assertEquals(1L, vm.uiState.value.selectedScenarioId)
        assertEquals(2, vm.uiState.value.scenarios.size)
        assertTrue(vm.uiState.value.results.isEmpty())
    }

    @Test
    fun `rollAll produces results matching scenario slots`() = runTest {
        val vm = RollViewModel(itemRepository, scenarioRepository, dataStore)
        vm.rollAll()
        val results = vm.uiState.value.results
        assertEquals(2, results.size)
        val categories = results.map { it.category }.toSet()
        assertEquals(setOf("Breakfast", "Activity"), categories)
        assertTrue(results.all { it.item != null })
    }

    @Test
    fun `rollAll with count 2 produces correct number of results`() = runTest {
        val vm = RollViewModel(itemRepository, scenarioRepository, dataStore)
        vm.selectScenario(2L)
        vm.rollAll()
        val results = vm.uiState.value.results
        assertEquals(3, results.size)
        val breakfastResults = results.filter { it.category == "Breakfast" }
        val activityResults = results.filter { it.category == "Activity" }
        assertEquals(2, breakfastResults.size)
        assertEquals(1, activityResults.size)
        // Labels should be category name without #N
        assertTrue(results.all { it.label == it.category })
        // Two breakfast items should be different (picked without replacement)
        assertNotEquals(breakfastResults[0].item?.id, breakfastResults[1].item?.id)
    }

    @Test
    fun `reroll replaces only targeted result`() = runTest {
        val vm = RollViewModel(itemRepository, scenarioRepository, dataStore)
        vm.rollAll()
        val original = vm.uiState.value.results.toList()
        vm.reroll(0)
        val updated = vm.uiState.value.results
        assertEquals(original[1].item, updated[1].item)
    }

    @Test
    fun `first roll is free, subsequent rerolls consume budget`() = runTest {
        val vm = RollViewModel(itemRepository, scenarioRepository, dataStore)
        vm.rollAll()
        assertTrue(vm.uiState.value.hasRolled)
        val initialRerolls = vm.uiState.value.rerollsLeft
        vm.reroll(0)
        assertEquals(initialRerolls - 1, vm.uiState.value.rerollsLeft)
        vm.rollAll()
        assertEquals(initialRerolls - 2, vm.uiState.value.rerollsLeft)
    }

    @Test
    fun `selectScenario clears results`() = runTest {
        val vm = RollViewModel(itemRepository, scenarioRepository, dataStore)
        vm.rollAll()
        assertTrue(vm.uiState.value.results.isNotEmpty())
        vm.selectScenario(2L)
        assertTrue(vm.uiState.value.results.isEmpty())
        assertEquals(2L, vm.uiState.value.selectedScenarioId)
    }

    @Test
    fun `selectScenario with same id does not clear results`() = runTest {
        val vm = RollViewModel(itemRepository, scenarioRepository, dataStore)
        vm.rollAll()
        val results = vm.uiState.value.results
        vm.selectScenario(1L) // same scenario
        assertEquals(results, vm.uiState.value.results)
    }

    @Test
    fun `complete marks card and uncomplete reverts it`() = runTest {
        val vm = RollViewModel(itemRepository, scenarioRepository, dataStore)
        vm.rollAll()
        assertFalse(vm.uiState.value.results[0].completed)
        vm.complete(0)
        assertTrue(vm.uiState.value.results[0].completed)
        coVerify { itemRepository.incrementCompleted(any()) }
        vm.uncomplete(0)
        assertFalse(vm.uiState.value.results[0].completed)
    }

    @Test
    fun `rerollAll preserves completed cards`() = runTest {
        val vm = RollViewModel(itemRepository, scenarioRepository, dataStore)
        vm.rollAll()
        val completedItem = vm.uiState.value.results[0].item
        vm.complete(0)
        vm.rollAll()
        val results = vm.uiState.value.results
        val completed = results.filter { it.completed }
        assertEquals(1, completed.size)
        assertEquals(completedItem, completed[0].item)
    }

    @Test
    fun `insufficient items shows warning and blocks roll`() = runTest {
        every { itemRepository.observeEnabled("Empty") } returns flowOf(emptyList())
        every { scenarioRepository.observeAll() } returns flowOf(listOf(
            Scenario(id = 3L, name = "Bad", slots = listOf(ScenarioSlot(category = "Empty", count = 2))),
        ))
        val vm = RollViewModel(itemRepository, scenarioRepository, dataStore)
        vm.rollAll()
        assertTrue(vm.uiState.value.insufficientItems.isNotEmpty())
        assertTrue(vm.uiState.value.results.isEmpty())
    }

    @Test
    fun `rollAll increments rolled stats`() = runTest {
        val vm = RollViewModel(itemRepository, scenarioRepository, dataStore)
        vm.rollAll()
        coVerify(atLeast = 1) { itemRepository.incrementRolled(any()) }
    }
}
