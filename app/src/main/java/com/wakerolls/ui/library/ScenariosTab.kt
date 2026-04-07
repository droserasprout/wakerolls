package com.wakerolls.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.wakerolls.domain.model.Scenario
import com.wakerolls.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScenariosTab(state: LibraryUiState, viewModel: LibraryViewModel) {
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)) {
            items(state.scenarios, key = { it.id }) { scenario ->
                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = {
                        if (it == SwipeToDismissBoxValue.EndToStart) {
                            viewModel.onDeleteScenarioClick(scenario)
                        }
                        false
                    }
                )
                SwipeToDismissBox(
                    state = dismissState,
                    backgroundContent = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(12.dp))
                                .background(AccentCoral.copy(alpha = 0.3f))
                                .padding(horizontal = 20.dp),
                            contentAlignment = Alignment.CenterEnd,
                        ) {
                            Icon(Icons.Filled.Delete, "Delete", tint = AccentCoral)
                        }
                    },
                    enableDismissFromStartToEnd = false,
                ) {
                    ScenarioCard(
                        scenario = scenario,
                        onClick = { viewModel.onEditScenarioClick(scenario) },
                    )
                }
                Spacer(Modifier.height(8.dp))
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
