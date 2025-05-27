package com.yagosouza.crudcomposeapp.data.remote.dto

// DTO para resposta da API ao criar/atualizar (pode ser o ItemDto ou algo mais simples)
data class ApiResponse<T>( // Exemplo de uma resposta genérica da API
    val success: Boolean,
    val data: T?,
    val message: String?
)