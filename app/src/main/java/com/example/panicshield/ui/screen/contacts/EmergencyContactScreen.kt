package com.example.panicshield.ui.screen.contacts

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class EmergencyContact(
    val name: String,
    val phone: String,
    val relationship: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyContactScreen() {
    var contacts by remember {
        mutableStateOf(
            listOf(
                EmergencyContact("María García", "+1234567890", "Madre"),
                EmergencyContact("Dr. Rodríguez", "+0987654321", "Médico"),
                EmergencyContact("Policía Local", "911", "Autoridad")
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Contactos de Emergencia",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            FloatingActionButton(
                onClick = { /* Agregar nuevo contacto */ },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar contacto")
            }
        }

        LazyColumn {
            items(contacts) { contact ->
                ContactCard(contact = contact)
            }
        }
    }
}

@Composable
private fun ContactCard(contact: EmergencyContact) {
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
                imageVector = Icons.Default.Person,
                contentDescription = "Contacto",
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = contact.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = contact.relationship,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = contact.phone,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            IconButton(onClick = { /* Llamar */ }) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = "Llamar",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
