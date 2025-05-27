package com.yagosouza.crudcomposeapp.domain.usecase

import com.yagosouza.crudcomposeapp.data.local.model.ItemEntity
import com.yagosouza.crudcomposeapp.domain.repository.ItemRepository
import com.yagosouza.crudcomposeapp.util.Resource

class DeleteItemUseCase(private val repository: ItemRepository) {
    suspend operator fun invoke(item: ItemEntity): Resource<Unit> {
        return repository.deleteItem(item)
    }
}