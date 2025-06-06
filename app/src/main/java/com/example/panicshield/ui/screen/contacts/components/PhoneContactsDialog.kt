package com.example.panicshield.ui.screen.contacts.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.panicshield.domain.model.PhoneContact

@Composable
fun PhoneContactsDialog(
    phoneContacts: List<PhoneContact>,
    isLoading: Boolean,
    onContactSelected: (PhoneContact) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Seleccionar Contacto") },
        text = {
            when {
                isLoading -> {
                    LoadingPhoneContacts()
                }
                phoneContacts.isEmpty() -> {
                    EmptyPhoneContacts()
                }
                else -> {
                    PhoneContactsList(
                        phoneContacts = phoneContacts,
                        onContactSelected = onContactSelected
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        },
        modifier = modifier
    )
}

@Composable
private fun LoadingPhoneContacts() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Cargando contactos...",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun EmptyPhoneContacts() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No se encontraron contactos en el teléfono",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PhoneContactsList(
    phoneContacts: List<PhoneContact>,
    onContactSelected: (PhoneContact) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    ) {
        items(phoneContacts) { phoneContact ->
            PhoneContactItem(
                phoneContact = phoneContact,
                onSelected = { onContactSelected(phoneContact) }
            )
        }
    }
}

@Composable
private fun PhoneContactItem(
    phoneContact: PhoneContact,
    onSelected: () -> Unit
) {
    TextButton(
        onClick = onSelected,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = phoneContact.name,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = phoneContact.phone,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    HorizontalDivider()
}