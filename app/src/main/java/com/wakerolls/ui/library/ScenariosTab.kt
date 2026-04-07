package com.wakerolls.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
                    Spacer(Modifier.height(16.dp))
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
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(DarkSurface)
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(listOf(AccentGold.copy(alpha = 0.4f), AccentGold.copy(alpha = 0.05f))),
                shape = RoundedCornerShape(20.dp),
            )
            .clickable { onClick() }
            .padding(20.dp),
    ) {
        Column {
            Text(
                text = scenario.name,
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
            )
            Spacer(Modifier.height(12.dp))
            scenario.slots.forEach { slot ->
                Row(
                    modifier = Modifier.padding(vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = slot.category.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        letterSpacing = 2.sp,
                        color = TextSecondary,
                    )
                    if (slot.count > 1) {
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = AccentGold.copy(alpha = 0.15f),
                        ) {
                            Text(
                                text = "\u00D7${slot.count}",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = AccentGold,
                            )
                        }
                    }
                }
            }
        }
    }
}
