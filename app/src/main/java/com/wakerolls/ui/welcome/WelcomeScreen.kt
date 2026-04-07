package com.wakerolls.ui.welcome

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wakerolls.ui.theme.*

@Composable
fun WelcomeScreen(onGetStarted: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(48.dp))

        Text(
            text = "Welcome to Wakerolls",
            style = MaterialTheme.typography.headlineLarge,
            color = AccentGold,
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Randomize your day with style",
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary,
        )

        Spacer(Modifier.height(32.dp))

        ConceptCard(
            title = "Items",
            description = "Things you want to randomize \u2014 meals, activities, workouts, anything. Each item belongs to a category and has a rarity that affects how often it\u2019s picked.",
        )
        Spacer(Modifier.height(16.dp))
        ConceptCard(
            title = "Scenarios",
            description = "Templates that define what to roll. A scenario has slots \u2014 pick a category and how many items to draw from it. Mix and match to build your perfect random day.",
        )
        Spacer(Modifier.height(16.dp))
        ConceptCard(
            title = "Roll",
            description = "Hit the button and get your randomized picks for the day. Results are saved between sessions. Use rerolls to shake things up if you\u2019re not feeling it.",
        )
        Spacer(Modifier.height(16.dp))
        ConceptCard(
            title = "Settings",
            description = "Control rerolls, adjust rarity weights, toggle animations, and set up daily reminders to roll.",
        )

        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(24.dp))

        Button(
            onClick = onGetStarted,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentGold),
            shape = RoundedCornerShape(16.dp),
        ) {
            Text("Get started", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkBackground)
        }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun ConceptCard(title: String, description: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = DarkSurface,
    ) {
        Column(Modifier.padding(20.dp)) {
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = TextPrimary)) {
                        append(title)
                    }
                    append("  ")
                    withStyle(SpanStyle(color = TextSecondary)) {
                        append(description)
                    }
                },
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}
