package com.wakerolls.ui.roll

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wakerolls.domain.model.Item
import com.wakerolls.domain.model.Rarity
import com.wakerolls.ui.theme.*

@Composable
fun RollScreen(padding: PaddingValues, viewModel: RollViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(padding)
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(32.dp))
        Text(
            text = "Today's Roll",
            style = MaterialTheme.typography.headlineLarge,
            color = TextPrimary,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Tap roll to discover your day",
            style = MaterialTheme.typography.bodyMedium,
        )

        // Scenario selector chips
        if (state.scenarios.size > 1) {
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                state.scenarios.forEach { scenario ->
                    FilterChip(
                        selected = scenario.id == state.selectedScenarioId,
                        onClick = { viewModel.selectScenario(scenario.id) },
                        label = { Text(scenario.name) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AccentGold.copy(alpha = 0.2f),
                            selectedLabelColor = AccentGold,
                        ),
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // Roll results — scrollable if many
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (state.results.isEmpty()) {
                // Show empty placeholders based on selected scenario slots
                val scenario = state.scenarios.find { it.id == state.selectedScenarioId }
                scenario?.slots?.forEach { slot ->
                    if (slot.count == 1) {
                        RollCard(label = slot.category.displayName, item = null)
                    } else {
                        repeat(slot.count) { i ->
                            RollCard(label = "${slot.category.displayName} #${i + 1}", item = null)
                        }
                    }
                }
            } else {
                state.results.forEachIndexed { index, result ->
                    RollCard(
                        label = result.label,
                        item = result.item,
                        onClick = { viewModel.reroll(index) },
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = { viewModel.rollAll() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentGold),
            shape = RoundedCornerShape(16.dp),
            enabled = state.selectedScenarioId != null,
        ) {
            Text(
                text = "Roll the day",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = DarkBackground,
            )
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
fun RollCard(label: String, item: Item?, onClick: (() -> Unit)? = null) {
    val rarityColor = item?.rarity?.color() ?: TextSecondary

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(DarkSurface)
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(listOf(rarityColor.copy(alpha = 0.6f), rarityColor.copy(alpha = 0.1f))),
                shape = RoundedCornerShape(20.dp),
            )
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = 2.sp,
                color = TextSecondary,
            )
            if (item != null) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary,
                )
                RarityBadge(item.rarity)
            } else {
                Text(
                    text = "—",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextSecondary,
                )
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun RarityBadge(rarity: Rarity) {
    val color = rarity.color()
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.15f),
    ) {
        Text(
            text = rarity.name,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
        )
    }
}

fun Rarity.color(): Color = when (this) {
    Rarity.COMMON -> RarityCommon
    Rarity.UNCOMMON -> RarityUncommon
    Rarity.RARE -> RarityRare
}
