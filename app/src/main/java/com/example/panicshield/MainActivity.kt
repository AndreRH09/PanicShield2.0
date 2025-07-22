package com.example.panicshield

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.panicshield.ui.navigation.AppNavigation
import com.example.panicshield.ui.screen.settings.SettingsViewModel
import com.example.panicshield.ui.theme.PanicShieldTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Asegura que el sistema inserte padding por la status bar
        WindowCompat.setDecorFitsSystemWindows(window, true)

        setContent {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val isDarkTheme by settingsViewModel.darkModeEnabled.collectAsState()

            // Configurar status y nav bar
            SideEffect {
                val window = this@MainActivity.window
                val controller = WindowInsetsControllerCompat(window, window.decorView)

                val backgroundColor = if (isDarkTheme) 0xFF07254A.toInt() else 0xFFFFFFFF.toInt()

                window.statusBarColor = backgroundColor
                window.navigationBarColor = backgroundColor

                controller.isAppearanceLightStatusBars = !isDarkTheme
                controller.isAppearanceLightNavigationBars = !isDarkTheme
            }

            PanicShieldTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}
