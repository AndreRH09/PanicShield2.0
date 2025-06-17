package com.example.panicshield.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.panicshield.ui.screen.auth.AuthViewModel


sealed class AppScreen(val route: String) {
    object Auth : AppScreen("auth")
    object Main : AppScreen("main")
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by authViewModel.uiState.collectAsState()

    // Observar cambios en el estado de autenticación
    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) {
            navController.navigate(AppScreen.Main.route) {
                popUpTo(AppScreen.Auth.route) {
                    inclusive = true
                }
            }
        } else {
            // Solo navegar a Auth si no estamos ya allí
            if (navController.currentDestination?.route != AppScreen.Auth.route) {
                navController.navigate(AppScreen.Auth.route) {
                    popUpTo(0) {
                        inclusive = true
                    }
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = if (uiState.isLoggedIn) AppScreen.Main.route else AppScreen.Auth.route
    ) {
        composable(AppScreen.Auth.route) {
            AuthNavigation()
        }

        composable(AppScreen.Main.route) {
            MainNavigation(
                onLogout = {
                    authViewModel.logout()
                }
            )
        }
    }
}