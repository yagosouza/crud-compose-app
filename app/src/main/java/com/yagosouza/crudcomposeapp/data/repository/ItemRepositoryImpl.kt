package com.yagosouza.crudcomposeapp.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.yagosouza.crudcomposeapp.data.local.dao.ItemDao
import com.yagosouza.crudcomposeapp.data.local.model.ItemEntity
import com.yagosouza.crudcomposeapp.data.remote.ApiService
import com.yagosouza.crudcomposeapp.data.remote.dto.ItemRequest
import com.yagosouza.crudcomposeapp.data.remote.dto.ItemDto
import com.yagosouza.crudcomposeapp.domain.repository.ItemRepository
import com.yagosouza.crudcomposeapp.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import retrofit2.HttpException
import java.io.IOException

// Helper para mapear DTO para Entity e vice-versa
fun ItemDto.toEntity(needsSync: Boolean = false): ItemEntity {
    return ItemEntity(
        serverId = this.id,
        name = this.name,
        description = this.description,
        needsSync = needsSync,
    )
}

fun ItemEntity.toCreateItemRequest(): ItemRequest {
    return ItemRequest(
        name = this.name,
        description = this.description
    )
}


class ItemRepositoryImpl(
    private val itemDao: ItemDao,
    private val apiService: ApiService,
    private val context: Context // Injetado para verificar a conectividade
) : ItemRepository {

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }

    override fun getItems(): Flow<Resource<List<ItemEntity>>> = flow {
        emit(Resource.Loading())

        // Primeiro, emite os dados locais
        val localItemsFlow = itemDao.getAllItems().map { Resource.Success(it) }
        localItemsFlow.collect { emit(it) } // Coleta e emite os dados locais

        // Tenta buscar do servidor se houver internet
        if (isNetworkAvailable()) {
            try {
                val response = apiService.getItems()
                if (response.isSuccessful) {
                    val remoteItems = response.body()
                    remoteItems?.let { dtos ->
                        itemDao.insertAll(dtos.map { it.toEntity() })
                        // Não precisa emitir aqui, o Flow do DAO já vai atualizar
                    }
                } else {
                    // Não emite erro aqui para não sobrescrever os dados locais se a API falhar
                    // Apenas loga ou trata o erro da API silenciosamente se os dados locais são a prioridade
                    println("ItemRepositoryImpl: Erro ao buscar itens da API: ${response.message()}")
                }
            } catch (e: HttpException) {
                println("ItemRepositoryImpl: Erro HTTP ao buscar itens: ${e.message()}")
            } catch (e: IOException) {
                println("ItemRepositoryImpl: Erro de IO ao buscar itens: ${e.message}")
            }
        }
    }


    override suspend fun addItem(name: String, description: String): Resource<ItemEntity> {
        if (isNetworkAvailable()) {
            try {
                val request = ItemRequest(name, description)
                val response = apiService.createItem(request)
                if (response.isSuccessful && response.body() != null) {
                    val createdItemDto = response.body()!!
                    val entity = createdItemDto.toEntity(needsSync = false) // Sincronizado
                    itemDao.insertItem(entity)
                    return Resource.Success(itemDao.getItemByServerId(entity.serverId!!)!!) // Busca para ter o localId
                } else {
                    // Falha na API, tenta salvar localmente como 'needsSync'
                    val localEntity = ItemEntity(name = name, description = description, needsSync = true)
                    val localId = itemDao.insertItem(localEntity)
                    return Resource.Success(itemDao.getItemByLocalId(localId)!!)
                }
            } catch (e: Exception) {
                // Qualquer exceção na API, salva localmente
                val localEntity = ItemEntity(name = name, description = description, needsSync = true)
                val localId = itemDao.insertItem(localEntity)
                return Resource.Success(itemDao.getItemByLocalId(localId)!!)
            }
        } else {
            // Sem internet, salva localmente
            val localEntity = ItemEntity(name = name, description = description, needsSync = true)
            val localId = itemDao.insertItem(localEntity)
            return Resource.Success(itemDao.getItemByLocalId(localId)!!)
        }
    }

    override suspend fun deleteItem(item: ItemEntity): Resource<Unit> {
        // Marca localmente para deleção e sincronização
        itemDao.markAsDeleted(item.localId)

        if (isNetworkAvailable()) {
            if (item.serverId != null) {
                try {
                    val response = apiService.deleteItem(item.serverId)
                    if (response.isSuccessful) {
                        itemDao.deleteItemByServerId(item.serverId) // Remove de vez do local
                        return Resource.Success(Unit)
                    } else {
                        // API falhou, mas já está marcado como needsSync = true e isDeleted = true
                        return Resource.Error("Erro ao deletar no servidor, item marcado para sincronização.")
                    }
                } catch (e: Exception) {
                    // API falhou, mas já está marcado como needsSync = true e isDeleted = true
                    return Resource.Error("Exceção ao deletar no servidor: ${e.message}, item marcado para sincronização.")
                }
            } else {
                // Item nunca foi para o servidor, apenas deleta localmente
                itemDao.deleteItemByLocalId(item.localId)
                return Resource.Success(Unit)
            }
        } else {
            // Sem internet, já está marcado para deleção e sincronização
            return Resource.Success(Unit) // Sucesso local, aguardando sincronização
        }
    }


    override suspend fun syncItems(): Resource<Unit> {
        if (!isNetworkAvailable()) {
            return Resource.Error("Sem conexão com a internet para sincronizar.")
        }

        //emit(Resource.Loading()) // Opcional: para indicar na UI que a sincronização começou

        val itemsToSync = itemDao.getItemsToSync()
        var allSynced = true

        for (item in itemsToSync) {
            try {
                if (item.isDeleted) { // Sincronizar deleção
                    if (item.serverId != null) { // Só deleta no servidor se tiver serverId
                        val deleteResponse = apiService.deleteItem(item.serverId)
                        if (deleteResponse.isSuccessful) {
                            itemDao.deleteItemByServerId(item.serverId) // Remove de vez do local
                        } else {
                            allSynced = false
                            println("Sync: Falha ao deletar item ${item.serverId} no servidor.")
                        }
                    } else {
                        itemDao.deleteItemByLocalId(item.localId) // Item local nunca sincronizado, apenas remove
                    }
                } else if (item.serverId == null) { // Item novo para criar no servidor
                    val createResponse = apiService.createItem(item.toCreateItemRequest())
                    if (createResponse.isSuccessful && createResponse.body() != null) {
                        val newServerId = createResponse.body()!!.id
                        // Atualiza o item local com o serverId e marca como sincronizado
                        itemDao.updateItem(item.copy(serverId = newServerId, needsSync = false))
                    } else {
                        allSynced = false
                        println("Sync: Falha ao criar item ${item.name} no servidor.")
                    }
                } else { // Item existente para atualizar no servidor
                    val updateResponse = apiService.updateItem(item.serverId, item.toCreateItemRequest())
                    if (updateResponse.isSuccessful) {
                        itemDao.updateItem(item.copy(needsSync = false))
                    } else {
                        allSynced = false
                        println("Sync: Falha ao atualizar item ${item.serverId} no servidor.")
                    }
                }
            } catch (e: Exception) {
                allSynced = false
                println("Sync: Exceção ao sincronizar item ${item.localId}: ${e.message}")
            }
        }

        // Após sincronizar, busca todos os itens do servidor para garantir consistência
        try {
            val response = apiService.getItems()
            if (response.isSuccessful) {
                val remoteItems = response.body()
                remoteItems?.let { dtos ->
                    itemDao.clearDeletedItems() // Limpa os que foram deletados com sucesso
                    itemDao.insertAll(dtos.map { it.toEntity() }) // Atualiza o banco local
                }
            } else {
                allSynced = false // Se a busca final falhar, a sincronização não foi completa
            }
        } catch (e: Exception) {
            allSynced = false
            println("Sync: Exceção ao buscar todos os itens do servidor após sincronização: ${e.message}")
        }

        return if(allSynced) Resource.Success(Unit) else Resource.Error("Alguns itens não puderam ser sincronizados.")
    }
}