package com.yagosouza.crudcomposeapp.ui.screen.itemlist

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yagosouza.crudcomposeapp.data.local.model.ItemEntity
import com.yagosouza.crudcomposeapp.domain.usecase.AddItemUseCase
import com.yagosouza.crudcomposeapp.domain.usecase.DeleteItemUseCase
import com.yagosouza.crudcomposeapp.domain.usecase.GetItemsUseCase
import com.yagosouza.crudcomposeapp.domain.usecase.SyncItemsUseCase
import com.yagosouza.crudcomposeapp.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

data class ItemListState(
    val items: List<ItemEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSyncing: Boolean = false,
    val syncMessage: String? = null
)

class ItemListViewModel(
    private val getItemsUseCase: GetItemsUseCase,
    private val addItemUseCase: AddItemUseCase,
    private val deleteItemUseCase: DeleteItemUseCase,
    private val syncItemsUseCase: SyncItemsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ItemListState())
    val state: StateFlow<ItemListState> = _state.asStateFlow()

    // Para o diálogo de adicionar item
    private val _showAddItemDialog = mutableStateOf(false)
    val showAddItemDialog: State<Boolean> = _showAddItemDialog

    private val _newItemName = mutableStateOf("")
    val newItemName: State<String> = _newItemName

    private val _newItemDescription = mutableStateOf("")
    val newItemDescription: State<String> = _newItemDescription

    init {
        loadItems()
        // Opcional: Iniciar uma sincronização ao iniciar o ViewModel
        // synchronizeItems()
    }

    fun loadItems() {
        getItemsUseCase().onEach { result ->
            when (result) {
                is Resource.Loading -> {
                    _state.value = _state.value.copy(isLoading = true, error = null)
                }
                is Resource.Success -> {
                    _state.value = _state.value.copy(
                        items = result.data ?: emptyList(),
                        isLoading = false,
                        error = null
                    )
                }
                is Resource.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = result.message ?: "Erro desconhecido ao carregar itens"
                    )
                }
            }
        }.launchIn(viewModelScope)
    }

    fun onAddNewItemClicked() {
        _showAddItemDialog.value = true
    }

    fun onDismissAddItemDialog() {
        _showAddItemDialog.value = false
        _newItemName.value = ""
        _newItemDescription.value = ""
    }

    fun onNewItemNameChange(name: String) {
        _newItemName.value = name
    }

    fun onNewItemDescriptionChange(description: String) {
        _newItemDescription.value = description
    }

    fun onConfirmAddItem() {
        viewModelScope.launch {
            val name = _newItemName.value
            val description = _newItemDescription.value
            if (name.isNotBlank() && description.isNotBlank()) {
                _state.value = _state.value.copy(isLoading = true) // Indica carregamento
                when (val result = addItemUseCase(name, description)) {
                    is Resource.Success -> {
                        // O Flow de getItemsUseCase() deve atualizar a lista automaticamente
                        // Se não, chame loadItems() ou atualize o state manualmente
                        _state.value = _state.value.copy(isLoading = false, error = null)
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = result.message ?: "Erro ao adicionar item"
                        )
                    }
                    is Resource.Loading -> { /* Não deve acontecer aqui */ }
                }
                onDismissAddItemDialog() // Fecha o diálogo
            } else {
                _state.value = _state.value.copy(error = "Nome e descrição são obrigatórios.")
            }
        }
    }


    fun deleteItem(item: ItemEntity) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            when (val result = deleteItemUseCase(item)) {
                is Resource.Success -> {
                    // A lista deve atualizar via Flow
                    _state.value = _state.value.copy(isLoading = false, error = null)
                }
                is Resource.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = result.message ?: "Erro ao deletar item"
                    )
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun synchronizeItems() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSyncing = true, syncMessage = "Sincronizando...")
            when (val result = syncItemsUseCase()) {
                is Resource.Success -> {
                    _state.value = _state.value.copy(isSyncing = false, syncMessage = "Sincronização completa!")
                    loadItems() // Recarrega os itens após a sincronização
                }
                is Resource.Error -> {
                    _state.value = _state.value.copy(
                        isSyncing = false,
                        syncMessage = result.message ?: "Erro na sincronização."
                    )
                }
                is Resource.Loading -> {} // Já tratado pelo isSyncing
            }
        }
    }
}