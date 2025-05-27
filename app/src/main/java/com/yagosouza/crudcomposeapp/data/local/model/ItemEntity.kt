package com.yagosouza.crudcomposeapp.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "items")
data class ItemEntity(
    @PrimaryKey(autoGenerate = true) val localId: Long = 0, // Chave primária local
    val serverId: String? = null, // ID do servidor, pode ser nulo se ainda não sincronizado
    val name: String,
    val description: String,
    val needsSync: Boolean = false, // Flag para sincronização
    val isDeleted: Boolean = false // Para soft delete antes de sincronizar a exclusão
)