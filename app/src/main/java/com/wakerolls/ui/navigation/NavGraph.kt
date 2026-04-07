package com.wakerolls.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.wakerolls.ui.library.ItemsScreen
import com.wakerolls.ui.library.ScenariosScreen
import com.wakerolls.ui.roll.RollScreen
import com.wakerolls.ui.settings.SettingsScreen
import com.wakerolls.ui.theme.DarkSurface
import com.wakerolls.ui.welcome.WelcomeScreen
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private val KEY_WELCOME_SEEN = booleanPreferencesKey("welcome_seen")

data class Page(val label: String, val icon: ImageVector)

val pages = listOf(
    Page("Roll", Icons.Filled.Star),
    Page("Items", Icons.Filled.List),
    Page("Scenarios", Icons.Filled.PlayArrow),
    Page("Settings", Icons.Filled.Settings),
)

@Composable
fun WakerollsNavGraph(dataStore: DataStore<Preferences>) {
    var showWelcome by remember { mutableStateOf<Boolean?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        showWelcome = !(dataStore.data.first()[KEY_WELCOME_SEEN] ?: false)
    }

    when (showWelcome) {
        null -> {} // loading
        true -> WelcomeScreen(onGetStarted = {
            scope.launch {
                dataStore.edit { it[KEY_WELCOME_SEEN] = true }
                showWelcome = false
            }
        })
        false -> MainPager()
    }
}

@Composable
private fun MainPager() {
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = DarkSurface) {
                pages.forEachIndexed { index, page ->
                    NavigationBarItem(
                        icon = { Icon(page.icon, contentDescription = page.label) },
                        label = { Text(page.label) },
                        selected = pagerState.currentPage == index,
                        onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                    )
                }
            }
        }
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.padding(innerPadding),
            beyondViewportPageCount = 1,
        ) { page ->
            when (page) {
                0 -> RollScreen()
                1 -> ItemsScreen()
                2 -> ScenariosScreen()
                3 -> SettingsScreen()
            }
        }
    }
}
