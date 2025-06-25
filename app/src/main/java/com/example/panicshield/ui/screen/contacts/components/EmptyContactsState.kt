package com.example.panicshield.ui.screen.contacts.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ContactPage
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun EmptyContactsState(
    onAddContact: () -> Unit,
    onImportContacts: () -> Unit,
    onSync: () -> Unit, // Parámetro faltante
    isSyncing: Boolean = false, // Parámetro faltante
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.ContactPage,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "No tienes contactos de emergencia",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Agrega contactos para tenerlos disponibles en caso de emergencia",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Botón para agregar contacto manual
        Button(
            onClick = onAddContact,
            enabled = !isSyncing
        ) {
            Icon(Icons.Default.PersonAdd, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Agregar contacto")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón para importar contactos
        OutlinedButton(
            onClick = onImportContacts,
            enabled = !isSyncing
        ) {
            Icon(Icons.Default.Contacts, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Importar del teléfono")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón para sincronizar
        TextButton(
            onClick = onSync,
            enabled = !isSyncing
        ) {
            if (isSyncing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sincronizando...")
            } else {
                Icon(Icons.Default.CloudSync, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sincronizar con servidor")
            }
        }
    }
}