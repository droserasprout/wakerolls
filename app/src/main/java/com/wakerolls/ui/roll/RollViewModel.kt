package com.wakerolls.ui.roll

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wakerolls.data.repository.ItemRepository
import com.wakerolls.data.repository.ScenarioRepository
import com.wakerolls.domain.model.Item
import com.wakerolls.domain.model.Rarity
import com.wakerolls.domain.model.Scenario
import com.wakerolls.ui.settings.SettingsViewModel.Companion.KEY_ALLOW_PARTIAL_REROLLS
import com.wakerolls.ui.settings.SettingsViewModel.Companion.KEY_ALLOW_REROLLS
import com.wakerolls.ui.settings.SettingsViewModel.Companion.KEY_ENABLE_ANIMATIONS
import com.wakerolls.ui.settings.SettingsViewModel.Companion.KEY_REROLLS_DATE
import com.wakerolls.ui.settings.SettingsViewModel.Companion.KEY_REROLLS_PER_DAY
import com.wakerolls.ui.settings.SettingsViewModel.Companion.KEY_REROLLS_USED
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class RollResult(
    val label: String,
    val category: String,
    val item: Item?,
)

data class RollUiState(
    val scenarios: List<Scenario> = emptyList(),
    val selectedScenarioId: Long? = null,
    val results: List<RollResult> = emptyList(),
    val isRolling: Boolean = false,
    val hasRolled: Boolean = false,
    val rerollsLeft: Int = 3,
    val rerollsPerDay: Int = 3,
    val allowRerolls: Boolean = true,
    val allowPartialRerolls: Boolean = true,
    val enableAnimations: Boolean = true,
    val rollGeneration: Int = 0,
)

