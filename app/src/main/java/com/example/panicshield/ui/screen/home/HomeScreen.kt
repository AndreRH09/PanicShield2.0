package com.example.panicshield.ui.screen.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.panicshield.ui.screen.auth.AuthViewModel
import com.example.panicshield.ui.screen.home.components.HomeBottomNavigation
import com.example.panicshield.ui.screen.home.components.PanicButtonContent

@Composable
fun HomeScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Boton de panico",
            style = MaterialTheme.typography.headlineMedium
        )
    }
}