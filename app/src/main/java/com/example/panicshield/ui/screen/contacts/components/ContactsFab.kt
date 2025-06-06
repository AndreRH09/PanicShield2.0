package com.example.panicshield.ui.screen.contacts.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ContactsFab(
    onAddContact: () -> Unit,
    onImportFromPhone: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        FloatingActionButton(
            onClick = onAddContact,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Agregar contacto"
            )
        }

        FloatingActionButton(
            onClick = onImportFromPhone
        ) {
            Icon(
                Icons.Default.ContactPhone,
                contentDescription = "Importar desde teléfono"
            )
        }
    }
}