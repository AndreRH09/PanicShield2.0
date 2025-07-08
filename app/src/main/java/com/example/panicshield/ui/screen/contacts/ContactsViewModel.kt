package com.example.panicshield.ui.screen.contacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.panicshield.domain.model.Contact
import com.example.panicshield.domain.model.PhoneContact
import com.example.panicshield.domain.usecase.ContactUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
        observeNetworkState()
    }

    fun loadContacts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            contactUseCase.getContacts()
                .onSuccess { contacts ->
                    _uiState.value = _uiState.value.copy(
                        contacts = contacts,
                        isLoading = false
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Error al cargar contactos"
                    )
                }
        }
    }

    fun refreshContacts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true)

            contactUseCase.syncContacts()
                .onSuccess {
                    loadContacts()
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isRefreshing = false,
                        errorMessage = "Error al sincronizar: ${error.message}"
                    )
                }
        }
    }

    fun loadPhoneContacts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingPhoneContacts = true, errorMessage = null)

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
                        contacts = _uiState.value.contacts + contact,
                        isCreating = false,
                        showAddDialog = false
                    )
                    clearDialogFields()
                    showSuccessMessage("Contacto creado exitosamente")
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
                    val updatedContacts = _uiState.value.contacts.map { contact ->
                        if (contact.id == id) updatedContact else contact
                    }
                    _uiState.value = _uiState.value.copy(
                        contacts = updatedContacts,
                        isUpdating = false,
                        showEditDialog = false,
                        editingContact = null
                    )
                    clearDialogFields()
                    showSuccessMessage("Contacto actualizado exitosamente")
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
                    val updatedContacts = _uiState.value.contacts.filter { it.id != id }
                    _uiState.value = _uiState.value.copy(
                        contacts = updatedContacts,
                        isDeleting = false,
                        showDeleteDialog = false,
                        contactToDelete = null
                    )
                    showSuccessMessage("Contacto eliminado exitosamente")
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isDeleting = false,
                        errorMessage = error.message ?: "Error al eliminar contacto"
                    )
                }
        }
    }

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

    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }

    private fun clearDialogFields() {
        _uiState.value = _uiState.value.copy(
            dialogName = "",
            dialogPhone = ""
        )
    }

    private fun showSuccessMessage(message: String) {
        _uiState.value = _uiState.value.copy(successMessage = message)
    }

    private fun observeNetworkState() {
        viewModelScope.launch {
            contactUseCase.observeNetworkState().collect { isConnected ->
                _uiState.value = _uiState.value.copy(isNetworkConnected = isConnected)
            }
        }
    }

    fun getSyncStats() {
        viewModelScope.launch {
            try {
                val stats = contactUseCase.getSyncStats()
                _uiState.value = _uiState.value.copy(syncStats = stats)
            } catch (e: Exception) {
                // Manejar error silenciosamente
            }
        }
    }

    fun validateContactData(name: String, phone: String): String? {
        return when {
            name.isBlank() -> "El nombre no puede estar vacío"
            name.length < 2 -> "El nombre debe tener al menos 2 caracteres"
            phone.isBlank() -> "El teléfono no puede estar vacío"
            phone.length < 7 -> "El teléfono debe tener al menos 7 dígitos"
            !phone.matches(Regex("^[+]?[0-9\\s\\-()]+$")) -> "El teléfono contiene caracteres inválidos"
            else -> null
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Limpieza adicional si es necesaria
    }
}

data class ContactsUiState(
    val contacts: List<Contact> = emptyList(),
    val phoneContacts: List<PhoneContact> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLoadingPhoneContacts: Boolean = false,
    val isCreating: Boolean = false,
    val isUpdating: Boolean = false,
    val isDeleting: Boolean = false,
    val isNetworkConnected: Boolean = true,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val showAddDialog: Boolean = false,
    val showEditDialog: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val showPhoneContactsDialog: Boolean = false,
    val editingContact: Contact? = null,
    val contactToDelete: Contact? = null,
    val dialogName: String = "",
    val dialogPhone: String = "",
    val syncStats: Map<String, Int> = emptyMap()
)