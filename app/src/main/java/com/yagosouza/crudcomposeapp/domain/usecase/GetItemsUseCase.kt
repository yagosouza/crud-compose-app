package com.yagosouza.crudcomposeapp.domain.usecase

import com.yagosouza.crudcomposeapp.data.local.model.ItemEntity
import com.yagosouza.crudcomposeapp.domain.repository.ItemRepository
import com.yagosouza.crudcomposeapp.util.Resource
import kotlinx.coroutines.flow.Flow

class GetItemsUseCase(private val repository: ItemRepository) {
    operator fun invoke(): Flow<Resource<List<ItemEntity>>> {
        return repository.getItems()
    }
}