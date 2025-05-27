package com.example.panicshield.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.panicshield.ui.screen.auth.AuthViewModel
import com.example.panicshield.ui.screen.auth.LoginScreen
import com.example.panicshield.ui.screen.auth.RegisterScreen
import com.example.panicshield.ui.screen.home.HomeScreen
import com.example.panicshield.ui.screen.contacts.EmergencyContactScreen
import com.example.panicshield.ui.screen.config.ConfigurationScreen
import com.example.panicshield.ui.screen.history.HistoryScreen
import com.example.panicshield.ui.screen.map.MapScreen

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Main : Screen("main")

    // Pantallas principales con bottom navigation
    object Home : Screen("home")
    object Map : Screen("map")
    object Contacts : Screen("contacts")
    object History : Screen("history")
    object Config : Screen("config")
}

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Home : BottomNavItem(Screen.Home.route, "Inicio", Icons.Default.Home)
    object Map : BottomNavItem(Screen.Map.route, "Mapa", Icons.Default.LocationOn)
    object Contacts : BottomNavItem(Screen.Contacts.route, "Contactos", Icons.Default.Person)
    object History : BottomNavItem(Screen.History.route, "Historial", Icons.Default.History)
    object Config : BottomNavItem(Screen.Config.route, "Ajustes", Icons.Default.Settings)
}

@Composable
fun Navigation(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val authState by authViewModel.authState.collectAsState()

    NavHost(
        navController = navController,
        startDestination = if (authState.isAuthenticated) Screen.Main.route else Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                viewModel = authViewModel
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                viewModel = authViewModel
            )
        }

        composable(Screen.Main.route) {
            MainScreenWithBottomNav(
                onSignOut = {
                    authViewModel.signOut()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Main.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenWithBottomNav(
    onSignOut: () -> Unit
) {
    val bottomNavController = rememberNavController()
    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val bottomNavItems = listOf(
        BottomNavItem.Home,
        BottomNavItem.Map,
        BottomNavItem.Contacts,
        BottomNavItem.History,
        BottomNavItem.Config
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { item ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title
                            )
                        },
                        label = { Text(item.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                        onClick = {
                            bottomNavController.navigate(item.route) {
                                // Pop hasta el start destination del graph para evitar
                                // construir un gran stack de back destinations
                                popUpTo(bottomNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                // Evita múltiples copias del mismo destino cuando se reselecciona el mismo item
                                launchSingleTop = true
                                // Restaura el estado cuando se vuelve a seleccionar un item previamente seleccionado
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = bottomNavController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen()
            }

            composable(Screen.Map.route) {
                MapScreen()
            }

            composable(Screen.Contacts.route) {
                EmergencyContactScreen()
            }

            composable(Screen.History.route) {
                HistoryScreen()
            }

            composable(Screen.Config.route) {
                ConfigurationScreen(onSignOut = onSignOut)
            }
        }
    }
}