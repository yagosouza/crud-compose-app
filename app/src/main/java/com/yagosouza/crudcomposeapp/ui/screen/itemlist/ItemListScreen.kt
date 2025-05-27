package com.yagosouza.crudcomposeapp.ui.screen.itemlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.yagosouza.crudcomposeapp.data.local.model.ItemEntity
import org.koin.androidx.compose.koinViewModel // Correto para ViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemListScreen(
    navController: NavController, // Pode ser usado para navegação para tela de detalhes, etc.
    viewModel: ItemListViewModel = koinViewModel() // Injeta o ViewModel
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                FloatingActionButton(onClick = { viewModel.synchronizeItems() }) {
                    Icon(Icons.Filled.Refresh, "Sincronizar Itens")
                }
                Spacer(modifier = Modifier.height(8.dp))
                FloatingActionButton(onClick = { viewModel.onAddNewItemClicked() }) {
                    Icon(Icons.Filled.Add, "Adicionar Item")
                }
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp)
        ) {
            if (state.isLoading && state.items.isEmpty()) { // Loading inicial
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }

            state.error?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            state.syncMessage?.let { message ->
                Text(
                    text = message,
                    color = if (message.contains("Erro")) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }


            if (state.items.isEmpty() && !state.isLoading) {
                Text("Nenhum item encontrado.", modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(state.items, key = { it.localId }) { item ->
                        ItemRow(item = item, onDeleteClick = { viewModel.deleteItem(item) })
                        Divider()
                    }
                }
            }
        }
    }

    if (viewModel.showAddItemDialog.value) {
        AddItemDialog(
            itemName = viewModel.newItemName.value,
            onItemNameChange = viewModel::onNewItemNameChange,
            itemDescription = viewModel.newItemDescription.value,
            onItemDescriptionChange = viewModel::onNewItemDescriptionChange,
            onDismiss = viewModel::onDismissAddItemDialog,
            onConfirm = viewModel::onConfirmAddItem
        )
    }
}

@Composable
fun ItemRow(item: ItemEntity, onDeleteClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Navegar para detalhes do item, se houver */ }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = item.name, style = MaterialTheme.typography.titleMedium)
            Text(text = item.description, style = MaterialTheme.typography.bodyMedium)
            if (item.needsSync) {
                Text("Pendente de sincronização", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            }
            if (item.isDeleted) {
                Text("Marcado para exclusão", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            }
        }
        IconButton(onClick = onDeleteClick) {
            Icon(Icons.Filled.Delete, contentDescription = "Deletar Item")
        }
    }
}

@Composable
fun AddItemDialog(
    itemName: String,
    onItemNameChange: (String) -> Unit,
    itemDescription: String,
    onItemDescriptionChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Adicionar Novo Item") },
        text = {
            Column {
                OutlinedTextField(
                    value = itemName,
                    onValueChange = onItemNameChange,
                    label = { Text("Nome do Item") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = itemDescription,
                    onValueChange = onItemDescriptionChange,
                    label = { Text("Descrição") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Adicionar")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}