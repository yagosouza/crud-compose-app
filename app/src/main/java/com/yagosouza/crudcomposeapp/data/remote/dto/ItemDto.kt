package com.yagosouza.crudcomposeapp.data.remote.dto

// DTO para receber dados da API (pode ser diferente da Entity)
data class ItemDto(
    val id: String, // Supondo que o servidor usa String como ID
    val name: String,
    val description: String,
    // Outros campos que a API retorna
)
