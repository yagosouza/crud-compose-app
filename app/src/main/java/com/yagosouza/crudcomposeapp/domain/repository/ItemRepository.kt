package com.yagosouza.crudcomposeapp.domain.repository

import com.yagosouza.crudcomposeapp.data.local.model.ItemEntity
import com.yagosouza.crudcomposeapp.util.Resource
import kotlinx.coroutines.flow.Flow

interface ItemRepository {
    fun getItems(): Flow<Resource<List<ItemEntity>>> // Observa itens do banco local, tenta sincronizar
    suspend fun addItem(name: String, description: String): Resource<ItemEntity>
    suspend fun deleteItem(item: ItemEntity): Resource<Unit>
    suspend fun syncItems(): Resource<Unit> // Força uma sincronização
    //Pode ser necessário um método para checar conectividade
    // fun isNetworkAvailable(): Boolean
}