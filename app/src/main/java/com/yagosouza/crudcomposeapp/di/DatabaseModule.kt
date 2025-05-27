package com.yagosouza.crudcomposeapp.di

import androidx.room.Room
import com.yagosouza.crudcomposeapp.data.local.AppDatabase
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val databaseModule = module {
    // (Será preenchido com a configuração do Room)
    single {
        Room.databaseBuilder(
            androidApplication(),
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration() // Em produção, defina uma estratégia de migração
            .build()
    }

    single { get<AppDatabase>().itemDao() }
}