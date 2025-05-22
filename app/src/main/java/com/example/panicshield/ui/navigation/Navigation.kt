package com.example.panicshield.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.panicshield.ui.screen.EmergencyContactsScreen
import com.example.panicshield.ui.screen.HistoryScreen
import com.example.panicshield.ui.screen.LoginScreen
import com.example.panicshield.ui.screen.HomeScreen
import com.example.panicshield.ui.screen.MapScreen
import com.example.panicshield.utils.NavigationRoutes

@Composable
fun Navigation() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = NavigationRoutes.LOGIN
    ) {

        // Pantalla de Login
        composable(NavigationRoutes.LOGIN) {
            LoginScreen(
                onNavigateToHome = {
                    navController.navigate(NavigationRoutes.HOME) {
                        popUpTo(NavigationRoutes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        // Pantalla de Home
        composable(NavigationRoutes.HOME) {
            HomeScreen(
                onNavigateToMap = { navController.navigate(NavigationRoutes.MAP) },
                onNavigateToEmergencyContacts = { navController.navigate(NavigationRoutes.EMERGENCY_CONTACTS) },
                onNavigateToHistory = { navController.navigate(NavigationRoutes.HISTORY) }
            )
        }

        // Pantalla del Mapa
        composable(NavigationRoutes.MAP) {
            MapScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Pantalla de Contactos de Emergencia
        composable(NavigationRoutes.EMERGENCY_CONTACTS) {
            EmergencyContactsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Pantalla de Historial
        composable(NavigationRoutes.HISTORY) {
            HistoryScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}