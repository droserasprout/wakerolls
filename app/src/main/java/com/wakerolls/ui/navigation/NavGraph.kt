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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.wakerolls.ui.library.ItemsScreen
import com.wakerolls.ui.library.ScenariosScreen
import com.wakerolls.ui.roll.RollScreen
import com.wakerolls.ui.settings.SettingsScreen
import com.wakerolls.ui.theme.DarkSurface
import kotlinx.coroutines.launch

data class Page(val label: String, val icon: ImageVector)

val pages = listOf(
    Page("Roll", Icons.Filled.Star),
    Page("Items", Icons.Filled.List),
    Page("Scenarios", Icons.Filled.PlayArrow),
    Page("Settings", Icons.Filled.Settings),
)

@Composable
fun WakerollsNavGraph() {
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
