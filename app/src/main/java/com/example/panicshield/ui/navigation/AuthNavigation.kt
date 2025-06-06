package com.example.panicshield.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.panicshield.ui.screen.auth.LoginScreen
import com.example.panicshield.ui.screen.auth.RegisterScreen

sealed class AuthScreen(val route: String) {
    object Login : AuthScreen("login")
    object Register : AuthScreen("register")
}

@Composable
fun AuthNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = AuthScreen.Login.route
    ) {
        composable(AuthScreen.Login.route) {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(AuthScreen.Register.route)
                },
                onLoginSuccess = {
                    // Este callback se maneja en AppNavigation a través del LaunchedEffect
                    // que observa uiState.isLoggedIn del AuthViewModel
                }
            )
        }

        composable(AuthScreen.Register.route) {
            RegisterScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }
    }
}