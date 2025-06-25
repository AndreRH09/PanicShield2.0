package com.example.panicshield.ui.screen.contacts.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

@Composable
fun ContactsFab(
    onAddContact: () -> Unit,
    onImportFromPhone: () -> Unit,
    enabled: Boolean = true, // Parámetro faltante
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    FloatingActionButton(
        onClick = { expanded = true },
        modifier = modifier,
        containerColor = if (enabled) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        }
    ) {
        Icon(
            Icons.Default.Add,
            contentDescription = "Agregar contacto",
            tint = if (enabled) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }

    DropdownMenu(
        expanded = expanded && enabled,
        onDismissRequest = { expanded = false }
    ) {
        DropdownMenuItem(
            text = { Text("Agregar contacto manual") },
            onClick = {
                onAddContact()
                expanded = false
            },
            leadingIcon = {
                Icon(Icons.Default.PersonAdd, contentDescription = null)
            }
        )
        DropdownMenuItem(
            text = { Text("Importar del teléfono") },
            onClick = {
                onImportFromPhone()
                expanded = false
            },
            leadingIcon = {
                Icon(Icons.Default.Contacts, contentDescription = null)
            }
        )
    }
}