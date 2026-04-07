package com.wakerolls.ui.library

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wakerolls.domain.model.Category
import com.wakerolls.domain.model.Item
import com.wakerolls.domain.model.Rarity
import com.wakerolls.ui.theme.*

@Composable
fun ItemEditDialog(
    item: Item,
    onSave: (Item) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf(item.name) }
    var category by remember { mutableStateOf(item.category) }
    var rarity by remember { mutableStateOf(item.rarity) }

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

                Text("Category", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Category.entries.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat.displayName) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AccentGold.copy(alpha = 0.2f),
                                selectedLabelColor = AccentGold,
                            ),
                        )
                    }
                }

                Text("Rarity", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Rarity.entries.forEach { r ->
                        FilterChip(
                            selected = rarity == r,
                            onClick = { rarity = r },
                            label = { Text(r.name) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AccentGold.copy(alpha = 0.2f),
                                selectedLabelColor = AccentGold,
                            ),
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(item.copy(name = name.trim(), category = category, rarity = rarity)) },
                enabled = name.isNotBlank(),
            ) {
                Text("Save", color = if (name.isNotBlank()) AccentGold else TextSecondary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        containerColor = DarkSurface,
    )
}
