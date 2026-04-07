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
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wakerolls.domain.model.Category
import com.wakerolls.domain.model.Item
import com.wakerolls.ui.roll.RarityBadge
import com.wakerolls.ui.roll.color
import com.wakerolls.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(padding: PaddingValues, viewModel: LibraryViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(padding),
    ) {
        Column {
            Spacer(Modifier.height(24.dp))
            Text(
                text = "Library",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(horizontal = 20.dp),
            )
            Spacer(Modifier.height(16.dp))

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = DarkSurfaceVariant,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = AccentGold,
                    )
                },
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            "Items",
                            color = if (selectedTab == 0) AccentGold else TextSecondary,
                        )
                    },
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            "Scenarios",
                            color = if (selectedTab == 1) AccentGold else TextSecondary,
                        )
                    },
                )
            }

            when (selectedTab) {
                0 -> ItemsTab(state, viewModel)
                1 -> ScenariosTab(state, viewModel)
            }
        }
    }

    // Item Edit / Add dialog
    state.editingItem?.let { editItem ->
        ItemEditDialog(
            item = editItem,
            onSave = { viewModel.onSaveItem(it) },
            onDismiss = { viewModel.onDismissEdit() },
        )
    }

    // Item delete confirmation
    state.showDeleteConfirm?.let { deleteItem ->
        AlertDialog(
            onDismissRequest = { viewModel.onDismissDelete() },
            title = { Text("Delete item?", color = TextPrimary) },
            text = { Text("Delete \"${deleteItem.name}\"?", color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = { viewModel.onConfirmDelete() }) {
                    Text("Delete", color = AccentCoral)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onDismissDelete() }) { Text("Cancel") }
            },
            containerColor = DarkSurface,
        )
    }

    // Scenario Edit / Add dialog
    state.editingScenario?.let { editScenario ->
        ScenarioEditDialog(
            scenario = editScenario,
            onSave = { viewModel.onSaveScenario(it) },
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemsTab(state: LibraryUiState, viewModel: LibraryViewModel) {
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)) {
            state.grouped.forEach { (category, items) ->
                item {
                    CategoryHeader(category)
                    Spacer(Modifier.height(8.dp))
                }
                items(items, key = { it.id }) { item ->
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = {
                            if (it == SwipeToDismissBoxValue.EndToStart) {
                                viewModel.onDeleteClick(item)
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
                        LibraryItemRow(
                            item = item,
                            onEdit = { viewModel.onEditClick(item) },
                            onToggle = { viewModel.toggleEnabled(item) },
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                }
                item { Spacer(Modifier.height(12.dp)) }
            }
        }

        FloatingActionButton(
            onClick = { viewModel.onAddClick() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
            containerColor = AccentGold,
            contentColor = DarkBackground,
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add item")
        }
    }
}

@Composable
private fun CategoryHeader(category: Category) {
    Text(
        text = category.displayName.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        letterSpacing = 2.sp,
        color = TextSecondary,
        modifier = Modifier.padding(vertical = 4.dp),
    )
}

@Composable
private fun LibraryItemRow(item: Item, onEdit: () -> Unit, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurface)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            Modifier
                .weight(1f)
                .clickable { onEdit() },
        ) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.titleMedium,
                color = if (item.enabled) TextPrimary else TextSecondary,
            )
            Spacer(Modifier.height(4.dp))
            RarityBadge(item.rarity)
        }
        Switch(
            checked = item.enabled,
            onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = AccentGold,
                checkedTrackColor = AccentGold.copy(alpha = 0.4f),
            ),
        )
    }
}
