package com.wakerolls.ui.library

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wakerolls.domain.model.Item
import com.wakerolls.domain.model.Rarity
import com.wakerolls.ui.roll.color
import com.wakerolls.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ItemEditDialog(
    item: Item,
    categories: List<String>,
    onSave: (Item) -> Unit,
    onDelete: (() -> Unit)?,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf(item.name) }
    var category by remember { mutableStateOf(item.category) }
    var rarity by remember { mutableStateOf(item.rarity) }
    var categoryExpanded by remember { mutableStateOf(false) }

    val suggestions = categories.filter {
        it.contains(category, ignoreCase = true) && it != category
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (item.id == 0L) "Add Item" else "Edit Item",
                color = TextPrimary,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentGold,
                        cursorColor = AccentGold,
                        focusedLabelColor = AccentGold,
                    ),
                )

                ExposedDropdownMenuBox(
                    expanded = categoryExpanded && suggestions.isNotEmpty(),
                    onExpandedChange = { categoryExpanded = it },
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {
                            category = it
                            categoryExpanded = true
                        },
                        label = { Text("Category") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryEditable),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentGold,
                            cursorColor = AccentGold,
                            focusedLabelColor = AccentGold,
                        ),
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded && suggestions.isNotEmpty(),
                        onDismissRequest = { categoryExpanded = false },
                        containerColor = DarkSurfaceVariant,
                    ) {
                        suggestions.forEach { suggestion ->
                            DropdownMenuItem(
                                text = { Text(suggestion, color = TextPrimary) },
                                onClick = {
                                    category = suggestion
                                    categoryExpanded = false
                                },
                            )
                        }
                    }
                }

                Text("Rarity", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Rarity.entries.forEach { r ->
                        val rarityColor = r.color()
                        FilterChip(
                            selected = rarity == r,
                            onClick = { rarity = r },
                            label = { Text(r.name) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = rarityColor.copy(alpha = 0.15f),
                                selectedLabelColor = rarityColor,
                                labelColor = TextSecondary,
                            ),
                            border = if (rarity == r) {
                                FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = true,
                                    borderColor = rarityColor.copy(alpha = 0.5f),
                                    selectedBorderColor = rarityColor.copy(alpha = 0.5f),
                                )
                            } else {
                                FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = false,
                                )
                            },
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(item.copy(name = name.trim(), category = category.trim(), rarity = rarity)) },
                enabled = name.isNotBlank() && category.isNotBlank(),
            ) {
                Text("Save", color = if (name.isNotBlank() && category.isNotBlank()) AccentGold else TextSecondary)
            }
        },
        dismissButton = {
            Row {
                if (onDelete != null) {
                    TextButton(onClick = onDelete) {
                        Text("Delete", color = AccentCoral)
                    }
                }
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        },
        containerColor = DarkSurface,
    )
}
