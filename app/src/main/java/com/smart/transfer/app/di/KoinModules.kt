package com.smart.transfer.app.di

import androidx.room.Room
import com.smart.transfer.app.com.smart.transfer.app.features.history.data.database.AppDatabase
import com.smart.transfer.app.com.smart.transfer.app.features.history.data.repository.HistoryRepository
import com.smart.transfer.app.com.smart.transfer.app.features.history.data.repository.HistoryRepositoryImpl
import com.smart.transfer.app.com.smart.transfer.app.features.history.viewmodel.HistoryViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module


val databaseModule = module {
    // Database instance
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "history_database_test_1"
        ).build()
    }

    // DAO
    single { get<AppDatabase>().historyDao() }
}


val viewModelModule = module {
    viewModel { HistoryViewModel(get()) }
}
val repositoryModule = module {
    single<HistoryRepository> { HistoryRepositoryImpl(get()) }
}
val appModules = listOf(databaseModule,repositoryModule, viewModelModule)
