package com.yagosouza.crudcomposeapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.yagosouza.crudcomposeapp.data.local.dao.ItemDao
import com.yagosouza.crudcomposeapp.data.local.model.ItemEntity

@Database(entities = [ItemEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao

    companion object {
        const val DATABASE_NAME = "crud_app_db"
    }
}