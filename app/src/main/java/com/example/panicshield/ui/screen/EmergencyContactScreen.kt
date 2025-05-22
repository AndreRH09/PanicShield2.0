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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp



@Composable
fun EmergencyContactsScreen(
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
            text = "CONTACTOS DE EMERGENCIA",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Text(
            text = "Lista de contactos de emergencia",
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Button(
            onClick = { /* TODO: Agregar contacto */ },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .padding(bottom = 8.dp)
        ) {
            Text("Agregar Contacto")
        }

        Button(
            onClick = { /* TODO: Ver contactos */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .padding(bottom = 24.dp)
        ) {
            Text("Ver Todos los Contactos")
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