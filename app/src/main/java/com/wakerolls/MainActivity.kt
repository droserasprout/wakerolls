package com.wakerolls

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.wakerolls.ui.navigation.WakerollsNavGraph
import com.wakerolls.ui.theme.WakerollsTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WakerollsTheme {
                WakerollsNavGraph()
            }
        }
    }
}
