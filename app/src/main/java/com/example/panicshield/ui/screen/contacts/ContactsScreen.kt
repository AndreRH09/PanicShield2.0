package com.example.panicshield.ui.screen.contacts

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Contactos de Emergencia") },
                actions = {
                    IconButton(
                        onClick = { viewModel.loadContacts() }
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualizar")
                    }
                }
            )
        },
        floatingActionButton = {
            ContactsFab(
                onAddContact = { viewModel.showAddDialog() },
                onImportFromPhone = handleImportContacts
            )
        }
    ) { paddingValues ->
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

    // Dialogs
    ContactsDialogs(
        uiState = uiState,
        viewModel = viewModel
    )

    // Error handling
    uiState.errorMessage?.let { error ->
        LaunchedEffect(error) {
            //  manejar errores
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
    modifier: Modifier = Modifier
) {
    when {
        uiState.isLoading -> {
            Box(
                modifier = modifier,
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        uiState.contacts.isEmpty() -> {
            EmptyContactsState(
                onAddContact = onAddContact,
                onImportContacts = onImportContacts,
                modifier = modifier
            )
        }

        else -> {
            ContactsList(
                contacts = uiState.contacts,
                onEditContact = onEditContact,
                onDeleteContact = onDeleteContact,
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