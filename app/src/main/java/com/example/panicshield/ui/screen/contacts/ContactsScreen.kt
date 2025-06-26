package com.example.panicshield.ui.screen.contacts

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.panicshield.ui.screen.contacts.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(
    modifier: Modifier = Modifier,
    viewModel: ContactsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Manejar errores con SnackBar
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { error ->
            // Aquí podrías mostrar un SnackBar si tienes acceso al SnackBarHostState
            // Por ahora solo limpiamos el error después de mostrarlo
            viewModel.clearErrorMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Contactos de Emergencia",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // FAB para importar desde contactos del teléfono
                FloatingActionButton(
                    onClick = { viewModel.showPhoneContactsDialog() },
                    containerColor = MaterialTheme.colorScheme.secondary
                ) {
                    Icon(
                        imageVector = Icons.Default.Contacts,
                        contentDescription = "Importar contactos"
                    )
                }

                // FAB principal para agregar contacto
                FloatingActionButton(
                    onClick = { viewModel.showAddDialog() }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Agregar contacto"
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading && uiState.contacts.isEmpty() -> {
                    ContactsLoadingIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                uiState.contacts.isEmpty() && !uiState.isLoading -> {
                    ContactsEmptyState(
                        onAddContactClick = { viewModel.showAddDialog() },
                        onImportContactsClick = { viewModel.showPhoneContactsDialog() },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Indicador de carga en la parte superior si está refrescando
                        if (uiState.isLoading) {
                            item {
                                LinearProgressIndicator(
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        // Mensaje de error si existe
                        uiState.errorMessage?.let { error ->
                            item {
                                ContactsErrorMessage(
                                    message = error,
                                    onRetry = { viewModel.refreshContacts() },
                                    onDismiss = { viewModel.clearErrorMessage() }
                                )
                            }
                        }

                        // Lista de contactos
                        items(
                            items = uiState.contacts,
                            key = { contact -> contact.id ?: 0 }
                        ) { contact ->
                            ContactCard(
                                contact = contact,
                                onEditClick = { viewModel.showEditDialog(contact) },
                                onDeleteClick = { viewModel.showDeleteDialog(contact) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Diálogos
    if (uiState.showAddDialog) {
        AddEditContactDialog(
            title = "Agregar Contacto",
            name = uiState.dialogName,
            phone = uiState.dialogPhone,
            onNameChange = viewModel::updateDialogName,
            onPhoneChange = viewModel::updateDialogPhone,
            onConfirm = { name, phone ->
                viewModel.createContact(name, phone)
            },
            onDismiss = { viewModel.hideAddDialog() },
            isLoading = uiState.isCreating,
            confirmButtonText = "Agregar"
        )
    }

    if (uiState.showEditDialog && uiState.editingContact != null) {
        AddEditContactDialog(
            title = "Editar Contacto",
            name = uiState.dialogName,
            phone = uiState.dialogPhone,
            onNameChange = viewModel::updateDialogName,
            onPhoneChange = viewModel::updateDialogPhone,
            onConfirm = { name, phone ->
                viewModel.updateContact(uiState.editingContact!!.id!!, name, phone)
            },
            onDismiss = { viewModel.hideEditDialog() },
            isLoading = uiState.isUpdating,
            confirmButtonText = "Actualizar"
        )
    }

    if (uiState.showDeleteDialog && uiState.contactToDelete != null) {
        DeleteContactDialog(
            contactName = uiState.contactToDelete!!.name,
            onConfirm = {
                viewModel.deleteContact(uiState.contactToDelete!!.id!!)
            },
            onDismiss = { viewModel.hideDeleteDialog() },
            isLoading = uiState.isDeleting
        )
    }

    if (uiState.showPhoneContactsDialog) {
        PhoneContactsDialog(
            phoneContacts = uiState.phoneContacts,
            isLoading = uiState.isLoadingPhoneContacts,
            onContactSelect = viewModel::selectPhoneContact,
            onDismiss = { viewModel.hidePhoneContactsDialog() }
        )
    }
}