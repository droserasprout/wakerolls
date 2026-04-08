package com.wakerolls.ui.library

import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.graphics.Color
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
    onResetStats: (() -> Unit)? = null,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf(item.name) }
    var category by remember { mutableStateOf(item.category) }
    var rarity by remember { mutableStateOf(item.rarity) }
    var categoryExpanded by remember { mutableStateOf(false) }

    val suggestions = categories.filter {
        it.contains(category, ignoreCase = true) && it != category
    }
    val canSave = name.isNotBlank() && category.isNotBlank()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 24.dp),
            shape = RoundedCornerShape(24.dp),
            color = DarkSurface,
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    if (item.id == 0L) "Add Item" else "Edit Item",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary,
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentGold,
                        unfocusedBorderColor = TextSecondary.copy(alpha = 0.5f),
                        cursorColor = AccentGold,
                        focusedLabelColor = AccentGold,
                        unfocusedLabelColor = TextSecondary,
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
                            unfocusedBorderColor = TextSecondary.copy(alpha = 0.5f),
                            cursorColor = AccentGold,
                            focusedLabelColor = AccentGold,
                            unfocusedLabelColor = TextSecondary,
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
                            label = { Text(r.name, color = if (rarity == r) rarityColor else TextSecondary) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = rarityColor.copy(alpha = 0.15f),
                                containerColor = Color.Transparent,
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = rarity == r,
                                borderColor = rarityColor.copy(alpha = 0.3f),
                                selectedBorderColor = rarityColor.copy(alpha = 0.5f),
                            ),
                        )
                    }
                }

                if (item.id != 0L) {
                    Text("Statistics", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text("Rolled", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            Text(item.rolledCount.toString(), style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                        }
                        Column {
                            Text("Completed", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            Text(item.completedCount.toString(), style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                        }
                        Spacer(Modifier.weight(1f))
                        if (onResetStats != null && (item.rolledCount > 0 || item.completedCount > 0)) {
                            TextButton(onClick = onResetStats) {
                                Text("Reset", color = AccentCoral)
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    if (onDelete != null) {
                        TextButton(onClick = onDelete) {
                            Text("Delete", color = AccentCoral)
                        }
                        Spacer(Modifier.weight(1f))
                    }
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    TextButton(
                        onClick = { onSave(item.copy(name = name.trim(), category = category.trim(), rarity = rarity)) },
                        enabled = canSave,
                    ) {
                        Text("Save", color = if (canSave) AccentGold else TextSecondary)
                    }
                }
            }
        }
    }
}
