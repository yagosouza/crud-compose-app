package com.yagosouza.crudcomposeapp.data.remote

import com.yagosouza.crudcomposeapp.data.remote.dto.ItemRequest
import com.yagosouza.crudcomposeapp.data.remote.dto.ItemDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {
    @GET("items.json") // Ex: "https://sua-api.com/items"
    suspend fun getItems(): Response<List<ItemDto>> // Usar List<ItemDto> se a API retornar uma lista diretamente

    @GET("items/{id}")
    suspend fun getItemById(@Path("id") itemId: String): Response<ItemDto>

    @POST("items")
    suspend fun createItem(@Body itemRequest: ItemRequest): Response<ItemDto> // API retorna o item criado

    @PUT("items/{id}")
    suspend fun updateItem(@Path("id") itemId: String, @Body itemRequest: ItemRequest): Response<ItemDto>

    @DELETE("items/{id}")
    suspend fun deleteItem(@Path("id") itemId: String): Response<Unit> // Sem corpo na resposta
}