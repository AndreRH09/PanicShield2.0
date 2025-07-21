package com.example.panicshield.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
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
import com.example.panicshield.ui.screen.settings.SettingsViewModel
import kotlinx.coroutines.launch

sealed class MainScreen(val route: String, val title: String, val icon: ImageVector) {
    object Home : MainScreen("home", "Inicio", Icons.Default.Home)
    object Map : MainScreen("map", "Mapa", Icons.Default.Map)
    object Contacts : MainScreen("contacts", "Contactos", Icons.Default.Group)
    object History : MainScreen("history", "Historial", Icons.Default.History)
    object Settings : MainScreen("settings", "Configuración", Icons.Default.Settings)
    object Help : MainScreen("help", "Ayuda", Icons.Default.Info)
    object About : MainScreen("about", "Sobre los desarrolladores", Icons.Default.Groups)

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNavigation(
    navController: NavHostController = rememberNavController(),
    onLogout: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val currentRoute = navBackStackEntry?.destination?.route


    val bottomNavItems = listOf(
        MainScreen.Home,
        MainScreen.Map,
        MainScreen.Contacts,
        MainScreen.History,
        MainScreen.Settings
    )

    val drawerItems = listOf(
        MainScreen.Home,
        MainScreen.Map,
        MainScreen.Contacts,
        MainScreen.History,
        MainScreen.Settings,
        MainScreen.Help,
        MainScreen.About
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text("Menú", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(16.dp))
                drawerItems.forEach { screen ->
                    NavigationDrawerItem(
                        label = { Text(screen.title) },
                        icon = { Icon(screen.icon, contentDescription = null) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate(screen.route) {
                                popUpTo(MainScreen.Home.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                if (showTopBar(currentRoute)) {
                    SmallTopAppBar(
                        title = { Text("Panic Shield") },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "Abrir menú")
                            }
                        }
                    )
                }
            },
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
                                    popUpTo(MainScreen.Home.route) { saveState = true }
                                    launchSingleTop = true
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
                composable(MainScreen.Home.route) { HomeScreen() }
                composable(MainScreen.Map.route) { MapScreen() }
                composable(MainScreen.Contacts.route) { ContactsScreen() }
                composable(MainScreen.History.route) { HistoryScreen() }
                composable(MainScreen.Settings.route) {
                    SettingsScreen(
                        navController = navController,
                        onLogout = onLogout
                    )
                }
                composable(MainScreen.Help.route) {
                    Text("Pantalla de Ayuda")
                }
                composable(MainScreen.About.route) {
                    Text("Pantalla Sobre los desarrolladores")
                }
            }
        }
    }
}

fun showTopBar(route: String?): Boolean {
    return route != MainScreen.Contacts.route
}
