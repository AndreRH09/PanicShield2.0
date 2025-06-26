package com.example.panicshield.ui.screen.contacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.panicshield.data.util.Resource
import com.example.panicshield.domain.model.Contact
import com.example.panicshield.domain.model.PhoneContact
import com.example.panicshield.domain.usecase.ContactUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContactsViewModel @Inject constructor(
    private val contactUseCase: ContactUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ContactsUiState())
    val uiState: StateFlow<ContactsUiState> = _uiState.asStateFlow()

    init {
        loadContacts()
    }

    fun loadContacts() {
        viewModelScope.launch {
            contactUseCase.getContacts().collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = true,
                            contacts = resource.data ?: emptyList(),
                            errorMessage = null
                        )
                    }
                    is Resource.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            contacts = resource.data ?: emptyList(),
                            errorMessage = null
                        )
                    }
                    is Resource.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            contacts = resource.data ?: emptyList(),
                            errorMessage = resource.message
                        )
                    }
                }
            }
        }
    }

    fun refreshContacts() {
        // Forzar actualización desde la red
        loadContacts()
    }

    fun loadPhoneContacts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingPhoneContacts = true)

            try {
                val phoneContacts = contactUseCase.getPhoneContacts()
                _uiState.value = _uiState.value.copy(
                    phoneContacts = phoneContacts,
                    isLoadingPhoneContacts = false,
                    showPhoneContactsDialog = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoadingPhoneContacts = false,
                    errorMessage = "Error al cargar contactos del teléfono: ${e.message}"
                )
            }
        }
    }

    fun createContact(name: String, phone: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCreating = true, errorMessage = null)

            contactUseCase.createContact(name, phone)
                .onSuccess { contact ->
                    _uiState.value = _uiState.value.copy(
                        isCreating = false,
                        showAddDialog = false
                    )
                    clearDialogFields()
                    // Los datos se actualizarán automáticamente a través del Flow de getContacts()
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isCreating = false,
                        errorMessage = error.message ?: "Error al crear contacto"
                    )
                }
        }
    }

    fun updateContact(id: Long, name: String, phone: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUpdating = true, errorMessage = null)

            contactUseCase.updateContact(id, name, phone)
                .onSuccess { updatedContact ->
                    _uiState.value = _uiState.value.copy(
                        isUpdating = false,
                        showEditDialog = false,
                        editingContact = null
                    )
                    clearDialogFields()
                    // Los datos se actualizarán automáticamente a través del Flow de getContacts()
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isUpdating = false,
                        errorMessage = error.message ?: "Error al actualizar contacto"
                    )
                }
        }
    }

    fun deleteContact(id: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDeleting = true, errorMessage = null)

            contactUseCase.deleteContact(id)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isDeleting = false,
                        showDeleteDialog = false,
                        contactToDelete = null
                    )
                    // Los datos se actualizarán automáticamente a través del Flow de getContacts()
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isDeleting = false,
                        errorMessage = error.message ?: "Error al eliminar contacto"
                    )
                }
        }
    }

    // Métodos de UI (sin cambios)
    fun showAddDialog() {
        _uiState.value = _uiState.value.copy(showAddDialog = true)
    }

    fun hideAddDialog() {
        _uiState.value = _uiState.value.copy(showAddDialog = false)
        clearDialogFields()
    }

    fun showEditDialog(contact: Contact) {
        _uiState.value = _uiState.value.copy(
            showEditDialog = true,
            editingContact = contact,
            dialogName = contact.name,
            dialogPhone = contact.phone
        )
    }

    fun hideEditDialog() {
        _uiState.value = _uiState.value.copy(
            showEditDialog = false,
            editingContact = null
        )
        clearDialogFields()
    }

    fun showDeleteDialog(contact: Contact) {
        _uiState.value = _uiState.value.copy(
            showDeleteDialog = true,
            contactToDelete = contact
        )
    }

    fun hideDeleteDialog() {
        _uiState.value = _uiState.value.copy(
            showDeleteDialog = false,
            contactToDelete = null
        )
    }

    fun showPhoneContactsDialog() {
        loadPhoneContacts()
    }

    fun hidePhoneContactsDialog() {
        _uiState.value = _uiState.value.copy(
            showPhoneContactsDialog = false,
            phoneContacts = emptyList()
        )
    }

    fun selectPhoneContact(phoneContact: PhoneContact) {
        _uiState.value = _uiState.value.copy(
            showPhoneContactsDialog = false,
            showAddDialog = true,
            dialogName = phoneContact.name,
            dialogPhone = phoneContact.phone,
            phoneContacts = emptyList()
        )
    }

    fun updateDialogName(name: String) {
        _uiState.value = _uiState.value.copy(dialogName = name)
    }

    fun updateDialogPhone(phone: String) {
        _uiState.value = _uiState.value.copy(dialogPhone = phone)
    }

    fun clearErrorMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    private fun clearDialogFields() {
        _uiState.value = _uiState.value.copy(
            dialogName = "",
            dialogPhone = ""
        )
    }
}

data class ContactsUiState(
    val contacts: List<Contact> = emptyList(),
    val phoneContacts: List<PhoneContact> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingPhoneContacts: Boolean = false,
    val isCreating: Boolean = false,
    val isUpdating: Boolean = false,
    val isDeleting: Boolean = false,
    val errorMessage: String? = null,
    val showAddDialog: Boolean = false,
    val showEditDialog: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val showPhoneContactsDialog: Boolean = false,
    val editingContact: Contact? = null,
    val contactToDelete: Contact? = null,
    val dialogName: String = "",
    val dialogPhone: String = ""
)