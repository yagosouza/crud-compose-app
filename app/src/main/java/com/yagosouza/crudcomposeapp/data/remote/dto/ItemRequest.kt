package com.yagosouza.crudcomposeapp.data.remote.dto

// DTO para enviar dados para a API (pode não ter o ID ao criar)
data class ItemRequest(
    val name: String,
    val description: String
)