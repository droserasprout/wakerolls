package com.wakerolls.ui.roll

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wakerolls.data.repository.ItemRepository
import com.wakerolls.domain.model.Category
import com.wakerolls.domain.model.Item
import com.wakerolls.domain.model.Rarity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RollUiState(
    val breakfast: Item? = null,
    val activity: Item? = null,
    val isRolling: Boolean = false,
)

@HiltViewModel
class RollViewModel @Inject constructor(
    private val repository: ItemRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RollUiState())
    val uiState: StateFlow<RollUiState> = _uiState.asStateFlow()

    fun rollAll() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRolling = true)
            val breakfast = pickFrom(Category.BREAKFAST)
            val activity = pickFrom(Category.ACTIVITY)
            _uiState.value = RollUiState(breakfast = breakfast, activity = activity)
        }
    }

    fun reroll(category: Category) {
        viewModelScope.launch {
            val picked = pickFrom(category)
            _uiState.value = when (category) {
                Category.BREAKFAST -> _uiState.value.copy(breakfast = picked)
                Category.ACTIVITY -> _uiState.value.copy(activity = picked)
            }
        }
    }

    private suspend fun pickFrom(category: Category): Item? {
        val items = repository.observeEnabled(category).first()
        if (items.isEmpty()) return null
        return Rarity.weightedRandom(items) { it.rarity }
    }
}
