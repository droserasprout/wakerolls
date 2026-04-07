package com.wakerolls.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wakerolls.domain.model.Scenario
import com.wakerolls.ui.theme.*

@Composable
fun ScenariosScreen(viewModel: LibraryViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
    ) {
        Column {
            Spacer(Modifier.height(24.dp))
            Text(
                text = "Scenarios",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(horizontal = 20.dp),
            )
            Spacer(Modifier.height(16.dp))
            LazyColumn(contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)) {
                items(state.scenarios, key = { it.id }) { scenario ->
                    ScenarioCard(
                        scenario = scenario,
                        onClick = { viewModel.onEditScenarioClick(scenario) },
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }
        }

        FloatingActionButton(
            onClick = { viewModel.onAddScenarioClick() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
            containerColor = AccentGold,
            contentColor = DarkBackground,
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add scenario")
        }
    }

    // Scenario Edit / Add dialog
    state.editingScenario?.let { editScenario ->
        ScenarioEditDialog(
            scenario = editScenario,
            categories = state.categories,
            onSave = { viewModel.onSaveScenario(it) },
            onDelete = if (editScenario.id != 0L) {
                { viewModel.onDeleteScenarioClick(editScenario); viewModel.onDismissScenarioEdit() }
            } else null,
            onDismiss = { viewModel.onDismissScenarioEdit() },
        )
    }

    // Scenario delete confirmation
    state.showDeleteScenarioConfirm?.let { deleteScenario ->
        AlertDialog(
            onDismissRequest = { viewModel.onDismissDeleteScenario() },
            title = { Text("Delete scenario?", color = TextPrimary) },
            text = { Text("Delete \"${deleteScenario.name}\"?", color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = { viewModel.onConfirmDeleteScenario() }) {
                    Text("Delete", color = AccentCoral)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onDismissDeleteScenario() }) { Text("Cancel") }
            },
            containerColor = DarkSurface,
        )
    }
}

@Composable
private fun ScenarioCard(scenario: Scenario, onClick: () -> Unit) {
    val slotSummary = scenario.slots.joinToString(", ") { "${it.count}x ${it.category}" }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurface)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = scenario.name,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = slotSummary,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )
        }
    }
}
