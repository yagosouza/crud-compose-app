package com.yagosouza.crudcomposeapp

import android.app.Application
import com.yagosouza.crudcomposeapp.di.appModule
import com.yagosouza.crudcomposeapp.di.databaseModule
import com.yagosouza.crudcomposeapp.di.networkModule
import com.yagosouza.crudcomposeapp.di.repositoryModule
import com.yagosouza.crudcomposeapp.di.useCaseModule
import com.yagosouza.crudcomposeapp.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@App)
            modules(
                listOf(
                    appModule,
                    networkModule,
                    databaseModule,
                    repositoryModule,
                    useCaseModule,
                    viewModelModule
                )
            )
        }
    }
}