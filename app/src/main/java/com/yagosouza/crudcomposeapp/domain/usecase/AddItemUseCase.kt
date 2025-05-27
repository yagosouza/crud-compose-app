package com.yagosouza.crudcomposeapp.domain.usecase

import com.yagosouza.crudcomposeapp.data.local.model.ItemEntity
import com.yagosouza.crudcomposeapp.domain.repository.ItemRepository
import com.yagosouza.crudcomposeapp.util.Resource

class AddItemUseCase(private val repository: ItemRepository) {
    suspend operator fun invoke(name: String, description: String): Resource<ItemEntity> {
        if (name.isBlank() || description.isBlank()) {
            return Resource.Error("Nome e descrição não podem estar vazios.")
        }
        return repository.addItem(name, description)
    }
}