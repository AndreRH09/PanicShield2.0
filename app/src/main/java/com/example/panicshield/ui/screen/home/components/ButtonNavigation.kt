package com.example.panicshield.ui.screen.home.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable

@Composable
fun HomeBottomNavigation(
    currentScreen: String = "home",
    onNavigate: (String) -> Unit = { /* TODO: Navegación */ }
) {
    NavigationBar {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Inicio") },
            selected = currentScreen == "home",
            onClick = { onNavigate("home") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Map, contentDescription = "Mapa") },
            label = { Text("Mapa") },
            selected = currentScreen == "map",
            onClick = { onNavigate("map") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Contacts, contentDescription = "Contactos") },
            label = { Text("Contactos") },
            selected = currentScreen == "contacts",
            onClick = { onNavigate("contacts") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.History, contentDescription = "Historial") },
            label = { Text("Historial") },
            selected = currentScreen == "history",
            onClick = { onNavigate("history") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Settings, contentDescription = "Config") },
            label = { Text("Config") },
            selected = currentScreen == "settings",
            onClick = { onNavigate("settings") }
        )
    }
}