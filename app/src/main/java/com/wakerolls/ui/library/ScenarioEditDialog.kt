package com.wakerolls.ui.library

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wakerolls.domain.model.Scenario
import com.wakerolls.domain.model.ScenarioSlot
import com.wakerolls.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScenarioEditDialog(
    scenario: Scenario,
    categories: List<String>,
    onSave: (Scenario) -> Unit,
    onDelete: (() -> Unit)?,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf(scenario.name) }
    var slots by remember { mutableStateOf(scenario.slots) }

    val canSave = name.isNotBlank() && slots.isNotEmpty() && slots.all { it.category.isNotBlank() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (scenario.id == 0L) "Add Scenario" else "Edit Scenario",
                color = TextPrimary,
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
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

                if (slots.isNotEmpty()) {
                    Text(
                        "Slots",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                    )
                    slots.forEachIndexed { index, slot ->
                        SlotRow(
                            slot = slot,
                            categories = categories,
                            onCategoryChange = { newCat ->
                                slots = slots.toMutableList().also { it[index] = slot.copy(category = newCat) }
                            },
                            onCountChange = { delta ->
                                val newCount = (slot.count + delta).coerceIn(1, 5)
                                slots = slots.toMutableList().also { it[index] = slot.copy(count = newCount) }
                            },
                            onDelete = {
                                slots = slots.toMutableList().also { it.removeAt(index) }
                            },
                        )
                    }
                }

                TextButton(
                    onClick = {
                        slots = slots + ScenarioSlot(category = "", count = 1)
                    },
                ) {
                    Text("+ Add slot", color = AccentTeal)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(scenario.copy(name = name.trim(), slots = slots))
                },
                enabled = canSave,
            ) {
                Text("Save", color = if (canSave) AccentGold else TextSecondary)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SlotRow(
    slot: ScenarioSlot,
    categories: List<String>,
    onCategoryChange: (String) -> Unit,
    onCountChange: (Int) -> Unit,
    onDelete: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val suggestions = categories.filter {
        it.contains(slot.category, ignoreCase = true) && it != slot.category
    }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ExposedDropdownMenuBox(
                expanded = expanded && suggestions.isNotEmpty(),
                onExpandedChange = { expanded = it },
                modifier = Modifier.weight(1f),
            ) {
                OutlinedTextField(
                    value = slot.category,
                    onValueChange = {
                        onCategoryChange(it)
                        expanded = true
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
                    expanded = expanded && suggestions.isNotEmpty(),
                    onDismissRequest = { expanded = false },
                    containerColor = DarkSurfaceVariant,
                ) {
                    suggestions.forEach { suggestion ->
                        DropdownMenuItem(
                            text = { Text(suggestion, color = TextPrimary) },
                            onClick = {
                                onCategoryChange(suggestion)
                                expanded = false
                            },
                        )
                    }
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Close, contentDescription = "Remove slot", tint = AccentCoral)
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Count:", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            IconButton(
                onClick = { onCountChange(-1) },
                enabled = slot.count > 1,
            ) {
                Text(
                    "−",
                    style = MaterialTheme.typography.titleLarge,
                    color = if (slot.count > 1) TextPrimary else TextSecondary,
                )
            }
            Text(
                text = slot.count.toString(),
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
            )
            IconButton(
                onClick = { onCountChange(1) },
                enabled = slot.count < 5,
            ) {
                Text(
                    "+",
                    style = MaterialTheme.typography.titleLarge,
                    color = if (slot.count < 5) TextPrimary else TextSecondary,
                )
            }
        }
        HorizontalDivider(color = DarkSurfaceVariant)
    }
}
