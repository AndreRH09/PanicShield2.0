package com.example.panicshield.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen(
    onNavigateToMap: () -> Unit,
    onNavigateToEmergencyContacts: () -> Unit,
    onNavigateToHistory: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "HOME",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Botón de Pánico Principal
        Button(
            onClick = { /* TODO: Implementar lógica de pánico */ },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = "BOTÓN DE PÁNICO",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Botones de navegación
        Button(
            onClick = onNavigateToMap,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .padding(bottom = 8.dp)
        ) {
            Text("Mapa")
        }

        Button(
            onClick = onNavigateToEmergencyContacts,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .padding(bottom = 8.dp)
        ) {
            Text("Contactos de Emergencia")
        }

        Button(
            onClick = onNavigateToHistory,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Historial")
        }
    }
}