package com.example.panicshield.data.sms

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun PermissionRequestHandler(
    onPermissionsGranted: () -> Unit,
    onPermissionsDenied: (List<String>) -> Unit
) {
    val context = LocalContext.current
    val permissionManager = remember { PermissionManager(context) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val deniedPermissions = permissions.filterValues { !it }.keys.toList()
        if (deniedPermissions.isEmpty()) {
            onPermissionsGranted()
        } else {
            onPermissionsDenied(deniedPermissions)
        }
    }

    LaunchedEffect(Unit) {
        if (!permissionManager.hasAllPermissions()) {
            val requiredPermissions = permissionManager.getRequiredPermissions()
            if (requiredPermissions.isNotEmpty()) {
                permissionLauncher.launch(requiredPermissions)
            }
        } else {
            onPermissionsGranted()
        }
    }
}

@Composable
fun PermissionStatusCard(
    modifier: Modifier = Modifier,
    onRequestPermissions: () -> Unit
) {
    val context = LocalContext.current
    val permissionManager = remember { PermissionManager(context) }

    var smsPermission by remember { mutableStateOf(permissionManager.hasSmsPermission()) }
    var contactsPermission by remember { mutableStateOf(permissionManager.hasContactsPermission()) }

    LaunchedEffect(context) {
        smsPermission = permissionManager.hasSmsPermission()
        contactsPermission = permissionManager.hasContactsPermission()
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (smsPermission && contactsPermission)
                Color.Green.copy(alpha = 0.1f)
            else
                Color.Red.copy(alpha = 0.1f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Estado de Permisos",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (smsPermission) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (smsPermission) Color.Green else Color.Red,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "SMS: ${if (smsPermission) "✅ Habilitado" else "❌ Deshabilitado"}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (contactsPermission) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (contactsPermission) Color.Green else Color.Red,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Contactos: ${if (contactsPermission) "✅ Habilitado" else "❌ Deshabilitado"}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (!smsPermission || !contactsPermission) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onRequestPermissions,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Habilitar Permisos")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "⚠️ Los permisos son necesarios para enviar SMS de emergencia",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Red
                )
            }
        }
    }
}

@Composable
fun PermissionExplanationDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onAccept: () -> Unit
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(text = "Permisos Necesarios", fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text("PanicShield necesita los siguientes permisos para funcionar correctamente:")
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("📱 SMS: Para enviar mensajes de emergencia a tus contactos")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("📞 Contactos: Para acceder a tu lista de contactos")
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Estos permisos son esenciales para el funcionamiento de emergencia.", color = Color.Gray)
                }
            },
            confirmButton = {
                Button(onClick = onAccept) {
                    Text("Conceder Permisos")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancelar")
                }
            }
        )
    }
}