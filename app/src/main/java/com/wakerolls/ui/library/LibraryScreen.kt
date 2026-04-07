package com.wakerolls.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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

@Composable
fun LibraryScreen(padding: PaddingValues, viewModel: LibraryViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(padding),
    ) {
        Spacer(Modifier.height(24.dp))
        Text(
            text = "Library",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(horizontal = 20.dp),
        )
        Spacer(Modifier.height(16.dp))
        LazyColumn(contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)) {
            state.grouped.forEach { (category, items) ->
                item {
                    CategoryHeader(category)
                    Spacer(Modifier.height(8.dp))
                }
                items(items, key = { it.id }) { item ->
                    LibraryItemRow(item = item, onToggle = { viewModel.toggleEnabled(item) })
                    Spacer(Modifier.height(8.dp))
                }
                item { Spacer(Modifier.height(12.dp)) }
            }
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
private fun LibraryItemRow(item: Item, onToggle: () -> Unit) {
    val rarityColor = item.rarity.color()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurface)
            .clickable { onToggle() }
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
