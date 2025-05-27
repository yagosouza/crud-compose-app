package com.yagosouza.crudcomposeapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.yagosouza.crudcomposeapp.data.local.model.ItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {
    @Query("SELECT * FROM items WHERE isDeleted = 0 ORDER BY name ASC")
    fun getAllItems(): Flow<List<ItemEntity>> // Observa mudanças

    @Query("SELECT * FROM items WHERE serverId = :serverId AND isDeleted = 0")
    suspend fun getItemByServerId(serverId: String): ItemEntity?

    @Query("SELECT * FROM items WHERE localId = :localId AND isDeleted = 0")
    suspend fun getItemByLocalId(localId: Long): ItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ItemEntity): Long // Retorna o localId gerado

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ItemEntity>)

    @Update
    suspend fun updateItem(item: ItemEntity)

    // Soft delete: marca como deletado para sincronização posterior
    @Query("UPDATE items SET isDeleted = 1, needsSync = 1 WHERE localId = :localId")
    suspend fun markAsDeleted(localId: Long)

    @Query("UPDATE items SET isDeleted = 1, needsSync = 1 WHERE serverId = :serverId")
    suspend fun markAsDeletedByServerId(serverId: String)

    // Hard delete: remove do banco local (usado após sincronização da exclusão)
    @Query("DELETE FROM items WHERE localId = :localId")
    suspend fun deleteItemByLocalId(localId: Long)

    @Query("DELETE FROM items WHERE serverId = :serverId")
    suspend fun deleteItemByServerId(serverId: String)

    @Query("SELECT * FROM items WHERE needsSync = 1")
    suspend fun getItemsToSync(): List<ItemEntity>

    @Query("DELETE FROM items WHERE isDeleted = 1") // Limpa os itens marcados como deletados
    suspend fun clearDeletedItems()
}