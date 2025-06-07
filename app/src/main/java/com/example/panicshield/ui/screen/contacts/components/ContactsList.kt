package com.example.panicshield.ui.screen.contacts.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.panicshield.domain.model.Contact

@Composable
fun ContactsList(
    contacts: List<Contact>,
    onEditContact: (Contact) -> Unit,
    onDeleteContact: (Contact) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(contacts) { contact ->
            ContactItem(
                contact = contact,
                onEdit = { onEditContact(contact) },
                onDelete = { onDeleteContact(contact) }
            )
        }
    }
}