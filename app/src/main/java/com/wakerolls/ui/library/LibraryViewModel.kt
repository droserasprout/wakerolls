package com.wakerolls.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wakerolls.data.repository.ItemRepository
import com.wakerolls.data.repository.ScenarioRepository
import com.wakerolls.domain.model.Item
import com.wakerolls.domain.model.Rarity
import com.wakerolls.domain.model.Scenario
import com.wakerolls.domain.model.ScenarioSlot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LibraryUiState(
    val grouped: Map<String, List<Item>> = emptyMap(),
    val categories: List<String> = emptyList(),
    val editingItem: Item? = null,
    val showDeleteConfirm: Item? = null,
    val scenarios: List<Scenario> = emptyList(),
    val editingScenario: Scenario? = null,
    val showDeleteScenarioConfirm: Scenario? = null,
)

data class DialogState(
    val editingItem: Item? = null,
    val showDeleteConfirm: Item? = null,
    val editingScenario: Scenario? = null,
    val showDeleteScenarioConfirm: Scenario? = null,
)

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val repository: ItemRepository,
    private val scenarioRepository: ScenarioRepository,
) : ViewModel() {

    private val _dialogState = MutableStateFlow(DialogState())

    val uiState: StateFlow<LibraryUiState> = combine(
        repository.observeAll(),
        repository.observeCategories(),
        scenarioRepository.observeAll(),
        _dialogState,
    ) { items, categories, scenarios, dialog ->
        LibraryUiState(
            grouped = items.groupBy { it.category },
            categories = categories,
            editingItem = dialog.editingItem,
            showDeleteConfirm = dialog.showDeleteConfirm,
            scenarios = scenarios,
            editingScenario = dialog.editingScenario,
            showDeleteScenarioConfirm = dialog.showDeleteScenarioConfirm,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LibraryUiState())

    fun toggleEnabled(item: Item) {
        viewModelScope.launch {
            repository.update(item.copy(enabled = !item.enabled))
        }
    }

    fun onAddClick() {
        _dialogState.value = _dialogState.value.copy(
            editingItem = Item(name = "", category = "", rarity = Rarity.COMMON)
        )
    }

    fun onEditClick(item: Item) {
        _dialogState.value = _dialogState.value.copy(editingItem = item)
    }

    fun onSaveItem(item: Item) {
        viewModelScope.launch {
            if (item.id == 0L) repository.save(item) else repository.update(item)
            _dialogState.value = _dialogState.value.copy(editingItem = null)
        }
    }

    fun onDismissEdit() {
        _dialogState.value = _dialogState.value.copy(editingItem = null)
    }

    fun onDeleteClick(item: Item) {
        _dialogState.value = _dialogState.value.copy(showDeleteConfirm = item)
    }

    fun onConfirmDelete() {
        val item = _dialogState.value.showDeleteConfirm ?: return
        viewModelScope.launch {
            repository.delete(item)
            _dialogState.value = _dialogState.value.copy(showDeleteConfirm = null)
        }
    }

    fun onDismissDelete() {
        _dialogState.value = _dialogState.value.copy(showDeleteConfirm = null)
    }

    fun onAddScenarioClick() {
        _dialogState.value = _dialogState.value.copy(
            editingScenario = Scenario(
                name = "",
                slots = listOf(ScenarioSlot(category = "", count = 1)),
            )
        )
    }

    fun onEditScenarioClick(scenario: Scenario) {
        _dialogState.value = _dialogState.value.copy(editingScenario = scenario)
    }

    fun onSaveScenario(scenario: Scenario) {
        viewModelScope.launch {
            scenarioRepository.save(scenario)
            _dialogState.value = _dialogState.value.copy(editingScenario = null)
        }
    }

    fun onDismissScenarioEdit() {
        _dialogState.value = _dialogState.value.copy(editingScenario = null)
    }

    fun onDeleteScenarioClick(scenario: Scenario) {
        _dialogState.value = _dialogState.value.copy(showDeleteScenarioConfirm = scenario)
    }

    fun onConfirmDeleteScenario() {
        val scenario = _dialogState.value.showDeleteScenarioConfirm ?: return
        viewModelScope.launch {
            scenarioRepository.delete(scenario.id)
            _dialogState.value = _dialogState.value.copy(showDeleteScenarioConfirm = null)
        }
    }

    fun onDismissDeleteScenario() {
        _dialogState.value = _dialogState.value.copy(showDeleteScenarioConfirm = null)
    }
}
