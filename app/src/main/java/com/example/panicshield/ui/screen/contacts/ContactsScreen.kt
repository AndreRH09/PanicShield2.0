package com.example.panicshield.ui.screen.contacts

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.panicshield.ui.screen.contacts.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(
    modifier: Modifier = Modifier,
    viewModel: ContactsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Launcher para solicitar permisos de contactos
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.showPhoneContactsDialog()
        }
    }

    // Función para manejar la importación de contactos
    val handleImportContacts = {
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) -> {
                viewModel.showPhoneContactsDialog()
            }
            else -> {
                permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
            }
        }
    }

    // Estado para mostrar SnackBar de sincronización
    var showSyncMessage by remember { mutableStateOf(false) }
    var syncMessage by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Contactos de Emergencia") },
                actions = {
                    // Botón de actualización rápida (datos locales)
                    IconButton(
                        onClick = { viewModel.loadContacts() },
                        enabled = !uiState.isLoading && !uiState.isSyncing
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualizar")
                    }

                    // Botón de sincronización (forzar desde servidor)
                    IconButton(
                        onClick = {
                            viewModel.syncContacts()
                            syncMessage = "Sincronizando con el servidor..."
                            showSyncMessage = true
                        },
                        enabled = !uiState.isLoading && !uiState.isSyncing
                    ) {
                        if (uiState.isSyncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                Icons.Default.CloudSync,
                                contentDescription = "Sincronizar con servidor"
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            ContactsFab(
                onAddContact = { viewModel.showAddDialog() },
                onImportFromPhone = handleImportContacts,
                enabled = !uiState.isLoading && !uiState.isSyncing
            )
        },
        snackbarHost = {
            SnackbarHost(
                hostState = remember { SnackbarHostState() }.apply {
                    LaunchedEffect(showSyncMessage) {
                        if (showSyncMessage) {
                            showSnackbar(
                                message = syncMessage,
                                duration = SnackbarDuration.Short
                            )
                            showSyncMessage = false
                        }
                    }

                    // Mostrar mensaje cuando termine la sincronización
                    LaunchedEffect(uiState.isSyncing) {
                        if (!uiState.isSyncing && syncMessage.isNotEmpty()) {
                            showSnackbar(
                                message = "Contactos sincronizados correctamente",
                                duration = SnackbarDuration.Short
                            )
                            syncMessage = ""
                        }
                    }

                    // Mostrar mensaje de error de sincronización
                    LaunchedEffect(uiState.syncError) {
                        uiState.syncError?.let { error ->
                            showSnackbar(
                                message = "Error al sincronizar: $error",
                                duration = SnackbarDuration.Long,
                                actionLabel = "Reintentar"
                            ).let { result ->
                                if (result == SnackbarResult.ActionPerformed) {
                                    viewModel.syncContacts()
                                }
                            }
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        ContactsContent(
            uiState = uiState,
            onAddContact = { viewModel.showAddDialog() },
            onImportContacts = handleImportContacts,
            onEditContact = { viewModel.showEditDialog(it) },
            onDeleteContact = { viewModel.showDeleteDialog(it) },
            onRefresh = { viewModel.loadContacts() },
            onSync = { viewModel.syncContacts() },
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        )
    }

    // Dialogs
    ContactsDialogs(
        uiState = uiState,
        viewModel = viewModel
    )

    // Error handling general
    uiState.errorMessage?.let { error ->
        LaunchedEffect(error) {
            // Manejar errores generales si es necesario
        }
    }
}

@Composable
private fun ContactsContent(
    uiState: ContactsUiState,
    onAddContact: () -> Unit,
    onImportContacts: () -> Unit,
    onEditContact: (com.example.panicshield.domain.model.Contact) -> Unit,
    onDeleteContact: (com.example.panicshield.domain.model.Contact) -> Unit,
    onRefresh: () -> Unit,
    onSync: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Indicador de estado de sincronización
    if (uiState.isSyncing) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Sincronizando contactos...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }

    when {
        uiState.isLoading && uiState.contacts.isEmpty() -> {
            Box(
                modifier = modifier,
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Cargando contactos...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        uiState.contacts.isEmpty() && !uiState.isLoading -> {
            EmptyContactsState(
                onAddContact = onAddContact,
                onImportContacts = onImportContacts,
                onSync = onSync,
                isSyncing = uiState.isSyncing,
                modifier = modifier
            )
        }

        else -> {
            ContactsList(
                contacts = uiState.contacts,
                onEditContact = onEditContact,
                onDeleteContact = onDeleteContact,
                isLoading = uiState.isLoading,
                isSyncing = uiState.isSyncing,
                onRefresh = onRefresh,
                modifier = modifier
            )
        }
    }
}

@Composable
private fun ContactsDialogs(
    uiState: ContactsUiState,
    viewModel: ContactsViewModel
) {
    // Add Contact Dialog
    if (uiState.showAddDialog) {
        ContactDialog(
            title = "Agregar Contacto",
            name = uiState.dialogName,
            phone = uiState.dialogPhone,
            onNameChange = viewModel::updateDialogName,
            onPhoneChange = viewModel::updateDialogPhone,
            onConfirm = { viewModel.createContact(uiState.dialogName, uiState.dialogPhone) },
            onDismiss = { viewModel.hideAddDialog() },
            isLoading = uiState.isCreating
        )
    }

    // Edit Contact Dialog
    if (uiState.showEditDialog && uiState.editingContact != null) {
        ContactDialog(
            title = "Editar Contacto",
            name = uiState.dialogName,
            phone = uiState.dialogPhone,
            onNameChange = viewModel::updateDialogName,
            onPhoneChange = viewModel::updateDialogPhone,
            onConfirm = {
                viewModel.updateContact(
                    uiState.editingContact!!.id!!,
                    uiState.dialogName,
                    uiState.dialogPhone
                )
            },
            onDismiss = { viewModel.hideEditDialog() },
            isLoading = uiState.isUpdating
        )
    }

    // Delete Contact Dialog
    if (uiState.showDeleteDialog && uiState.contactToDelete != null) {
        DeleteContactDialog(
            contactName = uiState.contactToDelete!!.name,
            onConfirm = { viewModel.deleteContact(uiState.contactToDelete!!.id!!) },
            onDismiss = { viewModel.hideDeleteDialog() },
            isLoading = uiState.isDeleting
        )
    }

    // Phone Contacts Dialog
    if (uiState.showPhoneContactsDialog) {
        PhoneContactsDialog(
            phoneContacts = uiState.phoneContacts,
            isLoading = uiState.isLoadingPhoneContacts,
            onContactSelected = viewModel::selectPhoneContact,
            onDismiss = { viewModel.hidePhoneContactsDialog() }
        )
    }
}