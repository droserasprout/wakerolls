package com.wakerolls.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wakerolls.ui.theme.*

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showTimePicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(24.dp))
        Text("Settings", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(24.dp))

        // Allow rerolls toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(DarkSurface)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text("Allow rerolls", style = MaterialTheme.typography.titleMedium)
                Text("Re-roll your results after the first roll", style = MaterialTheme.typography.bodyMedium)
            }
            Switch(
                checked = state.allowRerolls,
                onCheckedChange = { viewModel.setAllowRerolls(it) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = AccentGold,
                    checkedTrackColor = AccentGold.copy(alpha = 0.4f),
                ),
            )
        }

        if (state.allowRerolls) {
            Spacer(Modifier.height(12.dp))

            // Rerolls per day
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurface)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Rerolls per day", style = MaterialTheme.typography.titleMedium)
                    Text("How many rerolls you get each day", style = MaterialTheme.typography.bodyMedium)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { viewModel.setRerollsPerDay((state.rerollsPerDay - 1).coerceAtLeast(0)) },
                        enabled = state.rerollsPerDay > 0,
                    ) {
                        Text("\u2212", style = MaterialTheme.typography.titleLarge,
                            color = if (state.rerollsPerDay > 0) TextPrimary else TextSecondary)
                    }
                    Text(
                        text = if (state.rerollsPerDay == 0) "\u221E" else state.rerollsPerDay.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        color = AccentGold,
                    )
                    IconButton(
                        onClick = { viewModel.setRerollsPerDay(state.rerollsPerDay + 1) },
                        enabled = state.rerollsPerDay < 10,
                    ) {
                        Text("+", style = MaterialTheme.typography.titleLarge,
                            color = if (state.rerollsPerDay < 10) TextPrimary else TextSecondary)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Allow partial rerolls toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurface)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Allow partial rerolls", style = MaterialTheme.typography.titleMedium)
                    Text("Re-roll individual cards", style = MaterialTheme.typography.bodyMedium)
                }
                Switch(
                    checked = state.allowPartialRerolls,
                    onCheckedChange = { viewModel.setAllowPartialRerolls(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = AccentGold,
                        checkedTrackColor = AccentGold.copy(alpha = 0.4f),
                    ),
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Notification toggle row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(DarkSurface)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text("Daily reminder", style = MaterialTheme.typography.titleMedium)
                Text("Get notified to roll your day", style = MaterialTheme.typography.bodyMedium)
            }
            Switch(
                checked = state.notificationsEnabled,
                onCheckedChange = { viewModel.setNotificationsEnabled(it) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = AccentGold,
                    checkedTrackColor = AccentGold.copy(alpha = 0.4f),
                ),
            )
        }

        if (state.notificationsEnabled) {
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurface)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Reminder time", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "%02d:%02d".format(state.notificationHour, state.notificationMinute),
                        style = MaterialTheme.typography.bodyMedium,
                        color = AccentGold,
                    )
                }
                TextButton(onClick = { showTimePicker = true }) {
                    Text("Change", color = AccentGold)
                }
            }
        }

        if (showTimePicker) {
            TimePickerDialog(
                initialHour = state.notificationHour,
                initialMinute = state.notificationMinute,
                onConfirm = { h, m ->
                    viewModel.setNotificationTime(h, m)
                    showTimePicker = false
                },
                onDismiss = { showTimePicker = false },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onConfirm: (Int, Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val state = rememberTimePickerState(initialHour = initialHour, initialMinute = initialMinute)
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onConfirm(state.hour, state.minute) }) {
                Text("OK", color = AccentGold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        text = { TimePicker(state = state) },
        containerColor = DarkSurface,
    )
}
