package com.wakerolls.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wakerolls.data.repository.ItemRepository
import com.wakerolls.domain.model.Category
import com.wakerolls.domain.model.Item
import com.wakerolls.domain.model.Rarity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LibraryUiState(
    val grouped: Map<Category, List<Item>> = emptyMap(),
    val editingItem: Item? = null,
    val showDeleteConfirm: Item? = null,
)

data class DialogState(
    val editingItem: Item? = null,
    val showDeleteConfirm: Item? = null,
)

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val repository: ItemRepository,
) : ViewModel() {

    private val _dialogState = MutableStateFlow(DialogState())

    val uiState: StateFlow<LibraryUiState> = combine(
        repository.observeAll(),
        _dialogState,
    ) { items, dialog ->
        LibraryUiState(
            grouped = items.groupBy { it.category },
            editingItem = dialog.editingItem,
            showDeleteConfirm = dialog.showDeleteConfirm,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LibraryUiState())

    fun toggleEnabled(item: Item) {
        viewModelScope.launch {
            repository.update(item.copy(enabled = !item.enabled))
        }
    }

    fun onAddClick() {
        _dialogState.value = _dialogState.value.copy(
            editingItem = Item(name = "", category = Category.BREAKFAST, rarity = Rarity.COMMON)
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
}
