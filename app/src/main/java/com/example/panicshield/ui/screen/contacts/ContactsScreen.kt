package com.example.panicshield.ui.screen.contacts

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.panicshield.domain.model.Contact
import com.example.panicshield.ui.screen.contacts.components.*
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(
    modifier: Modifier = Modifier,
    viewModel: ContactsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = uiState.isRefreshing)

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

    // Función para manejar el refresh
    val handleRefresh = {
        viewModel.refreshContacts()
    }

    Scaffold(
        topBar = {
            ContactsTopBar(
                isNetworkConnected = uiState.isNetworkConnected,
                onRefresh = { viewModel.loadContacts() },
                onSync = { viewModel.refreshContacts() }
            )
        },
        floatingActionButton = {
            ContactsFab(
                onAddContact = { viewModel.showAddDialog() },
                onImportFromPhone = handleImportContacts
            )
        }
    ) { paddingValues ->
        SwipeRefresh(
            state = swipeRefreshState,
            onRefresh = handleRefresh
        ) {
            ContactsContent(
                uiState = uiState,
                onAddContact = { viewModel.showAddDialog() },
                onImportContacts = handleImportContacts,
                onEditContact = { viewModel.showEditDialog(it) },
                onDeleteContact = { viewModel.showDeleteDialog(it) },
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            )
        }
    }

    // Dialogs
    ContactsDialogs(
        uiState = uiState,
        viewModel = viewModel
    )

    // Mensajes de estado
    ContactsMessages(
        uiState = uiState,
        viewModel = viewModel
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContactsTopBar(
    isNetworkConnected: Boolean,
    onRefresh: () -> Unit,
    onSync: () -> Unit
) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Contactos de Emergencia")
                if (!isNetworkConnected) {
                    Icon(
                        Icons.Default.CloudOff,
                        contentDescription = "Sin conexión",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        },
        actions = {
            IconButton(onClick = onRefresh) {
                Icon(Icons.Default.Refresh, contentDescription = "Actualizar")
            }
            IconButton(onClick = onSync) {
                Icon(Icons.Default.Sync, contentDescription = "Sincronizar")
            }
        }
    )
}

@Composable
private fun ContactsContent(
    uiState: ContactsUiState,
    onAddContact: () -> Unit,
    onImportContacts: () -> Unit,
    onEditContact: (Contact) -> Unit,
    onDeleteContact: (Contact) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Información de estado de red
        if (!uiState.isNetworkConnected) {
            NetworkStatusCard()
        }

        // Estadísticas de sincronización
        if (uiState.syncStats.isNotEmpty()) {
            SyncStatsCard(stats = uiState.syncStats)
        }

        // Contenido principal
        when {
            uiState.isLoading -> {
                LoadingContent()
            }

            uiState.contacts.isEmpty() -> {
                EmptyContactsState(
                    onAddContact = onAddContact,
                    onImportContacts = onImportContacts,
                    modifier = Modifier.fillMaxSize()
                )
            }

            else -> {
                ContactsList(
                    contacts = uiState.contacts,
                    onEditContact = onEditContact,
                    onDeleteContact = onDeleteContact,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Cargando contactos...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun NetworkStatusCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.CloudOff,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Sin conexión. Los cambios se sincronizarán cuando se restablezca la conexión.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

@Composable
private fun SyncStatsCard(stats: Map<String, Int>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Estado de Sincronización",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SyncStatItem("Total", stats["total"] ?: 0)
                SyncStatItem("Sincronizados", stats["synced"] ?: 0)
                SyncStatItem("Pendientes",
                    (stats["pending_insert"] ?: 0) +
                    (stats["pending_update"] ?: 0) +
                    (stats["pending_delete"] ?: 0)
                )
            }
        }
    }
}

@Composable
private fun SyncStatItem(label: String, value: Int) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
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
            onConfirm = {
                val validationError = viewModel.validateContactData(
                    uiState.dialogName,
                    uiState.dialogPhone
                )
                if (validationError == null) {
                    viewModel.createContact(uiState.dialogName, uiState.dialogPhone)
                }
            },
            onDismiss = { viewModel.hideAddDialog() },
            isLoading = uiState.isCreating,
            validationError = viewModel.validateContactData(
                uiState.dialogName,
                uiState.dialogPhone
            )
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
                val validationError = viewModel.validateContactData(
                    uiState.dialogName,
                    uiState.dialogPhone
                )
                if (validationError == null) {
                    viewModel.updateContact(
                        uiState.editingContact!!.id!!,
                        uiState.dialogName,
                        uiState.dialogPhone
                    )
                }
            },
            onDismiss = { viewModel.hideEditDialog() },
            isLoading = uiState.isUpdating,
            validationError = viewModel.validateContactData(
                uiState.dialogName,
                uiState.dialogPhone
            )
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

@Composable
private fun ContactsMessages(
    uiState: ContactsUiState,
    viewModel: ContactsViewModel
) {
    // Error messages
    uiState.errorMessage?.let { error ->
        LaunchedEffect(error) {
            // Aquí podrías mostrar un Snackbar o manejar el error
            delay(3000)
            viewModel.clearErrorMessage()
        }
    }

    // Success messages
    uiState.successMessage?.let { success ->
        LaunchedEffect(success) {
            // Aquí podrías mostrar un Snackbar de éxito
            delay(2000)
            viewModel.clearSuccessMessage()
        }
    }
}