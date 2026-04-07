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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wakerolls.domain.model.Item
import com.wakerolls.ui.roll.RarityBadge
import com.wakerolls.ui.theme.*

@Composable
fun ItemsScreen(viewModel: LibraryViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
    ) {
        Column {
            Spacer(Modifier.height(24.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Items",
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.weight(1f),
                )
                TextButton(onClick = { viewModel.toggleSort() }) {
                    Text(
                        text = if (state.sortMode == ItemSortMode.AZ) "A-Z" else "Rarity",
                        color = AccentGold,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            LazyColumn(contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)) {
                state.grouped.forEach { (category, items) ->
                    item {
                        CategoryHeader(category)
                        Spacer(Modifier.height(8.dp))
                    }
                    items(items, key = { it.id }) { item ->
                        LibraryItemRow(
                            item = item,
                            onEdit = { viewModel.onEditClick(item) },
                            onToggle = { viewModel.toggleEnabled(item) },
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                    item { Spacer(Modifier.height(12.dp)) }
                }
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

    // Item Edit / Add dialog
    state.editingItem?.let { editItem ->
        ItemEditDialog(
            item = editItem,
            categories = state.categories,
            onSave = { viewModel.onSaveItem(it) },
            onDelete = if (editItem.id != 0L) {
                { viewModel.onDeleteClick(editItem); viewModel.onDismissEdit() }
            } else null,
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
}

@Composable
private fun CategoryHeader(category: String) {
    Text(
        text = category.uppercase(),
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
            .clickable { onEdit() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
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
