package com.wakerolls.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wakerolls.data.repository.ItemRepository
import com.wakerolls.domain.model.Category
import com.wakerolls.domain.model.Item
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LibraryUiState(
    val grouped: Map<Category, List<Item>> = emptyMap(),
)

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val repository: ItemRepository,
) : ViewModel() {

    val uiState: StateFlow<LibraryUiState> = repository.observeAll()
        .map { items -> LibraryUiState(grouped = items.groupBy { it.category }) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LibraryUiState())

    fun toggleEnabled(item: Item) {
        viewModelScope.launch {
            repository.update(item.copy(enabled = !item.enabled))
        }
    }
}
