package com.example.panicshield.ui.screen.contacts.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DeleteContactDialog(
    contactName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Eliminar Contacto") },
        text = {
            Text("¿Estás seguro de que quieres eliminar a $contactName de tus contactos de emergencia?")
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onError
                    )
                } else {
                    Text("Eliminar")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("Cancelar")
            }
        },
        modifier = modifier
    )
}