@HiltViewModel
class RollViewModel @Inject constructor(
    private val itemRepository: ItemRepository,
    private val scenarioRepository: ScenarioRepository,
    private val dataStore: DataStore<Preferences>,
) : ViewModel() {

    companion object {
        private val KEY_SAVED_SCENARIO_ID = longPreferencesKey("roll_scenario_id")
        private val KEY_SAVED_RESULTS = stringPreferencesKey("roll_results")
    }

    private val _uiState = MutableStateFlow(RollUiState())
    val uiState: StateFlow<RollUiState> = _uiState.asStateFlow()
    private var restoredResults = false

    init {
        viewModelScope.launch {
            scenarioRepository.observeAll().collect { scenarios ->
                val currentId = _uiState.value.selectedScenarioId
                val selectedId = if (currentId != null && scenarios.any { it.id == currentId }) {
                    currentId
                } else {
                    scenarios.firstOrNull()?.id
                }
                _uiState.value = _uiState.value.copy(
                    scenarios = scenarios,
                    selectedScenarioId = selectedId,
                )
                if (!restoredResults) {
                    restoredResults = true
                    restoreResults()
                }
            }
        }
        viewModelScope.launch {
            dataStore.data.collect { prefs ->
                val perDay = prefs[KEY_REROLLS_PER_DAY] ?: 3
                val allowRerolls = prefs[KEY_ALLOW_REROLLS] ?: true
                val allowPartialRerolls = prefs[KEY_ALLOW_PARTIAL_REROLLS] ?: true
                val enableAnimations = prefs[KEY_ENABLE_ANIMATIONS] ?: true
                val today = LocalDate.now().toString()
                val storedDate = prefs[KEY_REROLLS_DATE] ?: ""
                val used = if (storedDate == today) (prefs[KEY_REROLLS_USED] ?: 0) else 0
                val rerollsLeft = if (perDay == 0) Int.MAX_VALUE else (perDay - used).coerceAtLeast(0)
                _uiState.value = _uiState.value.copy(
                    rerollsPerDay = perDay,
                    rerollsLeft = rerollsLeft,
                    allowRerolls = allowRerolls,
                    allowPartialRerolls = allowPartialRerolls,
                    enableAnimations = enableAnimations,
                )
            }
        }
    }

    fun selectScenario(id: Long) {
        if (id == _uiState.value.selectedScenarioId) return
        _uiState.value = _uiState.value.copy(selectedScenarioId = id, results = emptyList(), hasRolled = false)
        viewModelScope.launch { saveResults(id, emptyList()) }
    }

    fun rollAll() {
        val state = _uiState.value
        val scenario = state.scenarios.find { it.id == state.selectedScenarioId } ?: return
        if (state.hasRolled) {
            if (!state.allowRerolls || state.rerollsLeft <= 0) return
        }
        viewModelScope.launch {
            if (state.hasRolled) consumeReroll()

            // Outro animation: shrink old cards
            if (state.hasRolled && state.results.isNotEmpty() && state.enableAnimations) {
                _uiState.value = _uiState.value.copy(isRolling = true)
                delay(300) // wait for shrink animation
            }

            val results = mutableListOf<RollResult>()
            for (slot in scenario.slots) {
                val items = pickMultiple(slot.category, slot.count)
                if (slot.count == 1) {
                    results.add(RollResult(label = slot.category, category = slot.category, item = items.firstOrNull()))
                } else {
                    items.forEachIndexed { index, item ->
                        results.add(RollResult(label = "${slot.category} #${index + 1}", category = slot.category, item = item))
                    }
                    repeat(slot.count - items.size) { i ->
                        results.add(RollResult(label = "${slot.category} #${items.size + i + 1}", category = slot.category, item = null))
                    }
                }
            }
            _uiState.value = _uiState.value.copy(
                results = results,
                isRolling = false,
                hasRolled = true,
                rollGeneration = _uiState.value.rollGeneration + 1,
            )
            saveResults(state.selectedScenarioId!!, results)
        }
    }

    fun reroll(index: Int) {
        if (!_uiState.value.allowPartialRerolls || _uiState.value.rerollsLeft <= 0) return
        val current = _uiState.value.results.getOrNull(index) ?: return
        viewModelScope.launch {
            consumeReroll()
            val items = itemRepository.observeEnabled(current.category).first()
            val picked = if (items.isEmpty()) null else Rarity.weightedRandom(items) { it.rarity }
            val updated = _uiState.value.results.toMutableList()
            updated[index] = current.copy(item = picked)
            _uiState.value = _uiState.value.copy(results = updated)
            _uiState.value.selectedScenarioId?.let { saveResults(it, updated) }
        }
    }

    private suspend fun consumeReroll() {
        if (_uiState.value.rerollsPerDay == 0) return // unlimited
        _uiState.value = _uiState.value.copy(rerollsLeft = (_uiState.value.rerollsLeft - 1).coerceAtLeast(0))
        val today = LocalDate.now().toString()
        dataStore.edit { prefs ->
            val storedDate = prefs[KEY_REROLLS_DATE] ?: ""
            val used = if (storedDate == today) (prefs[KEY_REROLLS_USED] ?: 0) else 0
            prefs[KEY_REROLLS_DATE] = today
            prefs[KEY_REROLLS_USED] = used + 1
        }
    }

    private suspend fun pickMultiple(category: String, count: Int): List<Item> {
        val available = itemRepository.observeEnabled(category).first().toMutableList()
        val picked = mutableListOf<Item>()
        repeat(count) {
            if (available.isEmpty()) return picked
            val item = Rarity.weightedRandom(available) { it.rarity }
            picked.add(item)
            available.remove(item)
        }
        return picked
    }

    // Persistence: encode results as "label\tcategory\titemId;..." (-1 for null item)
    private suspend fun saveResults(scenarioId: Long, results: List<RollResult>) {
        dataStore.edit { prefs ->
            prefs[KEY_SAVED_SCENARIO_ID] = scenarioId
            prefs[KEY_SAVED_RESULTS] = results.joinToString(";") { r ->
                "${r.label}\t${r.category}\t${r.item?.id ?: -1}"
            }
        }
    }

    private suspend fun restoreResults() {
        val prefs = dataStore.data.first()
        val savedScenarioId = prefs[KEY_SAVED_SCENARIO_ID] ?: return
        val savedResults = prefs[KEY_SAVED_RESULTS] ?: return
        if (savedResults.isBlank()) return

        val entries = savedResults.split(";").mapNotNull { entry ->
            val parts = entry.split("\t")
            if (parts.size != 3) return@mapNotNull null
            Triple(parts[0], parts[1], parts[2].toLongOrNull() ?: return@mapNotNull null)
        }
        if (entries.isEmpty()) return

        val itemIds = entries.mapNotNull { (_, _, id) -> if (id != -1L) id else null }
        val itemMap = if (itemIds.isNotEmpty()) itemRepository.getByIds(itemIds) else emptyMap()

        val results = entries.map { (label, category, itemId) ->
            RollResult(label = label, category = category, item = itemMap[itemId])
        }

        _uiState.value = _uiState.value.copy(
            selectedScenarioId = savedScenarioId,
            results = results,
            hasRolled = true,
        )
    }
}
