package com.example.panicshield.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun HistoryScreen(
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "HISTORIAL",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Text(
            text = "Historial de alertas de pánico",
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Button(
            onClick = { /* TODO: Ver historial completo */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .padding(bottom = 8.dp)
        ) {
            Text("Ver Historial Completo")
        }

        Button(
            onClick = { /* TODO: Limpiar historial */ },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .padding(bottom = 24.dp)
        ) {
            Text("Limpiar Historial")
        }

        Button(
            onClick = onNavigateBack,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Volver")
        }
    }
}