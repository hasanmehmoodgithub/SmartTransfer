package com.smart.transfer.app.di

import androidx.room.Room
import com.smart.transfer.app.com.smart.transfer.app.features.history.data.database.AppDatabase
import com.smart.transfer.app.com.smart.transfer.app.features.history.viewmodel.HistoryViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module


val databaseModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "history_database_test_1",
        ).build()
    }

    single { get<AppDatabase>().historyDao() }
}

val viewModelModule = module {
    viewModel { HistoryViewModel(get()) }
}

val appModules = listOf(databaseModule, viewModelModule)
