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
import com.wakerolls.domain.model.Category
import com.wakerolls.domain.model.Scenario
import com.wakerolls.domain.model.ScenarioSlot
import com.wakerolls.ui.theme.*

@Composable
fun ScenarioEditDialog(
    scenario: Scenario,
    onSave: (Scenario) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf(scenario.name) }
    var slots by remember { mutableStateOf(scenario.slots) }

    val usedCategories = slots.map { it.category }.toSet()
    val unusedCategories = Category.entries.filter { it !in usedCategories }
    val canAddSlot = unusedCategories.isNotEmpty()
    val canSave = name.isNotBlank() && slots.isNotEmpty()

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
                            availableCategories = Category.entries.filter {
                                it == slot.category || it !in usedCategories
                            },
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

                if (canAddSlot) {
                    TextButton(
                        onClick = {
                            val nextCategory = unusedCategories.first()
                            slots = slots + ScenarioSlot(category = nextCategory, count = 1)
                        },
                    ) {
                        Text("+ Add slot", color = AccentTeal)
                    }
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
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        containerColor = DarkSurface,
    )
}

@Composable
private fun SlotRow(
    slot: ScenarioSlot,
    availableCategories: List<Category>,
    onCategoryChange: (Category) -> Unit,
    onCountChange: (Int) -> Unit,
    onDelete: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            availableCategories.forEach { cat ->
                FilterChip(
                    selected = slot.category == cat,
                    onClick = { onCategoryChange(cat) },
                    label = { Text(cat.displayName) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AccentGold.copy(alpha = 0.2f),
                        selectedLabelColor = AccentGold,
                    ),
                )
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
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
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Close, contentDescription = "Remove slot", tint = AccentCoral)
            }
        }
        HorizontalDivider(color = DarkSurfaceVariant)
    }
}
