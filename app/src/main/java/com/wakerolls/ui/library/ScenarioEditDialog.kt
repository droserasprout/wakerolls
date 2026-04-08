package com.wakerolls.ui.library

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
                    if (scenario.id == 0L) "Add Scenario" else "Edit Scenario",
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
                        onClick = { onSave(scenario.copy(name = name.trim(), slots = slots)) },
                        enabled = canSave,
                    ) {
                        Text("Save", color = if (canSave) AccentGold else TextSecondary)
                    }
                }
            }
        }
    }
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

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
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
                    unfocusedBorderColor = TextSecondary.copy(alpha = 0.5f),
                    cursorColor = AccentGold,
                    focusedLabelColor = AccentGold,
                    unfocusedLabelColor = TextSecondary,
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
        IconButton(
            onClick = { onCountChange(-1) },
            enabled = slot.count > 1,
            modifier = Modifier.size(32.dp),
        ) {
            Text("\u2212", style = MaterialTheme.typography.titleMedium,
                color = if (slot.count > 1) TextPrimary else TextSecondary)
        }
        Text(
            text = slot.count.toString(),
            style = MaterialTheme.typography.titleMedium,
            color = AccentGold,
            modifier = Modifier.width(24.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
        IconButton(
            onClick = { onCountChange(1) },
            enabled = slot.count < 5,
            modifier = Modifier.size(32.dp),
        ) {
            Text("+", style = MaterialTheme.typography.titleMedium,
                color = if (slot.count < 5) TextPrimary else TextSecondary)
        }
        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Filled.Close, contentDescription = "Remove slot", tint = AccentCoral, modifier = Modifier.size(18.dp))
        }
    }
}
