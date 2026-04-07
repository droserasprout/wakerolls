package com.wakerolls.ui.roll

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wakerolls.data.repository.ItemRepository
import com.wakerolls.data.repository.ScenarioRepository
import com.wakerolls.domain.model.Item
import com.wakerolls.domain.model.Rarity
import com.wakerolls.domain.model.Scenario
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
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
)

@HiltViewModel
class RollViewModel @Inject constructor(
    private val itemRepository: ItemRepository,
    private val scenarioRepository: ScenarioRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RollUiState())
    val uiState: StateFlow<RollUiState> = _uiState.asStateFlow()

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
            }
        }
    }

    fun selectScenario(id: Long) {
        _uiState.value = _uiState.value.copy(selectedScenarioId = id, results = emptyList())
    }

    fun rollAll() {
        val scenario = _uiState.value.scenarios.find { it.id == _uiState.value.selectedScenarioId } ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRolling = true)
            val results = mutableListOf<RollResult>()
            for (slot in scenario.slots) {
                val items = pickMultiple(slot.category, slot.count)
                if (slot.count == 1) {
                    results.add(RollResult(
                        label = slot.category,
                        category = slot.category,
                        item = items.firstOrNull(),
                    ))
                } else {
                    items.forEachIndexed { index, item ->
                        results.add(RollResult(
                            label = "${slot.category} #${index + 1}",
                            category = slot.category,
                            item = item,
                        ))
                    }
                    // Fill remaining slots if fewer items than count
                    repeat(slot.count - items.size) { i ->
                        results.add(RollResult(
                            label = "${slot.category} #${items.size + i + 1}",
                            category = slot.category,
                            item = null,
                        ))
                    }
                }
            }
            _uiState.value = _uiState.value.copy(results = results, isRolling = false)
        }
    }

    fun reroll(index: Int) {
        val current = _uiState.value.results.getOrNull(index) ?: return
        viewModelScope.launch {
            val items = itemRepository.observeEnabled(current.category).first()
            val picked = if (items.isEmpty()) null else Rarity.weightedRandom(items) { it.rarity }
            val updated = _uiState.value.results.toMutableList()
            updated[index] = current.copy(item = picked)
            _uiState.value = _uiState.value.copy(results = updated)
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
}
