package com.yagosouza.crudcomposeapp.domain.usecase

import com.yagosouza.crudcomposeapp.domain.repository.ItemRepository
import com.yagosouza.crudcomposeapp.util.Resource

class SyncItemsUseCase(private val repository: ItemRepository) {
    suspend operator fun invoke(): Resource<Unit> {
        return repository.syncItems()
    }
}