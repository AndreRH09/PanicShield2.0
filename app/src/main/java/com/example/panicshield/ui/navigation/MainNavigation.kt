package com.example.panicshield.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.panicshield.ui.screen.home.HomeScreen
import com.example.panicshield.ui.screen.map.MapScreen
import com.example.panicshield.ui.screen.contacts.ContactsScreen
import com.example.panicshield.ui.screen.history.HistoryScreen
import com.example.panicshield.ui.screen.settings.SettingsScreen
import com.example.panicshield.data.local.TokenManager

sealed class MainScreen(val route: String, val title: String, val icon: ImageVector) {
    object Home : MainScreen("home", "Inicio", Icons.Default.Home)
    object Map : MainScreen("map", "Mapa", Icons.Default.Map)
    object Contacts : MainScreen("contacts", "Contactos", Icons.Default.Group)
    object History : MainScreen("history", "Historial", Icons.Default.History)
    object Settings : MainScreen("settings", "Configuración", Icons.Default.Settings)
}

@Composable
fun MainNavigation(
    navController: NavHostController = rememberNavController(),
    onLogout: () -> Unit

) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val bottomNavItems = listOf(
        MainScreen.Home,
        MainScreen.Map,
        MainScreen.Contacts,
        MainScreen.History,
        MainScreen.Settings
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.title
                            )
                        },
                        label = { Text(screen.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                // Pop up to the start destination of the graph to
                                // avoid building up a large stack of destinations
                                // on the back stack as users select items
                                popUpTo(MainScreen.Home.route) {
                                    saveState = true
                                }
                                // Avoid multiple copies of the same destination when
                                // reselecting the same item
                                launchSingleTop = true
                                // Restore state when reselecting a previously selected item
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = MainScreen.Home.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable(MainScreen.Home.route) {
                HomeScreen()
            }

            composable(MainScreen.Map.route) {
                MapScreen()
            }

            composable(MainScreen.Contacts.route) {
                ContactsScreen()
            }

            composable(MainScreen.History.route) {
                HistoryScreen()
            }

            composable(MainScreen.Settings.route) {
                SettingsScreen(
                    onLogout = onLogout
                )
            }
        }
    }
}