package com.yagosouza.crudcomposeapp.di

import com.yagosouza.crudcomposeapp.data.repository.ItemRepositoryImpl
import com.yagosouza.crudcomposeapp.domain.repository.ItemRepository
import com.yagosouza.crudcomposeapp.domain.usecase.AddItemUseCase
import com.yagosouza.crudcomposeapp.domain.usecase.DeleteItemUseCase
import com.yagosouza.crudcomposeapp.domain.usecase.GetItemsUseCase
import com.yagosouza.crudcomposeapp.domain.usecase.SyncItemsUseCase
import com.yagosouza.crudcomposeapp.ui.screen.itemlist.ItemListViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // Definições gerais do aplicativo, se necessário
    // Ex: single { android.content.Context } // Já injetado pelo androidContext
}

val repositoryModule = module {
    // (Será preenchido com a configuração dos Repositórios)
    single<ItemRepository> { ItemRepositoryImpl(get(), get(), androidContext()) }
}

val useCaseModule = module {
    // (Será preenchido com a configuração dos UseCases)
    factory { GetItemsUseCase(repository = get()) }
    factory { AddItemUseCase(repository = get()) }
    factory { DeleteItemUseCase(get()) }
    factory { SyncItemsUseCase(get()) }
}

val viewModelModule = module {
    // (Será preenchido com a configuração dos ViewModels)
    viewModel { ItemListViewModel(get(), get(), get(), get()) }

}