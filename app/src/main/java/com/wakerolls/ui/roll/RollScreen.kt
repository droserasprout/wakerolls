package com.wakerolls.ui.roll

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.graphicsLayer
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun RollScreen(viewModel: RollViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val canRerollAll = state.allowRerolls && state.rerollsLeft > 0
    val isUnlimited = state.rerollsPerDay == 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
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

        if (state.hasRolled && state.allowRerolls && !isUnlimited) {
            Text(
                text = "${state.rerollsLeft} reroll${if (state.rerollsLeft != 1) "s" else ""} left",
                style = MaterialTheme.typography.bodyMedium,
                color = if (state.rerollsLeft > 0) AccentTeal else TextSecondary,
            )
        } else if (!state.hasRolled) {
            Text(
                text = "Tap roll to discover your day",
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        // Scenario selector dropdown
        if (state.scenarios.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            ScenarioDropdown(
                scenarios = state.scenarios,
                selectedId = state.selectedScenarioId,
                onSelect = { viewModel.selectScenario(it) },
            )
        }

        Spacer(Modifier.height(24.dp))

        // Roll results
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (state.results.isEmpty()) {
                val scenario = state.scenarios.find { it.id == state.selectedScenarioId }
                scenario?.slots?.forEach { slot ->
                    if (slot.count == 1) {
                        RollCard(label = slot.category, item = null)
                    } else {
                        repeat(slot.count) { i ->
                            RollCard(label = "${slot.category} #${i + 1}", item = null)
                        }
                    }
                }
            } else {
                state.results.forEachIndexed { index, result ->
                    val showReroll = state.allowRerolls && state.allowPartialRerolls
                            && state.rerollsLeft > 0 && result.item != null
                    AnimatedRollCard(
                        label = result.label,
                        item = result.item,
                        index = index,
                        showReroll = showReroll,
                        onReroll = { viewModel.reroll(index) },
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Roll / Reroll button
        if (!state.hasRolled) {
            Button(
                onClick = { viewModel.rollAll() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentGold),
                shape = RoundedCornerShape(16.dp),
                enabled = state.selectedScenarioId != null,
            ) {
                Text("Roll the day", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkBackground)
            }
        } else if (state.allowRerolls) {
            Button(
                onClick = { viewModel.rollAll() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentGold,
                    disabledContainerColor = AccentGold.copy(alpha = 0.2f),
                ),
                shape = RoundedCornerShape(16.dp),
                enabled = canRerollAll,
            ) {
                Text(
                    text = if (canRerollAll) "Reroll all" else "No rerolls left",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (canRerollAll) DarkBackground else TextSecondary,
                )
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun AnimatedRollCard(
    label: String,
    item: Item?,
    index: Int,
    showReroll: Boolean,
    onReroll: () -> Unit,
) {
    val offsetX = remember { Animatable(300f) }
    val alpha = remember { Animatable(0f) }
    val scale = remember { Animatable(0.6f) }

    LaunchedEffect(Unit) {
        delay(index * 80L)
        launch { offsetX.animateTo(0f, tween(400, easing = LinearOutSlowInEasing)) }
        launch { alpha.animateTo(1f, tween(300)) }
        launch {
            scale.animateTo(
                1f,
                keyframes {
                    durationMillis = 500
                    0.6f at 0 using FastOutSlowInEasing
                    1.08f at 300 using FastOutSlowInEasing
                    1f at 500 using FastOutSlowInEasing
                },
            )
        }
    }

    Box(
        modifier = Modifier
            .offset(x = offsetX.value.dp)
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            }
            .then(Modifier.fillMaxWidth()),
    ) {
        RollCard(
            label = label,
            item = item,
            showReroll = showReroll,
            onReroll = onReroll,
            alpha = alpha.value,
        )
    }
}

@Composable
fun RollCard(
    label: String,
    item: Item?,
    showReroll: Boolean = false,
    onReroll: (() -> Unit)? = null,
    alpha: Float = 1f,
) {
    val rarityColor = item?.rarity?.color() ?: TextSecondary
    val glowLevel = item?.rarity?.glowLevel() ?: 0f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(DarkSurface)
            .then(
                if (glowLevel > 0f) {
                    val edgeAlpha = glowLevel * 0.12f * alpha
                    val glowInset = 20f // same spread for all rarities
                    Modifier.drawWithContent {
                        drawContent()
                        // Top edge glow
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(rarityColor.copy(alpha = edgeAlpha), Color.Transparent),
                                startY = 0f,
                                endY = glowInset * density,
                            ),
                        )
                        // Bottom edge glow
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color.Transparent, rarityColor.copy(alpha = edgeAlpha)),
                                startY = size.height - glowInset * density,
                                endY = size.height,
                            ),
                        )
                        // Left edge glow
                        drawRect(
                            brush = Brush.horizontalGradient(
                                colors = listOf(rarityColor.copy(alpha = edgeAlpha), Color.Transparent),
                                startX = 0f,
                                endX = glowInset * density,
                            ),
                        )
                        // Right edge glow
                        drawRect(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color.Transparent, rarityColor.copy(alpha = edgeAlpha)),
                                startX = size.width - glowInset * density,
                                endX = size.width,
                            ),
                        )
                    }
                } else Modifier
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(listOf(rarityColor.copy(alpha = 0.6f * alpha), rarityColor.copy(alpha = 0.1f * alpha))),
                shape = RoundedCornerShape(20.dp),
            ),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            // Top row: category label (left) + rarity badge (right)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = label.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    letterSpacing = 2.sp,
                    color = TextSecondary,
                )
                if (item != null) {
                    RarityBadge(item.rarity)
                }
            }

            // Item name + reroll button
            if (item != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f),
                    )
                    if (showReroll && onReroll != null) {
                        IconButton(
                            onClick = onReroll,
                            modifier = Modifier.size(36.dp),
                        ) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "Reroll",
                                tint = TextSecondary,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                }
            } else {
                Text(
                    text = "\u2014",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextSecondary,
                )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScenarioDropdown(
    scenarios: List<com.wakerolls.domain.model.Scenario>,
    selectedId: Long?,
    onSelect: (Long) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = scenarios.find { it.id == selectedId }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
    ) {
        OutlinedTextField(
            value = selected?.name ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Scenario") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AccentGold,
                focusedLabelColor = AccentGold,
            ),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = DarkSurfaceVariant,
        ) {
            scenarios.forEach { scenario ->
                DropdownMenuItem(
                    text = { Text(scenario.name, color = TextPrimary) },
                    onClick = {
                        onSelect(scenario.id)
                        expanded = false
                    },
                )
            }
        }
    }
}

fun Rarity.color(): Color = when (this) {
    Rarity.COMMON -> RarityCommon
    Rarity.UNCOMMON -> RarityUncommon
    Rarity.RARE -> RarityRare
    Rarity.LEGENDARY -> RarityLegendary
}

/** Returns glow intensity (0 = none). Higher rarity = brighter glow, same spread. */
fun Rarity.glowLevel(): Float = when (this) {
    Rarity.COMMON -> 0f
    Rarity.UNCOMMON -> 1f
    Rarity.RARE -> 2f
    Rarity.LEGENDARY -> 3f
}

