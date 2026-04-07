package com.wakerolls.ui.settings

import android.content.Context
import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wakerolls.data.ImportExportManager
import com.wakerolls.domain.model.Rarity
import com.wakerolls.worker.DailyRollWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val notificationsEnabled: Boolean = false,
    val notificationHour: Int = 8,
    val notificationMinute: Int = 0,
    val rerollsPerDay: Int = 3,
    val allowRerolls: Boolean = true,
    val allowPartialRerolls: Boolean = true,
    val enableAnimations: Boolean = true,
    val weights: Map<Rarity, Int> = Rarity.entries.associate { it to it.weight },
    val importExportMessage: String? = null,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    @ApplicationContext private val context: Context,
    private val importExportManager: ImportExportManager,
) : ViewModel() {

    private val _importExportMessage = MutableStateFlow<String?>(null)

    companion object {
        val KEY_NOTIF_ENABLED = booleanPreferencesKey("notif_enabled")
        val KEY_NOTIF_HOUR = intPreferencesKey("notif_hour")
        val KEY_NOTIF_MINUTE = intPreferencesKey("notif_minute")
        val KEY_REROLLS_PER_DAY = intPreferencesKey("rerolls_per_day")
        val KEY_REROLLS_USED = intPreferencesKey("rerolls_used")
        val KEY_REROLLS_DATE = stringPreferencesKey("rerolls_date")
        val KEY_ALLOW_REROLLS = booleanPreferencesKey("allow_rerolls")
        val KEY_ALLOW_PARTIAL_REROLLS = booleanPreferencesKey("allow_partial_rerolls")
        val KEY_ENABLE_ANIMATIONS = booleanPreferencesKey("enable_animations")

        fun weightKey(rarity: Rarity) = intPreferencesKey("weight_${rarity.name.lowercase()}")
    }

    val uiState: StateFlow<SettingsUiState> = combine(
        dataStore.data,
        _importExportMessage,
    ) { prefs, message ->
        SettingsUiState(
            notificationsEnabled = prefs[KEY_NOTIF_ENABLED] ?: false,
            notificationHour = prefs[KEY_NOTIF_HOUR] ?: 8,
            notificationMinute = prefs[KEY_NOTIF_MINUTE] ?: 0,
            rerollsPerDay = prefs[KEY_REROLLS_PER_DAY] ?: 3,
            allowRerolls = prefs[KEY_ALLOW_REROLLS] ?: true,
            allowPartialRerolls = prefs[KEY_ALLOW_PARTIAL_REROLLS] ?: true,
            enableAnimations = prefs[KEY_ENABLE_ANIMATIONS] ?: true,
            weights = Rarity.entries.associate { r ->
                r to (prefs[weightKey(r)] ?: r.weight)
            },
            importExportMessage = message,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.edit { it[KEY_NOTIF_ENABLED] = enabled }
            val current = uiState.value
            if (enabled) {
                DailyRollWorker.scheduleDaily(context, current.notificationHour, current.notificationMinute)
            } else {
                DailyRollWorker.cancel(context)
            }
        }
    }

    fun setRerollsPerDay(count: Int) {
        viewModelScope.launch {
            dataStore.edit { it[KEY_REROLLS_PER_DAY] = count }
        }
    }

    fun setAllowRerolls(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.edit { it[KEY_ALLOW_REROLLS] = enabled }
        }
    }

    fun setAllowPartialRerolls(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.edit { it[KEY_ALLOW_PARTIAL_REROLLS] = enabled }
        }
    }

    fun setEnableAnimations(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.edit { it[KEY_ENABLE_ANIMATIONS] = enabled }
        }
    }

    fun setWeight(rarity: Rarity, weight: Int) {
        viewModelScope.launch {
            dataStore.edit { it[weightKey(rarity)] = weight.coerceAtLeast(0) }
        }
    }

    fun exportData(uri: Uri) {
        viewModelScope.launch {
            try {
                val json = importExportManager.exportToJson()
                context.contentResolver.openOutputStream(uri)?.use { it.write(json.toByteArray()) }
                _importExportMessage.value = "Export successful"
            } catch (e: Exception) {
                _importExportMessage.value = "Export failed: ${e.message}"
            }
        }
    }

    fun importData(uri: Uri) {
        viewModelScope.launch {
            try {
                val json = context.contentResolver.openInputStream(uri)?.use {
                    it.bufferedReader().readText()
                } ?: throw IllegalStateException("Cannot read file")
                importExportManager.importFromJson(json)
                _importExportMessage.value = "Import successful"
            } catch (e: Exception) {
                _importExportMessage.value = "Import failed: ${e.message}"
            }
        }
    }

    fun dismissImportExportMessage() {
        _importExportMessage.value = null
    }

    fun setNotificationTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            dataStore.edit {
                it[KEY_NOTIF_HOUR] = hour
                it[KEY_NOTIF_MINUTE] = minute
            }
            if (uiState.value.notificationsEnabled) {
                DailyRollWorker.scheduleDaily(context, hour, minute)
            }
        }
    }
}
