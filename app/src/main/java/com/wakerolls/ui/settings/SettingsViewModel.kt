package com.wakerolls.ui.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wakerolls.worker.DailyRollWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val notificationsEnabled: Boolean = false,
    val notificationHour: Int = 8,
    val notificationMinute: Int = 0,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    companion object {
        val KEY_NOTIF_ENABLED = booleanPreferencesKey("notif_enabled")
        val KEY_NOTIF_HOUR = intPreferencesKey("notif_hour")
        val KEY_NOTIF_MINUTE = intPreferencesKey("notif_minute")
    }

    val uiState: StateFlow<SettingsUiState> = dataStore.data
        .map { prefs ->
            SettingsUiState(
                notificationsEnabled = prefs[KEY_NOTIF_ENABLED] ?: false,
                notificationHour = prefs[KEY_NOTIF_HOUR] ?: 8,
                notificationMinute = prefs[KEY_NOTIF_MINUTE] ?: 0,
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

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
