package com.wakerolls.ui.roll

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wakerolls.domain.model.Item
import com.wakerolls.domain.model.Rarity
import com.wakerolls.ui.theme.*
import kotlinx.coroutines.launch

private const val LONG_PRESS_DURATION_MS = 3000

@Composable
fun RollScreen(viewModel: RollViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

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

        if (state.hasRolled) {
            Text(
                text = "${state.rerollsLeft} reroll${if (state.rerollsLeft != 1) "s" else ""} left",
                style = MaterialTheme.typography.bodyMedium,
                color = if (state.rerollsLeft > 0) AccentTeal else TextSecondary,
            )
        } else {
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
                    RollCard(
                        label = result.label,
                        item = result.item,
                        canReroll = state.rerollsLeft > 0,
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
        } else {
            LongPressButton(
                text = "Reroll all",
                enabled = state.rerollsLeft > 0,
                onComplete = { viewModel.rollAll() },
            )
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun LongPressButton(
    text: String,
    enabled: Boolean,
    onComplete: () -> Unit,
) {
    val progress = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val bgColor = if (enabled) AccentGold else TextSecondary

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor.copy(alpha = 0.2f))
            .then(
                if (enabled) {
                    Modifier.pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                val job = scope.launch {
                                    progress.animateTo(1f, tween(LONG_PRESS_DURATION_MS, easing = LinearEasing))
                                }
                                val released = tryAwaitRelease()
                                job.cancel()
                                if (progress.value >= 1f) {
                                    onComplete()
                                }
                                progress.snapTo(0f)
                            },
                        )
                    }
                } else Modifier
            )
            .drawBehind {
                drawRect(
                    color = bgColor,
                    topLeft = Offset.Zero,
                    size = Size(size.width * progress.value, size.height),
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = if (enabled) text else "No rerolls left",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = DarkBackground,
        )
    }
}

@Composable
fun RollCard(label: String, item: Item?, canReroll: Boolean = false, onReroll: (() -> Unit)? = null) {
    val rarityColor = item?.rarity?.color() ?: TextSecondary
    val progress = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val showProgress = canReroll && onReroll != null && item != null

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
            .then(
                if (showProgress) {
                    Modifier.pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                val job = scope.launch {
                                    progress.animateTo(1f, tween(LONG_PRESS_DURATION_MS, easing = LinearEasing))
                                }
                                val released = tryAwaitRelease()
                                job.cancel()
                                if (progress.value >= 1f) {
                                    onReroll!!()
                                }
                                progress.snapTo(0f)
                            },
                        )
                    }
                } else Modifier
            )
            .drawBehind {
                if (progress.value > 0f) {
                    drawRect(
                        color = rarityColor.copy(alpha = 0.15f),
                        topLeft = Offset.Zero,
                        size = Size(size.width * progress.value, size.height),
                    )
                }
            },
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
                    text = "\u2014",
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
