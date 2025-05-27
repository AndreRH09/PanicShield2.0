package com.example.panicshield.ui.screen.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class HistoryEntry(
    val date: String,
    val time: String,
    val type: String,
    val status: String,
    val location: String
)

@Composable
fun HistoryScreen() {
    val historyEntries = listOf(
        HistoryEntry("2024-01-15", "14:30", "Alerta de Pánico", "Resuelta", "Centro Comercial"),
        HistoryEntry("2024-01-10", "09:15", "Prueba del Sistema", "Completada", "Casa"),
        HistoryEntry("2024-01-05", "22:45", "Alerta de Pánico", "Falsa Alarma", "Parque Central")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Historial de Alertas",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (historyEntries.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "Sin historial",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "No hay alertas registradas",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn {
                items(historyEntries) { entry ->
                    HistoryCard(entry = entry)
                }
            }
        }
    }
}

@Composable
private fun HistoryCard(entry: HistoryEntry) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Alerta",
                modifier = Modifier.size(32.dp),
                tint = when (entry.status) {
                    "Resuelta" -> Color.Green
                    "Falsa Alarma" -> Color.Yellow
                    else -> MaterialTheme.colorScheme.primary
                }
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = entry.type,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${entry.date} - ${entry.time}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = entry.location,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = entry.status,
                style = MaterialTheme.typography.labelMedium,
                color = when (entry.status) {
                    "Resuelta" -> Color.Green
                    "Falsa Alarma" -> Color.Yellow
                    else -> MaterialTheme.colorScheme.primary
                },
                fontWeight = FontWeight.Bold
            )
        }
    }
}